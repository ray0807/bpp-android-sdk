package com.xmtj.bpgdecoder;

import android.database.Cursor;
import android.util.Log;

import com.xmtj.bpgdecoder.db.DBHelperManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DecoderWrapper {


    public static native void init(String packageName, String token);

    private static native int fetchDecodedBufferSize(byte[] encBuffer, int encBufferSize);

    private static native byte[] decodeBuffer(byte[] encBuffer, int encBufferSize);

    public static byte[] decodeBpgBuffer(InputStream input) throws IOException {
        byte[] bytes = toByteArray(input);
        return decodeBuffer(bytes, bytes.length);
    }

    private static byte[] toByteArray(InputStream input) throws IOException {
        byte[] countByte = new byte[1];
        if (input.read(countByte) > 0) {
            Log.e("wanglei", "countByte:" + countByte[0]);
            byte[] idByte = new byte[countByte[0]];
            Log.e("wanglei", "idByte.length: " + idByte.length);
            long id = 0;
            switch (input.read(idByte)) {
                case 0:
                    break;
                case 1:
                    id = idByte[0];
                    Log.e("wanglei", "id:" + id);
                    break;
                case 2:
                    id = (idByte[0] * 100 + idByte[1]);
                    Log.e("wanglei", "id:" + id);
                    break;
                default:
                    id = 1;
                    break;
            }
            if (id > 0) {
                saveIds(id);
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

    private static synchronized void saveIds(long key) {
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
                        Log.e("wanglei", "queryCursor.getInt(1):" + count);
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


}
