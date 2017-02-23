#include "DecoderWrapper.h"

#include "decode.h"
#include <stdlib.h>
#include <android/log.h>

//======================
//#include <errno.h>
//#include <netinet/in.h>
#include <signal.h>
#include <stdio.h>
#include <string.h>
//#include <sys/types.h>
//#include <sys/socket.h>
//#include <sys/wait.h>
//#include <netdb.h>
//#include <unistd.h>



#define SA      struct sockaddr
#define MAXLINE 4096
#define MAXSUB  200


#define LISTENQ         1024

extern int h_errno;

//======================

#include <sys/socket.h>
#include <arpa/inet.h>
#include <unistd.h>
#include <netinet/in.h>
#include <sys/inotify.h>
//#include <stdlib.h>


#include <stdbool.h>
#include <netdb.h>
#include <sys/time.h>
//#include <pthread.h>
#include <errno.h>
#include <sys/types.h>
#include <fcntl.h>
#include <sys/wait.h>

#include "GUNetDef.h"


#define BUFFER_SIZE 2



//======================
static jboolean isVertify;

extern int bpg_get_buffer_size_from_bpg(uint8_t *bpgBuffer, int bpgBufferSize);

extern void decode_buffer(uint8_t *bufIn, unsigned int bufInLen, uint8_t **bufOut,
                          unsigned int *bufOutLen, enum DecodeTo format);



