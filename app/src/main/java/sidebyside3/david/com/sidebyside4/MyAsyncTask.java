package sidebyside3.david.com.sidebyside4;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.GridView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyAsyncTask extends AsyncTask<String,Void,List<GridViewItem>> implements Runnable{
    private Context mContext;
    private Activity activity;
    List<GridViewItem> items;
    @Override
    public void run() {
        Collections.sort(items,new CustomComparator(mContext));
        Folder.adp = new MyGridAdapter(mContext, items);
        Folder.gridView.setAdapter(Folder.adp);
    }



    public interface AsyncResponse{
        void processFinish(List<GridViewItem> list);
    }

    public AsyncResponse delegate=null;
    public MyAsyncTask(Context context,AsyncResponse delegate){
        this.delegate=delegate;
        mContext=context;
        activity=(Activity)context;
    }

    @Override
    protected List<GridViewItem> doInBackground(String... path) {
        items = new ArrayList<GridViewItem>();

        // List all the items within the folder.
        File[] files = new File(path[0]).listFiles();
        if(files != null) {
            for (int i = 0; i < files.length; i++) {
                final File file = files[i];

                Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                        50,
                        50);
                items.add(i, new GridViewItem(file.getAbsolutePath(), false, image, false, i));

                activity.runOnUiThread(this);//this means the method implementing Runnable interface will be executed

            }
        }

        return items;
    }

    @Override
    protected void onPostExecute(List<GridViewItem> result){
        delegate.processFinish(result);
    }
    private class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }
    /**
     * Checks the file to see if it has a compatible extension.
     */
    private boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
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
