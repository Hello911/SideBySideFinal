package sidebyside3.david.com.sidebyside4;

import android.content.Intent;
import android.content.SharedPreferences;
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
    TextView folder,dailyPhotos;
    File mCurrentPhotoFile;
    Uri imageUri;
    SharedPreferences pref;
    SharedPreferences.Editor editor;

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

        dailyPhotos=(TextView)findViewById(R.id.dailyPhotos);

        setting=(ImageView)findViewById(R.id.setting);
        setting.setOnClickListener(this);

        compare=(ImageView)findViewById(R.id.compare);
        compare.setOnClickListener(this);

        pref=getApplicationContext().getSharedPreferences("photoNum",MODE_PRIVATE);
        editor=pref.edit();

    }

    @Override
    public void onResume(){
        super.onResume();
        //refresh total photo count after returning from camera Intent
        File dir=new File("/sdcard/DCIM/raspberry");
        File[] files=dir.listFiles();
        int numOfFiles=files.length;
        folder.setText(numOfFiles+" photos in Folder");
        //to refresh daily photo taken after returning from camera Intent
        int num=pref.getInt("photos", 0);


        Calendar cal=Calendar.getInstance();
        int today=cal.get(Calendar.DAY_OF_MONTH);
        int dayLastSaved=pref.getInt("day",0);
        if(today==dayLastSaved){

        }else{
            num=0;
        }

        switch(num){
            case 0:
                dailyPhotos.setText("No photo taken today");
                break;
            case 1:
                dailyPhotos.setText("1 photo taken today");
                break;
            default:
                dailyPhotos.setText(num+" photos taken today");
        }
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
            Calendar cal=Calendar.getInstance();
            int today=cal.get(Calendar.DAY_OF_MONTH);
            int dayLastSaved=pref.getInt("day",0);
            //every time a photo is saved, increment num to keep track daily photo number
            int num=pref.getInt("photos",0);
            if(dayLastSaved==today){
                num++;
            }else{
                num=1;
            }
            editor.putInt("photos", num);
            //every time a photo was saved, put today's date into "day"
            editor.putInt("day",today);//
            editor.apply();

            MediaScannerConnection.scanFile(this,
                    new String[]{mCurrentPhotoFile.getPath()},
                    null,
                    null);
        }//requestCode=REQUEST_TAKE_PHOTO
    }
}
