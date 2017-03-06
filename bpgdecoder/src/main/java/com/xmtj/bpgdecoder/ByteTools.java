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
        byte[] countByte = new byte[1];
        long id = 0;
        if (input.read(countByte) > 0) {
            byte[] idByte = new byte[countByte[0]];
            long b0 = 0;
            long b1 = 0;
            long b2 = 0;
            long b3 = 0;
            switch (input.read(idByte)) {
                case 0:
                    break;
                case 1:
                    id = idByte[0] & 0xff;
                    break;
                case 2:
                    b0 = idByte[0] & 0xff;
                    b1 = idByte[1] & 0xff;
                    id = ((b0 << 8) + b1);
                    break;
                case 3:
                    b0 = idByte[0] & 0xff;
                    b1 = idByte[1] & 0xff;
                    b2 = idByte[2] & 0xff;
                    id = ((b0 << 16) + (b1 << 8) + b2);
                    break;
                case 4:
                    b0 = idByte[0] & 0xff;
                    b1 = idByte[1] & 0xff;
                    b2 = idByte[2] & 0xff;
                    b3 = idByte[3] & 0xff;
                    id = ((b0 << 24) + (b1 << 16) + (b2 << 8) + b3);
                    break;
                default:
                    id = 0;
                    break;
            }
            if (id > 0) {
                BPG.getSingleThreadExecutor().execute(new BpgSaveIdThread(id));
            }

        }
        if (id <= 0) {
            Log.e(BPG.BPG_TAG, "Illegal image resource");
            return null;
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
        private long id = 0;

        public BpgSaveIdThread(long id) {
            this.id = id;
        }

        @Override
        public void run() {
            saveIds(id);
        }
    }
}
