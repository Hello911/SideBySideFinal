package sidebyside3.david.com.sidebyside4;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Main extends AppCompatActivity implements View.OnClickListener{
    static final int  CAMERA=1;
    ImageView camera,edit,setting,compare;
    TextView folder;
    File mCurrentPhotoFile;
    Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState != null){
           if(imageUri==null && savedInstanceState.getString("imageUri")!=null){
               imageUri=Uri.parse(savedInstanceState.getString("imageUri"));
           }
        }
        setContentView(R.layout.main);

        camera=(ImageView)findViewById(R.id.camera);
        camera.setOnClickListener(this);

        edit=(ImageView)findViewById(R.id.edit);
        edit.setOnClickListener(this);

        folder=(TextView)findViewById(R.id.folder);
        folder.setOnClickListener(this);

        setting=(ImageView)findViewById(R.id.setting);
        setting.setOnClickListener(this);

        compare=(ImageView)findViewById(R.id.compare);
        compare.setOnClickListener(this);
    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.camera:
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e){

                }
                break;
            case R.id.folder:
                Intent openFolder=new Intent(this,Folder.class);
                Toast.makeText(this, "Please wait...", Toast.LENGTH_SHORT).show();
                startActivity(openFolder);
                break;
            case R.id.edit:
                Intent openEdit=new Intent(this, Edit.class);
                startActivity(openEdit);
                break;
            case R.id.setting:
                Intent openSetting=new Intent (this, Setting.class);
                startActivity(openSetting);
                break;
            case R.id.compare:
                Intent openCompare=new Intent(this,Compare.class);
                startActivity(openCompare);
        }
    }

    /**
     * Launch camera
     * @throws IOException
     */
    private void dispatchTakePictureIntent() throws IOException{
        String dateString=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss").format(Calendar.getInstance().getTime());
        mCurrentPhotoFile=new File(getPublicDir(),"SideBySide4_"+dateString+".png");
        Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        imageUri=FileProvider.getUriForFile(this
                                ,BuildConfig.APPLICATION_ID+".provider"
                                ,mCurrentPhotoFile);
        intent.putExtra(MediaStore.EXTRA_OUTPUT,imageUri);
        this.startActivityForResult(intent,CAMERA);
    }
    /**
     *Return raspberry folder if no such folder create one
     *
     */
        public File getPublicDir() {
            // Get the directory for the user's public pictures directory.
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "raspberry");
            if (!file.mkdirs()) {
                Log.e("PUBLIC DIRECTORY", "Directory not created");
            }
            return file;
        }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAMERA && resultCode==RESULT_OK){
            MediaScannerConnection.scanFile(this,
                    new String[]{mCurrentPhotoFile.getPath()},
                    null,
                    null);
        }//requestCode=REQUEST_TAKE_PHOTO
    }
}
