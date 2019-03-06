package sidebyside3.david.com.sidebyside5.offline;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import sidebyside3.david.com.sidebyside5.BuildConfig;
import sidebyside3.david.com.sidebyside5.R;
import sidebyside3.david.com.sidebyside5.online.Login;
import sidebyside3.david.com.sidebyside5.online.Space;


public class Main extends AppCompatActivity implements View.OnClickListener{
    static final int  CAMERA=1;
    ImageView camera,edit,setting,compare,help;
    TextView dailyPhotos;
    Button folder,login;
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
        help=(ImageView)findViewById(R.id.help);
        help.setOnClickListener(this);

        camera=(ImageView)findViewById(R.id.camera);
        camera.setOnClickListener(this);

        edit=(ImageView)findViewById(R.id.edit);
        edit.setOnClickListener(this);

        folder=(Button)findViewById(R.id.folder);
        folder.setOnClickListener(this);

        dailyPhotos=(TextView)findViewById(R.id.dailyPhotos);

        setting=(ImageView)findViewById(R.id.setting);
        setting.setOnClickListener(this);

        compare=(ImageView)findViewById(R.id.compare);
        compare.setOnClickListener(this);

        login=(Button)findViewById(R.id.login);
        login.setOnClickListener(this);

        pref=getApplicationContext().getSharedPreferences("photoNum",MODE_PRIVATE);
        editor=pref.edit();

        //open camera when opened from notification
        if("openCamera".equals(getIntent().getAction())){
            if(ContextCompat.checkSelfPermission(this
                    , Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
                Toast.makeText(this,"Enable permission in Setting->App",Toast.LENGTH_SHORT).show();
            }//check if storage permission is granted
            else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
            }
            else {
                try {
                    dispatchTakePictureIntent();
                } catch (IOException e){

                }
            }
        }
    }//onCreate()

    @Override
    public void onResume(){
        super.onResume();
        //refresh total photo count after returning from camera Intent
        File dir=new File(Environment.getExternalStorageDirectory()+ getString(R.string.file));
        File[] files=dir.listFiles();
        int numOfFiles;
        if(files!=null){
            numOfFiles=files.length;
        }else{
            numOfFiles=0;
        }
        folder.setText(numOfFiles+" photos in Folder");
        //to refresh daily photo taken after returning from camera Intent
        int num=pref.getInt("photos", 0);


        Calendar cal=Calendar.getInstance();
        int today=cal.get(Calendar.DAY_OF_MONTH);
        int dayLastSaved=pref.getInt("day",0);
        if(today==dayLastSaved){

        }else{
            num=0;
            editor.putInt("photos", num);
            editor.apply();
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1:{
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    try {
                        dispatchTakePictureIntent();
                    } catch (IOException e){

                    }
                }else{

                }
            }
            case 2:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {

                }
        }
    }

    @Override
    public void onClick(View v){
        switch(v.getId()) {
            case R.id.camera:
                //Check if camera permission is granted
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},1);
                    Toast.makeText(this,"Enable permission in Setting->App",Toast.LENGTH_SHORT).show();
                }//check if storage permission is granted
                else if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 2);
                }
                else {
                    try {
                        dispatchTakePictureIntent();
                    } catch (IOException e){

                    }
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
                break;
            case R.id.help:
                Intent showNote=new Intent(this, Help.class);
                startActivity(showNote);
                break;
            case R.id.login:
                Intent openLogin=new Intent(this,Login.class);
                startActivity(openLogin);
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
                                , BuildConfig.APPLICATION_ID+".provider"
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
            num++;
            editor.putInt("photos", num);
            //every time a photo was saved, put today's date into "day" for dayLastSaved
            editor.putInt("day",today);//
            editor.apply();

            MediaScannerConnection.scanFile(this,
                    new String[]{mCurrentPhotoFile.getPath()},
                    null,
                    null);
        }//requestCode=REQUEST_TAKE_PHOTO
    }
}
