package sidebyside3.david.com.sidebyside4;


import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by irpei on 9/7/2018.
 */

public class MyGridAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<GridViewItem> items;
    Context mContext;


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
            if(items.get(position).isSelected())
            {
                imageView.setColorFilter(Color.argb(150,200,200,200));
            }
            else
            {
                imageView.setColorFilter(null);
            }
        }

        return convertView;
    }

    @TargetApi(25)
    private String getDate(GridViewItem g, Context context) {
        String dateString="";
        Uri uri=getImageContentUri(context,new File(g.getPath()));
        try{
            InputStream in=context.getContentResolver().openInputStream(uri);
            ExifInterface exifInterface=new ExifInterface(in);
            dateString=exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
        }catch(IOException e){
            e.printStackTrace();
        }
        return dateString;
    }

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
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

}