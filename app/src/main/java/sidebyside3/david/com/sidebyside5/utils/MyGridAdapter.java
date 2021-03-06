package sidebyside3.david.com.sidebyside5.utils;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import sidebyside3.david.com.sidebyside5.offline.GridViewItem;
import sidebyside3.david.com.sidebyside5.R;


/**
 * Created by irpei on 9/7/2018.
 */

public class MyGridAdapter extends BaseAdapter {

    private LayoutInflater inflater;
    private List<GridViewItem> items;
    private Context mContext;

    public MyGridAdapter(Context context, List<GridViewItem> items) {
        mContext=context;
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public void add(GridViewItem i){
        items.add(i);
    }
    @Override
    public int getCount() {
        return items.size();
    }


    @Override
    public Object getItem(int position) {
        return items.get(position);
    }


    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.grid_item, null);
        }


        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageView);
        Bitmap image = items.get(position).getImage();
        TextView date=(TextView) convertView.findViewById(R.id.date);

        date.setText(getDate(items.get(position),mContext));


        if (image != null){
            imageView.setImageBitmap(image);
        }

        return convertView;
    }

    /**
     * For displaying the date of each photo in the Folder
     * @param g
     * @param context
     * @return
     */
    private String getDate(GridViewItem g, Context context) {
        Long longDate=null;
        String[] projection=new String[] {MediaStore.Images.Media.DATE_TAKEN};
        Uri uri=getImageContentUri(context,new File(g.getPath()));
        Cursor cur=context.getContentResolver().query(
                uri
                ,projection
                ,null
                ,null
                ,null);
        if(cur.moveToFirst()){//when cursor is empty
            int dateColumn=cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            longDate = cur.getLong(dateColumn);
        }
        Date d=new Date(longDate);
        java.text.DateFormat formatter=new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

    /**
     * This function is needed to get content:// scheme Uri to retrieve exif data
     * @param context
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                Uri ret = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                return ret;
            } else {
                return null;
            }
        }
    }
}
