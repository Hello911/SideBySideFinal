package sidebyside3.david.com.sidebyside4;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Gongwei (David) Chen on 11/14/2018.
 */

public class MyAsyncTask extends AsyncTask<Void,Void,String> {

        String path;
        List<GridViewItem> items;

        public AsyncResponse delete=null;

        public interface AsyncResponse{
            void processFinish(String output);
        }

        public AsyncResponse delegate=null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String result) {
            delegate.processFinish(result);
        }


        @Override
        protected void doInBackground(Void...params) {
            items = new ArrayList<GridViewItem>();

            // List all the items within the folder.
            File[] files = new File(path).listFiles();

            for (int i = 0; i < files.length; i++) {
                final File file = files[i];
                // Add the directories containing images or sub-directories
                if (file.isDirectory()
                        && file.listFiles(new Folder.ImageFileFilter()).length > 0) {
                    items.add(new GridViewItem(file.getAbsolutePath(), true, null, false, i));
                }
                // Add the images
                else {
                    Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                            50,
                            50);

                    items.add(new GridViewItem(file.getAbsolutePath(), false, image, false, i));
                }
            }
            return null;
        }
}
