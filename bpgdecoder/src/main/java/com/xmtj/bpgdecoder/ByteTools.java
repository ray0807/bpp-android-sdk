package com.xmtj.bpgdecoder;

import android.database.Cursor;
import android.util.Log;

import com.xmtj.bpgdecoder.db.DBHelperManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by wanglei on 01/03/17.
 */

public class ByteTools {


    protected static byte[] toByteArray(InputStream input) throws IOException {
        byte[] idByte = new byte[12];
        String id;
        if (input.read(idByte) > 0) {
            id = bytesToHexString(idByte);
            if (id != null && id.length() > 0) {
                BPG.getSingleThreadExecutor().execute(new BpgSaveIdThread(id));
            } else {
                Log.e(BPG.BPG_TAG, "Illegal image resource");
            }

        }

        byte[] buffer = new byte[1024];
        int bytesRead;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        while ((bytesRead = input.read(buffer)) != -1) {
            output.write(buffer, 0, bytesRead);
        }
        return output.toByteArray();
    }


    private static synchronized void saveIds(String key) {
        if (null != BPG.getmDBHelper()) {
            DBHelperManager dbm = null;
            Cursor queryCursor = null;
            try {
                dbm = BPG.getmDBHelper();
                dbm.open();
                queryCursor = dbm.getBpgCount(key);
                int count = 0;
                if (queryCursor != null) {
                    if (queryCursor.getCount() > 0) {
                        count = queryCursor.getInt(1);
                    }
                }
                count++;
                if (count == 1) {
                    dbm.addBpgCount(key, count);
                } else {
                    dbm.updateBpgCount(key, count);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (null != queryCursor)
                    queryCursor.close();
                if (null != dbm) {
                    dbm.close();
                }
            }


        }

    }

    private static class BpgSaveIdThread implements Runnable {
        private String id;

        public BpgSaveIdThread(String id) {
            this.id = id;
        }

        @Override
        public void run() {
            saveIds(id);
        }
    }

    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
}
