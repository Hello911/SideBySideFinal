package sidebyside3.david.com.sidebyside5.offline;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractorMOG2;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import sidebyside3.david.com.sidebyside5.R;

public class OutlineCamera extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2,View.OnClickListener{
    static{
        if(!OpenCVLoader.initDebug()){
            Log.i("OPEN_CV","failed");
        }else{
            Log.i("OPEN_CV","success!");
        }
    }
    JavaCameraView openCVcamera;
    //async initialization
    BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch(status){
                case BaseLoaderCallback.SUCCESS:
                    openCVcamera.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };
    Button shutter,timerButton;
    ImageButton switchCam;
    private int mCameraId=1;//start with 1/back
    private Bitmap previewBitmap;
    TextView timeLeft;
    ImageView helpOutline;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.outline_camera);

        openCVcamera=(JavaCameraView)findViewById(R.id.camera);
        openCVcamera.setVisibility(SurfaceView.VISIBLE);
        openCVcamera.setCvCameraViewListener(this);


        shutter=(Button)findViewById(R.id.shutter);
        shutter.setOnClickListener(this);

        timerButton=(Button)findViewById(R.id.timerButton);
        timerButton.setOnClickListener(this);

        timeLeft=(TextView)findViewById(R.id.timeLeft);

        helpOutline=(ImageView)findViewById(R.id.helpOutline);
        helpOutline.setOnClickListener(this);

        switchCam=(ImageButton)findViewById(R.id.switchCam);
        switchCam.setOnClickListener(this);
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(openCVcamera!=null){
            openCVcamera.disableView();
        }
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(openCVcamera!=null){
            openCVcamera.disableView();
        }
    }
    @Override
    protected void onResume(){
        super.onResume();
        if(OpenCVLoader.initDebug()){
            //if openCV is loaded, open camera;
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            //if openCV is not loaded try again
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11,this,mLoaderCallback);
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    /**
     * Do processing after frame grabbing before rendering on screen
     * @param inputFrame
     * @return
     */
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        Imgproc.cvtColor(mRgba, mRgb, Imgproc.COLOR_RGBA2RGB); //the apply function will throw the above error if you don't feed it an RGB image
        sub.apply(mRgb, mFGMask, -1); //apply() exports a gray image by definition
        Imgproc.cvtColor(mFGMask, mRgba, Imgproc.COLOR_GRAY2RGBA);

        Core.transpose(mRgba,trans);//var trans is created because for .transpose() the arguments have to be different unlike .flip()
        if(mCameraId==1) {//flip about the x-axis if back camera is used
            Core.flip(trans, trans, 0);
        }
        Core.flip(trans, trans, 1);
        Imgproc.resize(trans, trans, mRgba.size());

        return trans;
    }
    private BackgroundSubtractorMOG2 sub = new BackgroundSubtractorMOG2(10, 25, false);
    private Mat mRgb=new Mat();
    private Mat mFGMask=new Mat();
    private Mat mRgba=new Mat();
    private Mat trans=new Mat();

    /**
     * Once permission is granted, do these things corresponding to each requestCode
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1://for shutter's save button
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try{
                        Calendar calendar=Calendar.getInstance();
                        SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                        String DateString=sdformat.format(calendar.getTime());

                        File file=new File(getPublicDir(),"mySnapshot_"+DateString+".png");
                        FileOutputStream fos=new FileOutputStream(file);
                        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                        fos.close();
                        MediaScannerConnection.scanFile(OutlineCamera.this,
                                new String[]{file.getPath()},
                                null,
                                null);
                        Toast.makeText(OutlineCamera.this,"Successfully saved: "+file, Toast.LENGTH_SHORT).show();
                    }catch(IOException e){
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    /**
     * Button function to take photo and timer
     * @param view
     */
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.shutter:
                ImageView outlinePreview=new ImageView(this);
//                previewBitmap=getBitmapOfView(shutter);
                previewBitmap=Bitmap.createBitmap(trans.cols(),trans.rows(),Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(trans,previewBitmap);
                outlinePreview.setImageBitmap(previewBitmap);
                AlertDialog.Builder previewDialog=new AlertDialog.Builder(this)
                        .setView(outlinePreview)
                        .setTitle("Share or save this photo")
                        .setNegativeButton("share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(OutlineCamera.this,"Loading....", Toast.LENGTH_SHORT).show();
                                Calendar calendar=Calendar.getInstance();
                                SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                String DateString=sdformat.format(calendar.getTime());
                                //get Photo uri
                                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                                previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                                String path= MediaStore.Images.Media.insertImage(OutlineCamera.this.getContentResolver(),
                                        previewBitmap,
                                        "mySnapshot_"+DateString+".png",
                                        null);
                                Uri uri=Uri.parse(path);
                                //start sharing Intent
                                Intent intent=new Intent(Intent.ACTION_SEND)
                                        .setType("image/*")
                                        .putExtra(Intent.EXTRA_STREAM,uri);
                                try{
                                    startActivity(Intent.createChooser(intent, "Share collage..."));
                                }catch(ActivityNotFoundException e){
                                    e.printStackTrace();
                                }
                            }//Dialog onClick()
                        })
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (ContextCompat.checkSelfPermission(OutlineCamera.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
                                {
                                    ActivityCompat.requestPermissions(OutlineCamera.this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                                }else{
                                    try{
                                        Calendar calendar=Calendar.getInstance();
                                        SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                        String DateString=sdformat.format(calendar.getTime());

                                        File file=new File(getPublicDir(),"mySnapshot_"+DateString+".png");
                                        FileOutputStream fos=new FileOutputStream(file);
                                        previewBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                                        fos.close();
                                        MediaScannerConnection.scanFile(OutlineCamera.this,
                                                new String[]{file.getPath()},
                                                null,
                                                null);
                                        Toast.makeText(OutlineCamera.this,"Successfully saved: "+file, Toast.LENGTH_SHORT).show();
                                    }catch(IOException e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });//AlertDialog.Builder
                previewDialog.create().show();
                break;
            case R.id.timerButton:
                //make it invisible once pressed until timer expires to avoid repressing
                timerButton.setVisibility(View.INVISIBLE);
                final long startTime=System.currentTimeMillis();
                Handler handler=new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        shutter.performClick();
                    }
                },5000);
                final Timer timer=new Timer();
                TimerTask myTask=new TimerTask(){
                    @Override
                    public void run() {
                        //check and show remaining time on camera timer
                        long elaspsedTime=System.currentTimeMillis()-startTime;
                        int remainingTime=(int)(6000-elaspsedTime)/1000;//in sec, not milisec
                        final String remainingTimeInSec=Integer.toString(remainingTime);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(remainingTimeInSec.equals("0")){
                                    timer.cancel();
                                    //when 5sec is up, make timer button visible again
                                    timerButton.setVisibility(View.VISIBLE);
                                    timeLeft.setText("5");
                                }else {
                                    timeLeft.setText(remainingTimeInSec);
                                }
                            }
                        });
                    }
                };
                timer.schedule(myTask,0,1000);
                break;
            case R.id.helpOutline:
                Intent openOutlineCameraTutorial=new Intent(this, helpOutline.class);
                startActivity(openOutlineCameraTutorial);
                break;
            case R.id.switchCam:
                mCameraId=mCameraId^1;//switch between 1/back and 0/front
                openCVcamera.disableView();
                openCVcamera.setCameraIndex(mCameraId);
                openCVcamera.enableView();
                break;
        }
    }//onClick

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

}
