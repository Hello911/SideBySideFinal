package sidebyside3.david.com.sidebyside5.utils;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import sidebyside3.david.com.sidebyside5.R;

/**
 * Created by Gongwei (David) Chen on 2/26/2019.
 */

public class MyAdapter extends PagerAdapter {
    private ArrayList<Integer> images;
    private ArrayList<Integer> notesText;
    private LayoutInflater inflater;
    private Context context;
    public MyAdapter(Context context,ArrayList<Integer> images, ArrayList<Integer> notesText){
        this.context=context;
        this.images=images;
        this.notesText=notesText;
        inflater=LayoutInflater.from(context);
    }

    @Override
    public void destroyItem(ViewGroup container, int pos, Object obj){
        container.removeView((View) obj);
    }
    /**
     * returns the  number of slides
     * @return
     */
    @Override
    public int getCount() {
        return images.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals(object);
    }

    @Override
    public Object instantiateItem(ViewGroup view,int pos){
        View slide=inflater.inflate(R.layout.slide,view,false);
        ImageView pic=(ImageView)slide.findViewById(R.id.image);
        TextView notes=(TextView)slide.findViewById(R.id.notes);
        pic.setImageResource(images.get(pos));
        notes.setText(notesText.get(pos));
        view.addView(slide,0);// the inflated slide is added to ViewPager
        return slide;
    }
}

