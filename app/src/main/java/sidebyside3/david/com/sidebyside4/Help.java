package sidebyside3.david.com.sidebyside4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Help extends AppCompatActivity implements View.OnClickListener{
    int position=2;//there are six buttons to explain for. To keep track of which explanation is visible.
    TextView skip,next;
    ImageView photoCountArrow,folderArrow,settingArrow,cameraArrow, editArrow, compareArrow;
    TextView photoCountNote,folderNote,settingNote,cameraNote,editNote,compareNote;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help);

        skip=(TextView)findViewById(R.id.skip);
        skip.setOnClickListener(this);
        next=(TextView)findViewById(R.id.next);
        next.setOnClickListener(this);

        photoCountArrow=(ImageView)findViewById(R.id.photoCountArrow);
        photoCountNote=(TextView)findViewById(R.id.photoCountNote);

        folderArrow=(ImageView)findViewById(R.id.folderArrow);
        folderNote=(TextView)findViewById(R.id.folderNote);

        settingArrow=(ImageView)findViewById(R.id.settingArrow);
        settingNote=(TextView)findViewById(R.id.settingNote);

        cameraArrow=(ImageView)findViewById(R.id.cameraArrow);
        cameraNote=(TextView)findViewById(R.id.cameraNote);

        editArrow=(ImageView)findViewById(R.id.editArrow);
        editNote=(TextView)findViewById(R.id.editNote);

        compareArrow=(ImageView)findViewById(R.id.compareArrow);
        compareNote=(TextView)findViewById(R.id.compareNote);
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.next:
                if(position==7) {
                    finish();
                }else {
                    switchCase(position);
                }
                position++;
                break;
            case R.id.skip:
                finish();
                break;
        }
    }

    /**
     * Show tutorial steps in order
     * @param pos
     */
    private void switchCase(int pos){
        switch (pos){
            case 1://daily photo count
                //do nothing.The note for daily photo count is already visible;
                break;
            case 2://folder button
                photoCountArrow.setVisibility(View.INVISIBLE);
                photoCountNote.setVisibility(View.INVISIBLE);
                folderArrow.setVisibility(View.VISIBLE);
                folderNote.setVisibility(View.VISIBLE);
                break;
            case 3://setting
                folderArrow.setVisibility(View.INVISIBLE);
                folderNote.setVisibility(View.INVISIBLE);
                settingArrow.setVisibility(View.VISIBLE);
                settingNote.setVisibility(View.VISIBLE);
                break;
            case 4://camera
                settingArrow.setVisibility(View.INVISIBLE);
                settingNote.setVisibility(View.INVISIBLE);
                cameraArrow.setVisibility(View.VISIBLE);
                cameraNote.setVisibility(View.VISIBLE);
                break;
            case 5://edit
                cameraArrow.setVisibility(View.INVISIBLE);
                cameraNote.setVisibility(View.INVISIBLE);
                editArrow.setVisibility(View.VISIBLE);
                editNote.setVisibility(View.VISIBLE);
                break;
            case 6://compare
                editArrow.setVisibility(View.INVISIBLE);
                editNote.setVisibility(View.INVISIBLE);
                compareArrow.setVisibility(View.VISIBLE);
                compareNote.setVisibility(View.VISIBLE);
        }
    }
}
