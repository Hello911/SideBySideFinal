package sidebyside3.david.com.sidebyside4;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by Gongwei (David) Chen on 9/12/2018.
 */

public class MyGridAdapter extends BaseAdapter {
    LayoutInflater inflater;
    List<GridViewItem> items;

    public MyGridAdapter(Context context, List<GridViewItem> items){
        this.items=items;
        inflater=(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @Override
    public int getCount(){
        return items.size();
    }
    @Override
    public Object getItem(int position){
        return items.get(position);
    }
    @Override
    public long getItemId(int position){
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        if(convertView==null){
            convertView=inflater.inflate(R.layout.grid_item,null);
        }
        TextView text=(TextView)convertView.findViewById(R.id.textView);
        text.setText(getDate(items.get(position)));//Photo date

        ImageView imageView=(ImageView)convertView.findViewById(R.id.imageView);
        Bitmap image=items.get(position).getImage();//thumbnail

        if(image!=null){
            imageView.setImageBitmap(image);
        }
        return convertView;
    }
    private String getDate(GridViewItem g){
        File f=new File(g.getPath());
        if(f.exists()){
            Date d=new Date();
            DateFormat formatter=new SimpleDateFormat("MM/dd/yyyy");
            return formatter.format(d);
        }
        return "";
    }
}
