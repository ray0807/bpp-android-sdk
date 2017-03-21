package com.xmtj.bpgdecoder.Utils;

import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ray
 * @time 2017-03-20
 * @github https://github.com/ray0807
 * @desc
 */

public class MemoryUtils {

    /**
     * 返回m
     *
     * @return
     */
    public static long getFreeMemory() {
        Method _readProclines = null;
        try {
            Class procClass;
            procClass = Class.forName("android.os.Process");
            Class parameterTypes[] = new Class[]{String.class, String[].class, long[].class};
            _readProclines = procClass.getMethod("readProcLines", parameterTypes);
            Object arglist[] = new Object[3];
            final String[] mMemInfoFields = new String[]{"MemTotal:",
                    "MemFree:", "Buffers:", "Cached:"};
            long[] mMemInfoSizes = new long[mMemInfoFields.length];
            mMemInfoSizes[0] = 30;
            mMemInfoSizes[1] = -30;
            arglist[0] = new String("/proc/meminfo");
            arglist[1] = mMemInfoFields;
            arglist[2] = mMemInfoSizes;
            if (_readProclines != null) {
                _readProclines.invoke(null, arglist);
                for (int i = 0; i < mMemInfoSizes.length; i++) {
                    if (mMemInfoFields[i] != null && mMemInfoFields[i].contains("MemFree")) {
                        return mMemInfoSizes[i] / 1024;
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
