package sidebyside3.david.com.sidebyside5.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.util.Comparator;

import sidebyside3.david.com.sidebyside5.offline.GridViewItem;

/**
 * Created by Gongwei (David) Chen on 11/27/2018.
 */

public class CustomComparator implements Comparator<GridViewItem> {
    private static Context mContext;
    public CustomComparator(Context context) {
        mContext=context;
    }
    @Override
    public int compare(GridViewItem item1, GridViewItem item2) {
        String[] projection = new String[]{MediaStore.Images.Media.DATE_TAKEN};
        Long millis1 = 0l;
        Long millis2 = 0l;

        Uri photoUri1 = getImageContentUri(mContext, new File(item1.getPath()));
        Cursor cur1 = mContext.getContentResolver().query(photoUri1, projection, null, null, null);
        if (cur1.moveToFirst()) {
            int dateColumn = cur1.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            millis1 = cur1.getLong(dateColumn);
        }

        Uri photoUri2 = getImageContentUri(mContext, new File(item2.getPath()));
        Cursor cur2 = mContext.getContentResolver().query(photoUri2, projection, null, null, null);
        if (cur2.moveToFirst()) {
            int dateColumn = cur2.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            millis2 = cur2.getLong(dateColumn);
        }
        int value=millis1>millis2?-1:millis1<millis2?1:0;
        Log.i("Comparator", Integer.toString(value));
        return value;
    }
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
}
