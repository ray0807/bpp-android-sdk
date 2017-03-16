#include "DecoderWrapper.h"

#include "decode.h"
#include <stdlib.h>
#include <android/log.h>
#include "libhttp_public.h"
#include "../http/libnhr.h"
#include <sys/time.h>
#include "md5.h"
long getCurrentTime()
{
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return tv.tv_sec * 1000 + tv.tv_usec / 1000;
}

static jboolean isVertify = JNI_FALSE;
static unsigned int canUseCount = 20;
static const char *TAG = "xmtj_bpgdecoder";

extern int bpg_get_buffer_size_from_bpg(uint8_t *bpgBuffer, int bpgBufferSize);

extern void decode_buffer(uint8_t *bufIn, unsigned int bufInLen, uint8_t **bufOut,
                          unsigned int *bufOutLen, enum DecodeTo format);

static nhr_request test_post_request = NULL;
static int test_post_error = 0;
static nhr_bool test_post_working = 0;
//static const char *test_get_param_name1 = "test_get_param_name1";
//static const char *test_get_param_value1 = "test_get_param_value1";

static void test_post_on_error(nhr_request request, nhr_error_code error_code)
{
    __android_log_print(ANDROID_LOG_ERROR, TAG, "Responce error: %i", (int)error_code);
    test_post_error = error_code;
    test_post_working = nhr_false;
}

static int test_post_parse_body(const char *body)
{
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "request: %s", body);
    cJSON *json = cJSON_ParseWithOpts(body, NULL, 0);
    // cJSON *args = json ? cJSON_GetObjectItem(json, "args") : NULL;
    // cJSON *headers = json ? cJSON_GetObjectItem(json, "headers") : NULL;
    // cJSON *param1 = args ? cJSON_GetObjectItem(args, test_get_param_name1) : NULL;
    // cJSON *deflated = json ? cJSON_GetObjectItem(json, "deflated") : NULL;
    // cJSON *gzipped = json ? cJSON_GetObjectItem(json, "gzipped") : NULL;

    // cJSON *host = headers ? cJSON_GetObjectItem(headers, "Host") : NULL;

    cJSON *errorCodeJson = json ? cJSON_GetObjectItem(json, "error") : NULL;
    cJSON *messageJson = json ? cJSON_GetObjectItem(json, "message") : NULL;
    int errorCode = errorCodeJson ? errorCodeJson->valueint : -1;
    
    if (messageJson)
    {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "bpg init : %s", messageJson->valuestring);
    }
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "bpg init errorCode : %d", errorCode);
    if (errorCode == 0)
    {
        isVertify = JNI_TRUE;
    }
    return errorCode;
}

static void test_post_log_body(const char *body, const unsigned int body_len)
{
    unsigned int i;
    if (!body || body_len == 0)
        return;
    for (i = 0; i < body_len; i++)
    {
        __android_log_print(ANDROID_LOG_INFO, TAG, "test_post_log_body %c", body[i]);
    }
}

static void test_post_on_response(nhr_request request, nhr_response responce)
{
    char *body = nhr_response_get_body(responce);
    unsigned int body_len = nhr_response_get_body_length(responce);
    // unsigned long test_number = (unsigned long)nhr_request_get_user_object(request);
    test_post_error = 1;

    //	test_post_log_body(body, body_len);
    // if (test_number == 0)
    // {
    //     test_post_error = 10;
    //     test_post_working = nhr_false;
    //     return;
    // }

    if (nhr_response_get_status_code(responce) != 200)
    {
        test_post_error = 15;
        test_post_working = nhr_false;
        return;
    }

    if (body && body_len)
    {
        test_post_error = test_post_parse_body(body);
    }
    else
    {
        test_post_error = 5;
    }

    test_post_working = nhr_false;
}

static int test_post_number(const char *packageName, const char *token, const char *timestamp)
{

    if (test_post_working)
    {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "OS is sending request");
        test_post_error = 199;
        return test_post_error;
    }

    test_post_request = nhr_request_create();

    nhr_request_set_url(test_post_request, "http", "testbpg.mkzcdn.com", "/sdk/index/check", 80);

    nhr_request_set_method(test_post_request, nhr_method_POST);
    nhr_request_set_timeout(test_post_request, 10);

    // nhr_request_set_user_object(test_post_request, (void *)number);

    nhr_request_add_header_field(test_post_request, "Cache-control", "no-cache");
    nhr_request_add_header_field(test_post_request, "Accept-Charset", "utf-8");
    nhr_request_add_header_field(test_post_request, "Accept", "application/json");
    nhr_request_add_header_field(test_post_request, "Connection", "close");
    nhr_request_add_header_field(test_post_request, "User-Agent", "CMake tests");

    nhr_request_add_parameter(test_post_request, "app_name", packageName);
    nhr_request_add_parameter(test_post_request, "app_key", token);
    nhr_request_add_parameter(test_post_request, "timestamp", timestamp);
    nhr_request_add_parameter(test_post_request, "app_type", "1");

    nhr_request_set_on_recvd_responce(test_post_request, &test_post_on_response);
    nhr_request_set_on_error(test_post_request, &test_post_on_error);
    test_post_working = nhr_request_send(test_post_request);

    if (test_post_working)
        test_post_error = 0;
    else
        test_post_error = 4;

    while (test_post_working)
    {
        nhr_thread_sleep(20);
    }

    //nhr_thread_sleep(834); // just delay between requests

    return test_post_error;
}

