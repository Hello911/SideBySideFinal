package sidebyside3.david.com.sidebyside5.offline;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import sidebyside3.david.com.sidebyside5.R;
import sidebyside3.david.com.sidebyside5.utils.MyAdapter;

public class Help extends AppCompatActivity implements View.OnClickListener{
    private static ViewPager mPager;
    private ArrayList<Integer> XPHOTO=new ArrayList<Integer>();
    private static final Integer[] XPHOTOArray={R.drawable.x_daily_photo
            ,R.drawable.x_folder
            ,R.drawable.x_setting
            ,R.drawable.x_camera
            ,R.drawable.x_edit
            ,R.drawable.x_compare};
    private ArrayList<Integer> notesText=new ArrayList<Integer>();
    private static final Integer[] notesTextArray={R.string.photoCountNote
            ,R.string.folderNote
            ,R.string.settingNote
            ,R.string.cameraNote
            ,R.string.editNote
            ,R.string.compareNote};
    Button skip;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);
        init();
    }
    private void init(){
        for(int i=0;i<XPHOTOArray.length;i++){
            XPHOTO.add(XPHOTOArray[i]);
        }
        for(int i=0;i<notesTextArray.length;i++){
            notesText.add(notesTextArray[i]);
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
