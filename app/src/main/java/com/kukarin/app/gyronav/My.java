package com.kukarin.app.gyronav;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.android.gms.internal.zzir.runOnUiThread;

/**
 * Created by alexk on 2/8/2016.
 */
public class My {
    private static final String TAG = "My.class";

    /**
     * Extracts file name from received Uri
     * @param context
     * @param uri
     * @return file name
     */
    public static String getPath(Context context, Uri uri) {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            Cursor cursor;
            try {
                cursor = context.getContentResolver().query(uri, null, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME);
                if (cursor.moveToFirst())
                    return cursor.getString(column_index);
            }
            catch (Exception e) {}
        }
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            List<String> arr = uri.getPathSegments();
            return arr.get(arr.size()-1);
        }
        return null;
    }

    /**
     * Show a message to the user and in the Log
     *
     * @param msg
     */
    public static void msg(String TAG, String msg) {
        final String m = (msg==null?"NULL":msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(Busy.getInstance().getContext(), m, Toast.LENGTH_LONG).show();
                Log.d("UI", m);
            }
        });

    }

    public static String Date2String(Date time) {
        DateFormat df = new SimpleDateFormat(Settings.DATETIMEFORMAT);
        return df.format(time);
    }
    public static Date String2Date(String dateString) {
        DateFormat df = new SimpleDateFormat(Settings.DATETIMEFORMAT);
        Date convertedDate = new Date();
        try {
            convertedDate = df.parse(dateString);
        } catch (Exception e) {
            e.printStackTrace();
            My.msg(TAG, e.getMessage());
        }
        return convertedDate;
    }

    public static Object copyObject(Object objSource) {
        Object objDest=null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(objSource);
            oos.flush();
            oos.close();
            bos.close();
            byte[] byteData = bos.toByteArray();
            ByteArrayInputStream bais = new ByteArrayInputStream(byteData);
            try {
                objDest = new ObjectInputStream(bais).readObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return objDest;

    }

}