JNIEXPORT void JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_init(JNIEnv *env, jclass class, jstring packageName, jstring token)
{
    long currentTime = getCurrentTime()/1000;
    
    char app_key[50]={0};
    char timestemp[15]={0};
    sprintf(timestemp,"%ld",currentTime);
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "before md5 timestamp : %s", timestemp);
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "before md5 token : %s", (*env)->GetStringUTFChars(env, token, NULL));
    //拼接的待加密字符串，可以根据自身需求修改
    sprintf(app_key,"%s%s",timestemp,(*env)->GetStringUTFChars(env, token, NULL));
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "before md5 app_key : %s", app_key);
    MD5_CTX context = {0};
    MD5Init(&context);
    MD5Update(&context, app_key, strlen(app_key));
    uint8_t dest[16] = {0};
    MD5Final(dest, &context);
    // (*env)->ReleaseStringUTFChars(old);

    int i = 0;
    char szMd5[32] = {0};
    for (i = 0; i < 16; i++)
    {
        sprintf(szMd5, "%s%02x", szMd5, dest[i]);
    }
    // __android_log_print(ANDROID_LOG_ERROR, TAG, "after md5 app_key : %s", szMd5);
    //if (isVertify)
    //{
    //  __android_log_print(ANDROID_LOG_ERROR, TAG, "vertify = true");
    //}
    //else
    //{
    //  __android_log_print(ANDROID_LOG_ERROR, TAG, "vertify = false");
    //}
    test_post_number((*env)->GetStringUTFChars(env, packageName, NULL), szMd5, timestemp);
    //__android_log_print(ANDROID_LOG_ERROR, TAG, "packageName : %s", (*env)->GetStringUTFChars(env, packageName, NULL));
    //__android_log_print(ANDROID_LOG_ERROR, TAG, "token : %s", (*env)->GetStringUTFChars(env, token, NULL));
}

JNIEXPORT jboolean JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_getInitState(JNIEnv *env, jclass class)
{
    return isVertify;
}

JNIEXPORT jint JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_fetchDecodedBufferSize(JNIEnv *env, jclass class, jbyteArray encBuffer, jint encBufferSize)
{
    jboolean isCopy;
    int capacity = 0;
    jbyte *cEncArray = (*env)->GetByteArrayElements(env, encBuffer, &isCopy);
    if (NULL == cEncArray)
    {
        return -1;
    }
    else
    {
        capacity = bpg_get_buffer_size_from_bpg(cEncArray, encBufferSize);

        (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
    }
    return capacity;
}

JNIEXPORT jbyteArray JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_decodeBuffer(JNIEnv *env, jclass class, jbyteArray encBuffer, jint encBufferSize)
{
    if (isVertify || canUseCount > 0)
    {
        canUseCount--;
        jboolean isCopy;
        jbyteArray decBuffer;
        //get c-style array
        jbyte *cEncArray = (*env)->GetByteArrayElements(env, encBuffer, &isCopy);
        if (NULL == cEncArray)
        {
            __android_log_print(ANDROID_LOG_ERROR, TAG, "FAILED to allocate cEncArray");
            return NULL;
        }
        else
        {
            uint8_t *outBuf;
            unsigned int outBufSize = 0;
            decode_buffer(cEncArray, encBufferSize, &outBuf, &outBufSize, BMP);

            //convert back to java-style array
            decBuffer = (*env)->NewByteArray(env, outBufSize);
            if (NULL == decBuffer)
            {
                (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
                return NULL;
            }
            else
            {
                (*env)->SetByteArrayRegion(env, decBuffer, 0, outBufSize, outBuf);
            }
            (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
        }
        return decBuffer;
    }
    return NULL;
}

static JNINativeMethod method_table[] = {
    {"fetchDecodedBufferSize", "([BI)I", (void *)Java_com_xmtj_bpgdecoder_DecoderWrapper_fetchDecodedBufferSize},
    {"decodeBuffer", "([BI)[B", (void *)Java_com_xmtj_bpgdecoder_DecoderWrapper_decodeBuffer},
};

static int method_table_size = sizeof(method_table) / sizeof(method_table[0]);

jint JNI_OnLoad(JavaVM *vm, void *reserved)
{
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **)&env, JNI_VERSION_1_6) != JNI_OK)
    {
        return JNI_ERR;
    }
    else
    {
        jclass clazz = (*env)->FindClass(env, "com/xmtj/bpgdecoder/DecoderWrapper");
        if (clazz)
        {
            jint ret = (*env)->RegisterNatives(env, clazz, method_table, method_table_size);
            (*env)->DeleteLocalRef(env, clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        }
        else
        {
            return JNI_ERR;
        }
    }
}
