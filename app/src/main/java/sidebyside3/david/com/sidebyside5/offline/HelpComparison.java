package sidebyside3.david.com.sidebyside5.offline;

import android.content.res.Configuration;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import sidebyside3.david.com.sidebyside5.R;
import sidebyside3.david.com.sidebyside5.utils.MyAdapter;

public class HelpComparison extends AppCompatActivity implements View.OnClickListener{
    Button skip;
    private static ViewPager mPager;
    private ArrayList<Integer> XPHOTO=new ArrayList<Integer>();
    private static final Integer[] XPHOTOArray={R.drawable.x_select1
            ,R.drawable.x_select2
            ,R.drawable.x_zoom_enabled};
    //tutorial snapshots when landscape version
    private static final Integer[] XPHOTOArrayLand={R.drawable.x_download_share
            ,R.drawable.x_select2_land
            ,R.drawable.x_zoom_land};

    private ArrayList<Integer> notesText=new ArrayList<Integer>();
    private static final Integer[] notesTextArray={R.string.select1Note
            ,R.string.select2Note
            ,R.string.zoomEnabledNote};
    //tutorial explanations for landscape version
    private static final Integer[] notesTextArrayLand={R.string.downloadShareNote
            ,R.string.select2LandNote
            ,R.string.zoomLandNote};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_comparison);
        init();
    }
    private void init(){
        if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT){
            for(int i=0;i<XPHOTOArray.length;i++){
                XPHOTO.add(XPHOTOArray[i]);
            }
            for(int i=0;i<notesTextArray.length;i++){
                notesText.add(notesTextArray[i]);
            }
        }else{
            for(int i=0;i<XPHOTOArrayLand.length;i++){
                XPHOTO.add(XPHOTOArrayLand[i]);
            }
            for(int i=0;i<notesTextArrayLand.length;i++){
                notesText.add(notesTextArrayLand[i]);
            }
        }

        mPager=(ViewPager)findViewById(R.id.pager);
        mPager.setAdapter(new MyAdapter(this,XPHOTO,notesText));
        CircleIndicator indicator=(CircleIndicator)findViewById(R.id.indicator);
        indicator.setViewPager(mPager);
        skip=(Button)findViewById(R.id.skip);
        skip.setOnClickListener(this);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.skip:
                finish();
        }
    }
}
