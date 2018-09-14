package sidebyside3.david.com.sidebyside4;

import android.content.Intent;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

public class Main extends AppCompatActivity implements View.OnClickListener{
    static final int  CAMERA=1;
    ImageView camera,edit;
    TextView folder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        camera=(ImageView)findViewById(R.id.camera);
        camera.setOnClickListener(this);

        edit=(ImageView)findViewById(R.id.edit);
        edit.setOnClickListener(this);

        folder=(TextView)findViewById(R.id.folder);
        folder.setOnClickListener(this);

    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.camera:
                Intent launchCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (launchCamera.resolveActivity(getPackageManager())!=null) {
                    startActivityForResult(launchCamera, CAMERA);
                }
                break;
            case R.id.folder:
                Intent openFolder=new Intent(this,Folder.class);
                startActivity(openFolder);
                break;
            case R.id.edit:
                Intent openEdit=new Intent(this, Edit.class);
                startActivity(openEdit);
                break;
        }
    }
}
