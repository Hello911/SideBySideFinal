package sidebyside3.david.com.sidebyside4;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.webkit.PermissionRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;



/**
 * Created by Gongwei (David) Chen on 9/24/2018.
 */

 class Camera extends AppCompatActivity{
    private static final int REQUEST_TAKE_PHOTO=1;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        try {
            dispatchTakePictureIntent();
        } catch (IOException e){

        }
    }


    /**
     *
     * @return File address just created for photo taken
     * @throws IOException
     */
    private File createImageFile() throws IOException{
        String timeStamp=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss").format(new Date());
        String imageFileName="SideBySide4_"+timeStamp+"_";
        File storageDir=new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "Camera");
        File image=File.createTempFile(
                imageFileName
                ,".jpeg"
                ,storageDir
        );
        mCurrentPhotoPath="file:"+image.getAbsolutePath();
        return image;
    }

    /**
     *
     * @throws IOException
     */
    private void dispatchTakePictureIntent() throws IOException{
        Intent takePictureIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //check if there is a camera activity
        if(takePictureIntent.resolveActivity(getPackageManager())!=null){
            File photoFile=null;
            try{
                photoFile=createImageFile();
            }catch(IOException e){
                return;
            }
            if(photoFile!=null){
                Uri photoUri= FileProvider.getUriForFile(this
                        ,BuildConfig.APPLICATION_ID+".provider"
                        ,photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(takePictureIntent,REQUEST_TAKE_PHOTO);
            }
        }
    }

    /**
     * Once photo is taken from Camera Intent, broadcast the newly taken photo
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_TAKE_PHOTO && resultCode==RESULT_OK){
            Uri imageUri=Uri.parse(mCurrentPhotoPath);
            MediaScannerConnection.scanFile(this
                    ,new String[]{imageUri.getPath()}
                    ,null
                    ,new MediaScannerConnection.OnScanCompletedListener(){
                        public void onScanCompleted(String path, Uri uri){

                        }
                    });
        }
    }
}
