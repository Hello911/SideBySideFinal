package sidebyside3.david.com.sidebyside4;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


/**
 * Created by irpei on 9/7/2018.
 */

public class MyGridAdapter extends BaseAdapter {

    LayoutInflater inflater;
    List<GridViewItem> items;


    public MyGridAdapter(Context context, List<GridViewItem> items) {
        this.items = items;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    private String getDate(GridViewItem g) {
        File f = new File(g.getPath());
        if(f.exists())
        {
            Date d = new Date(f.lastModified());
            java.text.DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
            return formatter.format(d);
        }
        return "";
    }

}