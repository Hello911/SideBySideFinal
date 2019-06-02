package sidebyside3.david.com.sidebyside5.offline;

import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;

import me.relex.circleindicator.CircleIndicator;
import sidebyside3.david.com.sidebyside5.R;
import sidebyside3.david.com.sidebyside5.utils.MyAdapter;

public class helpOutline extends AppCompatActivity implements View.OnClickListener{
    Button skip;
    private static ViewPager mPager;
    private ArrayList<Integer> XPHOTO=new ArrayList<Integer>();
    private static final Integer[] XPHOTOArray={R.drawable.x_best_outline
            ,R.drawable.x_shutter
            ,R.drawable.x_timer};
    private ArrayList<Integer> notesText=new ArrayList<>();
    private static final Integer[] notesTextArray={R.string.bestOutline
            ,R.string.shutter
            ,R.string.timer};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_outline);
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