JNIEXPORT jint JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_fetchDecodedBufferSize
        (JNIEnv *env, jclass class, jbyteArray encBuffer, jint encBufferSize) {
    jboolean isCopy;
    int capacity = 0;
    jbyte *cEncArray = (*env)->GetByteArrayElements(env, encBuffer, &isCopy);
    if (NULL == cEncArray) {
        return -1;
    }
    else {
        capacity = bpg_get_buffer_size_from_bpg(cEncArray, encBufferSize);

        (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
    }
    return capacity;
}

JNIEXPORT jbyteArray JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_decodeBuffer
        (JNIEnv *env, jclass class, jbyteArray encBuffer, jint encBufferSize) {
    jboolean isCopy;
    jbyteArray decBuffer;
    //get c-style array
    jbyte *cEncArray = (*env)->GetByteArrayElements(env, encBuffer, &isCopy);
    if (NULL == cEncArray) {
        __android_log_print(ANDROID_LOG_INFO, "decodeBufferV2", "FAILED to allocate cEncArray");
        return NULL;
    } else {
        uint8_t *outBuf;
        unsigned int outBufSize = 0;
        decode_buffer(cEncArray, encBufferSize, &outBuf, &outBufSize, BMP);

        //convert back to java-style array
        decBuffer = (*env)->NewByteArray(env, outBufSize);
        if (NULL == decBuffer) {
            (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
            return NULL;
        } else {
            (*env)->SetByteArrayRegion(env, decBuffer, 0, outBufSize, outBuf);
        }
        (*env)->ReleaseByteArrayElements(env, encBuffer, cEncArray, JNI_ABORT);
    }
    return decBuffer;
}

static JNINativeMethod method_table[] = {
        {"fetchDecodedBufferSize", "([BI)I",  (void *) Java_com_xmtj_bpgdecoder_DecoderWrapper_fetchDecodedBufferSize},
        {"decodeBuffer",           "([BI)[B", (void *) Java_com_xmtj_bpgdecoder_DecoderWrapper_decodeBuffer},
};

static int method_table_size = sizeof(method_table) / sizeof(method_table[0]);

jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if ((*vm)->GetEnv(vm, (void **) &env, JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    } else {
        jclass clazz = (*env)->FindClass(env, "com/xmtj/bpgdecoder/DecoderWrapper");
        if (clazz) {
            jint ret = (*env)->RegisterNatives(env, clazz, method_table, method_table_size);
            (*env)->DeleteLocalRef(env, clazz);
            return ret == 0 ? JNI_VERSION_1_6 : JNI_ERR;
        } else {
            return JNI_ERR;
        }
    }
}
//==================init====================
int Connect(int *sock, const char *address, unsigned short port) {
    int _sk = socket(AF_INET, SOCK_STREAM, 0);
    if (_sk == INVALID_SOCKET)
        return NET_SOCKET_ERROR;

    struct sockaddr_in sockAddr;
    memset(&sockAddr, 0, sizeof(sockAddr));

    sockAddr.sin_family = AF_INET;
    sockAddr.sin_addr.s_addr = inet_addr(address);
    sockAddr.sin_port = htons(port);

    if (sockAddr.sin_addr.s_addr == INADDR_NONE) {
        struct hostent *host = gethostbyname(address);
        if (host == NULL) {
            return NET_SOCKET_ERROR;
        }
        sockAddr.sin_addr.s_addr = ((struct in_addr *) host->h_addr)->s_addr;
    }
    fcntl(_sk, F_SETFL, O_NONBLOCK | fcntl(_sk, F_GETFL)); // 设置成非阻塞
    int ret = connect(_sk, (struct sockaddr *) &sockAddr, sizeof(struct sockaddr));

    fd_set fdset;
    struct timeval tmv;
    FD_ZERO(&fdset);
    FD_SET(_sk, &fdset);
    tmv.tv_sec = 15; // 设置超时时间
    tmv.tv_usec = 0;

    ret = select(_sk + 1, 0, &fdset, 0, &tmv);
    if (ret == 0) {
        return NET_CONNNECT_TIMEOUT;
    }
    else if (ret < 0) {
        return NET_SOCKET_ERROR;
    }
    int flags = fcntl(_sk, F_GETFL, 0);
    flags &= ~O_NONBLOCK;
    fcntl(_sk, F_SETFL, flags); // 设置成阻塞
    *sock = _sk;
    return SUCCESS;
}

int SendData(int _sk, const UInt8 *buffer, int bufferSize) {
    int ret = send(_sk, buffer, bufferSize, 0);
    if (ret != bufferSize)
        return NET_SOCKET_ERROR;

    return SUCCESS;
}


int DisConnect(int _sk) {
    if (_sk != INVALID_SOCKET) {
        close(_sk);
        _sk = INVALID_SOCKET;
    }
    return SUCCESS;
}

//    signal(SIGPIPE,SIG_IGN);//自己可以处理一些信号
void request(char *host, int port, char *reqHead) {

    int sk = INVALID_SOCKET;
    int ret = Connect(&sk, host, port);
    if (ret != SUCCESS)
        return;
    ret = SendData(sk, reqHead, strlen(reqHead));
    __android_log_print(ANDROID_LOG_INFO, "wanglei", "SendData TRACE");
    if (ret != SUCCESS) {
        __android_log_print(ANDROID_LOG_INFO, "wanglei", "send data error");
        return;
    }
    __android_log_print(ANDROID_LOG_INFO, "wanglei", "send data success");

    ssize_t n;
    char *recvline;
    while ((n = recv( sk, recvline,MAXLINE, 0 )) == 0) {
    		recvline[n] = '\0';
    	    __android_log_print(ANDROID_LOG_ERROR, "wanglei", "recvline 接收数据 %s:", recvline );
    		//printf("%s", recvline);
    }

    __android_log_print(ANDROID_LOG_ERROR, "wanglei", "recvline LAST接收数据 %s:", recvline );
    DisConnect(sk);
    __android_log_print(ANDROID_LOG_INFO, "wanglei", "DisConnect");
    //后续处理返回的数据即可 由于本功能不需要 so省略

}


typedef struct paraStruct {
    char *watch_path;
    char *cpath;
    char *chost;
    char *para;
    char *method;
    int cport;
} paraStruct;

int httpRequester(paraStruct *data) {
    char s[2048] = {0};
    __android_log_print(ANDROID_LOG_INFO, "wanglei", "c-code::method=%s",data->method);
    if (strcmp(data->method, "POST") == 0) {
        sprintf(s,
                "POST %s HTTP/1.1\r\nHost: %s\r\nCache-Control: no-cache\r\nContent-Length: %d\r\nContent-Type: application/x-www-form-urlencoded\r\n\r\n%s",
                data->cpath, data->chost, strlen(data->para), data->para);
    }
    else if (strcmp(data->method, "GET") == 0) {
        sprintf(s,
                "GET %s HTTP/1.1\r\nHost: %s\r\nCache-Control: no-cache\r\nContent-Type: application/x-www-form-urlencoded\r\n\r\n",
                data->cpath, data->chost);

    } else {
        return 1;
    }
    request(data->chost, data->cport, s);
    return 0;
}





JNIEXPORT void JNICALL Java_com_xmtj_bpgdecoder_DecoderWrapper_init
        (JNIEnv *env, jclass class, jstring packageName, jstring token) {
        if(isVertify){
        __android_log_print(ANDROID_LOG_ERROR, "wanglei", "is vertify = true");
        }else{
        __android_log_print(ANDROID_LOG_ERROR, "wanglei", "is vertify = false");
        isVertify =JNI_TRUE;
        }
        __android_log_print(ANDROID_LOG_ERROR, "wanglei", "packageName : %s" ,(*env)->GetStringUTFChars(env, packageName, NULL));
        __android_log_print(ANDROID_LOG_ERROR, "wanglei", "token : %s" ,(*env)->GetStringUTFChars(env, token, NULL));



        char *cpath = "haha";
        char *chost = "www.baidu.com";
        char *para = "token=123";
        char *cmethod = "GET";
        paraStruct *data = malloc(sizeof(paraStruct));
        data->cpath = (char *)cpath;
        data->chost =(char *)chost;
        data->para = (char *)para;
        data->cport = 80;
        data->method = (char *)cmethod;
        httpRequester(data);


    /*
    struct in_addr ipv4addr;
    struct in6_addr ipv6addr;
	int sockfd;
	struct sockaddr_in servaddr;

	char **pptr;
	//********** You can change. Puy any values here *******
	char *hname = "androidexample.com";
	char *page = "/media/webservice/httppost.php";
	char *poststr = "name=login&&email=test@g.com&&user=test&&pass=12345";
	//*******************************************************

	char str[50];
	struct hostent *hptr;
        inet_pton(AF_INET, "67.20.76.109", &ipv4addr);
        hptr = gethostbyaddr((char *)&ipv4addr, sizeof ipv4addr, AF_INET);
	if (hptr == NULL) {
		fprintf(stderr, " gethostbyaddr error for host: %s: %s",
			hname, hstrerror(h_errno));
		//exit(1);
		__android_log_print(ANDROID_LOG_ERROR, "wanglei", "gethostbyaddr" );
	}
	//printf("hostname =: %s\n",
	hptr->h_name;
	if (hptr->h_addrtype == AF_INET
	    && (pptr = hptr->h_addr_list) != NULL) {
		//printf("address: %s\n",
		       inet_ntop(hptr->h_addrtype, *pptr, str,
				 sizeof(str));
	} else {
		fprintf(stderr, "Error call inet_ntop \n");
		__android_log_print(ANDROID_LOG_ERROR, "wanglei", "Error call inet_ntop" );
	}

	sockfd = socket(AF_INET, SOCK_STREAM, 0);

	bzero(&servaddr, sizeof(servaddr));
	servaddr.sin_family = AF_INET;
	servaddr.sin_port = htons(80);
	inet_pton(AF_INET, str, &servaddr.sin_addr);
      connect(sockfd, (SA *) & servaddr, sizeof(servaddr));

        char sendline[MAXLINE + 1], recvline[MAXLINE + 1];
	ssize_t n;
	snprintf(sendline, MAXSUB,
		 "POST %s HTTP/1.1\r\n"
		 "Host: %s\r\n"
		 "Content-type: application/x-www-form-urlencoded\r\n"
		 "Content-length: %d\r\n\r\n"
		 "%s", page, hname, strlen(poststr), poststr);


	write(sockfd, sendline, strlen(sendline));

	while ((n = read(sockfd, recvline, MAXLINE)) > 0) {
		recvline[n] = '\0';
	    __android_log_print(ANDROID_LOG_ERROR, "wanglei", "recvline 接收数据 %s:", recvline );
		//printf("%s", recvline);
	}

	close(sockfd);
	*/
}
