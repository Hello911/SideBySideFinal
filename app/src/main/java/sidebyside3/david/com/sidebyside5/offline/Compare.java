package sidebyside3.david.com.sidebyside5.offline;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Target;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import in.goodiebag.carouselpicker.CarouselPicker;
import sidebyside3.david.com.sidebyside5.R;

/**
 * Created by Gongwei (David) Chen on 6/8/2018.
 */

public class Compare extends AppCompatActivity implements View.OnClickListener, View.OnTouchListener{
    Uri uri1;
    Uri uri2;
    ImageView photo1;
    ImageView photo2;
    TextView photo1text;
    TextView photo2text;
    ImageView selectPhoto1;
    ImageView selectPhoto2;
    ImageView saveCollage;
    ImageView shareCollage;
    RelativeLayout viewGroup;
    RelativeLayout viewGroupLandscape;
    File file;//file path of the collage last taken
    Bitmap viewBitmap;
    private final int PICK_PHOTO1 = 1;
    private final int PICK_PHOTO2 = 2;

    CarouselPicker carouselPicker;
    CarouselPicker carouselPickerLandscape;
    CarouselPicker data1;
    CarouselPicker data2;

    //DRAGGING & ZOOMING
    private static final int NONE=0;
    private static final int DRAG=1;
    private static final int ZOOM=2;

    //set of touch parameters for photo1
    private int mode1=NONE;
    private Matrix matrix1=new Matrix();
    private Matrix savedMatrix1=new Matrix();
    private PointF start1=new PointF();//PointF holds 2 coordinates
    private PointF mid1=new PointF();
    private Bitmap bmap1;
    private float oldDist1=1f;

    //set of touch parameters for photo2
    private int mode2=NONE;
    private Matrix matrix2=new Matrix();
    private Matrix savedMatrix2=new Matrix();
    private PointF start2=new PointF();//PointF holds 2 coordinates
    private PointF mid2=new PointF();
    private Bitmap bmap2;
    private float oldDist2=1f;

    String[] dataArray1;
    String[] dataArray2;

    Button enableZooming1;
    Button enableZooming2;
    Boolean isEnabled1=false;
    Boolean isEnabled2=false;
    //landscape equivalent of enableZooming but is instead imageView
    ImageView enableZoomingLandscape1;
    ImageView enableZoomingLandscape2;

    ImageView help;
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.compare);

        viewGroup=(RelativeLayout)findViewById(R.id.viewGroup);//viewGroup of the compare screen
        viewGroupLandscape=(RelativeLayout)findViewById(R.id.mainView);
        photo1 = (ImageView) findViewById(R.id.photo1);
        photo2 = (ImageView) findViewById(R.id.photo2);

        photo1.setOnClickListener(this);
        photo2.setOnClickListener(this);

        photo1text=(TextView)findViewById(R.id.photo1text);
        photo2text=(TextView)findViewById(R.id.photo2text);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);

        saveCollage=(ImageView) findViewById(R.id.save_collage);
        saveCollage.setOnClickListener(this);

        shareCollage=(ImageView)findViewById(R.id.share_collage);
        shareCollage.setOnClickListener(this);

        selectPhoto1=(ImageView)findViewById(R.id.selectPhoto1);
        selectPhoto1.setOnClickListener(this);
        selectPhoto2=(ImageView)findViewById(R.id.selectPhoto2);
        selectPhoto2.setOnClickListener(this);

        //for COMPARISON portrait mode
        carouselPicker=(CarouselPicker)findViewById(R.id.carousel);
        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, getList(20), 0);
        textAdapter.setTextColor(Color.MAGENTA);
        carouselPicker.setAdapter(textAdapter);

        //Everything is the same, but font size is 30 instead of 20.
        //for COMPARISON landscape mode
        carouselPickerLandscape=(CarouselPicker)findViewById(R.id.carouselLandscape);
        CarouselPicker.CarouselViewAdapter textAdapterLandscape = new CarouselPicker.CarouselViewAdapter(this, getList(30), 0);
        textAdapterLandscape.setTextColor(Color.MAGENTA);
        carouselPickerLandscape.setAdapter(textAdapterLandscape);

        //data of first photo
        data1=(CarouselPicker)findViewById(R.id.data1);
        CarouselPicker.CarouselViewAdapter dataAdapter1=new CarouselPicker.CarouselViewAdapter(this,getList(12),0);
        dataAdapter1.setTextColor(Color.MAGENTA);
        data1.setAdapter(dataAdapter1);

        //data of second photo
        data2=(CarouselPicker)findViewById(R.id.data2);
        CarouselPicker.CarouselViewAdapter dataAdapter2=new CarouselPicker.CarouselViewAdapter(this,getList(12),0);
        dataAdapter2.setTextColor(Color.MAGENTA);
        data2.setAdapter(dataAdapter2);

        /**retrieve two photos when Compare is opened from Folder
         *
         */
        Intent retrievePhotos=getIntent();
        String photo1Path=retrievePhotos.getStringExtra("photo1");
        String photo2Path=retrievePhotos.getStringExtra("photo2");
        Bitmap bitmap1= BitmapFactory.decodeFile(photo1Path);
        Bitmap bitmap2=BitmapFactory.decodeFile(photo2Path);
        photo1.setImageBitmap(bitmap1);
        photo2.setImageBitmap(bitmap2);

        if(bitmap1!=null){//check so Comparison don't crash
            photo1text.setText("");//after a photo is pick, make "Select Photo" disappear
            photo2text.setText("");
            //reading photo1 Exif data for photo1Path
            uri1= getImageContentUri(this, new File(photo1Path));
            String dataString1=getDataString(uri1);
            if(dataString1!=null) {//this if else structure is to prevent empty String[] for photos w/o data
                dataArray1 = dataString1.split("\\s+");
            }else{
                dataString1="0 0 0";
                dataArray1 = dataString1.split("\\s+");
            }
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String)getDate(uri1), 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[0]+"lbs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[2]+"BMIs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[1]+"ins", 12));
                CarouselPicker.CarouselViewAdapter textAdapter1 = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter1.setTextColor(Color.MAGENTA);
                data1.setAdapter(textAdapter1);
            }catch(NullPointerException e) {
                e.printStackTrace();
            }

            //reading photo1 Exif data for photo1Path
            uri2= getImageContentUri(this, new File(photo2Path));
            String dataString2=getDataString(uri2);
            if(dataString2!=null) {//this if else structure is to prevent empty String[] for photos w/o data
                dataArray2 = dataString2.split("\\s+");
            }else{
                dataString2="0 0 0";
                dataArray2 = dataString2.split("\\s+");
            }
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String)getDate(uri2), 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[0]+"lbs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[2]+"BMIs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[1]+"ins", 12));
                CarouselPicker.CarouselViewAdapter textAdapter2 = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter2.setTextColor(Color.MAGENTA);
                data2.setAdapter(textAdapter2);
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            if(uri1!=null&&uri2!=null) {//show difference
                try {
                    List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                    if(getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        textItems.add(new CarouselPicker.TextItem(getDateDifference(uri1, uri2) + "days", 20));
                        textItems.add(new CarouselPicker.TextItem(getDifference(0) + "lbs", 20));
                        textItems.add(new CarouselPicker.TextItem(getDifference(2) + "BMI", 20));
                        textItems.add(new CarouselPicker.TextItem(getDifference(1) + "ins", 20));
                        CarouselPicker.CarouselViewAdapter textAdapterZ = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                        textAdapterZ.setTextColor(Color.MAGENTA);
                        carouselPicker.setAdapter(textAdapterZ);
                    }else{
                        textItems.add(new CarouselPicker.TextItem(getDateDifference(uri1, uri2) + "days", 30));
                        textItems.add(new CarouselPicker.TextItem(getDifference(0) + "lbs", 30));
                        textItems.add(new CarouselPicker.TextItem(getDifference(2) + "BMI", 30));
                        textItems.add(new CarouselPicker.TextItem(getDifference(1) + "ins", 30));
                        CarouselPicker.CarouselViewAdapter textAdapterZ = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                        textAdapterZ.setTextColor(Color.MAGENTA);
                        carouselPickerLandscape.setAdapter(textAdapterZ);
                    }
                }catch (NullPointerException e){
                    e.printStackTrace();
                }
            }else{
                //wait for the other photo to be picked
            }
        }

        enableZooming1=(Button)findViewById(R.id.enableZooming1);
        enableZooming1.setOnClickListener(this);
        enableZooming2=(Button)findViewById(R.id.enableZooming2);
        enableZooming2.setOnClickListener(this);
        enableZoomingLandscape1=(ImageView)findViewById(R.id.enableZoomingLandscape1);
        enableZoomingLandscape1.setOnClickListener(this);
        enableZoomingLandscape2=(ImageView)findViewById(R.id.enableZoomingLandscape2);
        enableZoomingLandscape2.setOnClickListener(this);

        help=(ImageView)findViewById(R.id.helpComparisonLand);
        help.setOnClickListener(this);
    }
    /**
     * Once permission is granted, do these things corresponding to each requestCode
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1://select photo1
                if(grantResults.length>0&&grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO1);
                }
                break;
            case 2://select photo2
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    Intent pickPhoto2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto2, PICK_PHOTO2);
                }
                break;
        }
    }
    /**
     * used when opening Compare from Folder
     * @param context
     * @param imageFile
     * @return
     */
    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }
    /**
     * To make the action bar appear, otherwise it's not there
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compare, menu);
        return true;
    }

    /**
     * used in onCreate() to initialize adapter List
     * @param fontSize of the Carousel item
     * @return List to make Carousel adapter
     */
    public List<CarouselPicker.PickerItem> getList (int fontSize){
        List<CarouselPicker.PickerItem> dataItems=new ArrayList<>();
        dataItems.add(new CarouselPicker.TextItem("date",fontSize));
        dataItems.add(new CarouselPicker.TextItem("weight",fontSize));
        dataItems.add(new CarouselPicker.TextItem("BMI",fontSize));
        dataItems.add(new CarouselPicker.TextItem("height",fontSize));
        return dataItems;
    }

    /**For TOOLBAR
     * Do different tasks based on which button is clicked.
     * @param item buttons in the top toolbar
     * @return whichever button is clicked
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.download:
                ImageView image=new ImageView(this);
                enableZooming1.setVisibility(View.GONE);
                enableZooming2.setVisibility(View.GONE);
                viewBitmap=getBitmapOfView(viewGroup);
                enableZooming1.setVisibility(View.VISIBLE);
                enableZooming2.setVisibility(View.VISIBLE);
                image.setImageBitmap(viewBitmap);
                AlertDialog.Builder collageDialog=new AlertDialog.Builder(this)
                        .setView(image)
                        .setTitle("Collage Preview")
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try{
                                    Calendar calendar=Calendar.getInstance();
                                    SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                    String DateString=sdformat.format(calendar.getTime());

                                    file=new File(getPublicDir(),"mySnapshot_"+DateString+".png");
                                    FileOutputStream fos=new FileOutputStream(file);
                                    viewBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                                    fos.close();
                                    MediaScannerConnection.scanFile(Compare.this,
                                            new String[]{file.getPath()},
                                            null,
                                            null);
                                    Toast.makeText(Compare.this,"Successfully saved: "+file, Toast.LENGTH_SHORT).show();
                                }catch(IOException e){
                                    e.printStackTrace();
                                }
                            }//onClick()
                        })
                        .setNegativeButton("share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Compare.this,"Loading....", Toast.LENGTH_SHORT).show();
                                Calendar calendar=Calendar.getInstance();
                                SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                String DateString=sdformat.format(calendar.getTime());
                                //get Photo uri
                                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                                viewBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                                String path=MediaStore.Images.Media.insertImage(Compare.this.getContentResolver(),
                                        viewBitmap,
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

                            }//onClick()
                        });
                collageDialog.create().show();
                return true;
            case R.id.selectPhoto1:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{//if granted, goes here
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO1);
                }
                return true;
            case R.id.selectPhoto2:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                }else{//if granted, goes here
                    Intent pickPhoto2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto2, PICK_PHOTO2);
                }
                return true;
            case R.id.helpComparison:
                Intent showNote=new Intent(this, HelpComparison.class);
                startActivity(showNote);
                return true;
            default:
                // If we get here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }
    /**ImageView BUTTON Onclick
     * triggered when either imageview is clicked. Differentiated by imageview.ID
     * @param v which imageview is click
     */
    @TargetApi(21)
    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.share_collage:
                ImageView image=new ImageView(this);
                viewBitmap=getBitmapOfView(viewGroupLandscape);
                image.setImageBitmap(viewBitmap);
                AlertDialog.Builder collageDialog=new AlertDialog.Builder(this)
                        .setView(image)
                        .setTitle("Share Collage")
                        .setPositiveButton("share", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Toast.makeText(Compare.this,"Loading....", Toast.LENGTH_SHORT).show();
                                Calendar calendar=Calendar.getInstance();
                                SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                String DateString=sdformat.format(calendar.getTime());
                                //get Photo uri
                                ByteArrayOutputStream bytes=new ByteArrayOutputStream();
                                viewBitmap.compress(Bitmap.CompressFormat.JPEG,100,bytes);
                                //Force photo to scan for new photos stored.
                                String path=MediaStore.Images.Media.insertImage(Compare.this.getContentResolver(),
                                        viewBitmap,
                                        "mySnapshot_"+DateString+".png",
                                        null);
                                //share photo intent
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

                            }//onClick()
                        });
                collageDialog.create().show();
                break;
            case R.id.save_collage:
                ImageView image2=new ImageView(this);
                viewBitmap=getBitmapOfView(viewGroupLandscape);
                image2.setImageBitmap(viewBitmap);
                AlertDialog.Builder collageDialog2=new AlertDialog.Builder(this)
                        .setView(image2)
                        .setTitle("Save Collage")
                        .setPositiveButton("save", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try{
                                    Calendar calendar=Calendar.getInstance();
                                    SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                                    String DateString=sdformat.format(calendar.getTime());

                                    file=new File(getPublicDir(),"mySnapshot_"+DateString+".png");
                                    FileOutputStream fos=new FileOutputStream(file);
                                    viewBitmap.compress(Bitmap.CompressFormat.JPEG,100,fos);
                                    fos.close();
                                    MediaScannerConnection.scanFile(Compare.this,
                                            new String[]{file.getPath()},
                                            null,
                                            null);
                                    Toast.makeText(Compare.this,"Successfully saved: "+file, Toast.LENGTH_SHORT).show();
                                }catch(IOException e){
                                    e.printStackTrace();
                                }
                            }//onClick()
                        });
                collageDialog2.create().show();
                break;
            case R.id.selectPhoto1:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{//if granted, goes here
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO1);
                }
                break;
            case R.id.selectPhoto2:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                }else{//if granted, goes here
                    Intent pickPhoto2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto2, PICK_PHOTO2);
                }
                break;
            case R.id.photo1:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{//if granted, goes here
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO1);
                }
                break;
            case R.id.photo2:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},2);
                }else{//if granted, goes here
                    Intent pickPhoto2 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto2, PICK_PHOTO2);
                }
                break;
            case R.id.enableZooming1:
                if(isEnabled1){
                    photo1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    isEnabled1=false;
                    enableZooming1.setText("enable touch");
                    photo1.setOnClickListener(this);
                    photo1.setOnTouchListener(null);
                }else{//if touching is turned off. state=false, now enable it
                    photo1.setScaleType(ImageView.ScaleType.MATRIX);
                    isEnabled1=true;
                    enableZooming1.setText("disable touch");
                    photo1.setOnClickListener(null);
                    photo1.setOnTouchListener(this);
                }
                break;
            case R.id.enableZooming2:
                if(isEnabled2){
                    photo2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    isEnabled2=false;
                    enableZooming2.setText("enable touch");
                    photo2.setOnClickListener(this);
                    photo1.setOnTouchListener(null);
                }else{//if touching is turned off. state=false
                    photo2.setScaleType(ImageView.ScaleType.MATRIX);
                    isEnabled2=true;
                    enableZooming2.setText("disable touch");
                    photo2.setOnClickListener(null);
                    photo2.setOnTouchListener(this);
                }
                break;
            case R.id.enableZoomingLandscape1:
                if(isEnabled1){
                    photo1.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    isEnabled1=false;
                    enableZoomingLandscape1.setColorFilter(getResources().getColor(R.color.deepBlack));
                    photo1.setOnClickListener(this);
                    photo1.setOnTouchListener(null);
                }else{//if touching is turned off. state=false
                    photo1.setScaleType(ImageView.ScaleType.MATRIX);
                    isEnabled1=true;
                    enableZoomingLandscape1.setColorFilter(getResources().getColor(R.color.white));
                    photo1.setOnClickListener(null);
                    photo1.setOnTouchListener(this);
                }
                break;
            case R.id.enableZoomingLandscape2:
                if(isEnabled2){
                    photo2.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    isEnabled2=false;
                    enableZoomingLandscape2.setColorFilter(getResources().getColor(R.color.deepBlack));
                    photo2.setOnClickListener(this);
                    photo2.setOnTouchListener(null);
                }else{//if touching is turned off. state=false
                    photo2.setScaleType(ImageView.ScaleType.MATRIX);
                    isEnabled2=true;
                    enableZoomingLandscape2.setColorFilter(getResources().getColor(R.color.white));
                    photo2.setOnClickListener(null);
                    photo2.setOnTouchListener(this);
                }
                break;
            case R.id.helpComparisonLand:
                Intent showNote=new Intent(this, HelpComparison.class);
                startActivity(showNote);
        }
    }

    /**
     * retrieve exif image description string
     * @param uri
     * @return
     */
    @TargetApi(24)
    public String getDataString(Uri uri){
        String dataString="Not found";
        try{
            InputStream in;
            in=getContentResolver().openInputStream(uri);
            ExifInterface exifInterface=new ExifInterface(in);
            dataString=exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        }catch(IOException e){
            e.printStackTrace();
        }
        return dataString;
    }
    /**ImageView MEDIA REQUEST HANDLER
     * Do different things based on which imageview is clicked, differentiated by requestCode
     * @param requestCode customized constants
     * @param resultCode  system constant RESULT_OK
     * @param data i dunno. Its an intent.
     */
    @Override
    @TargetApi(24)
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //display the 1st select photo, and its data
        if (requestCode == PICK_PHOTO1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photo1text.setText("");//after a photo is pick, make "Select Photo" disappear
            uri1 = data.getData();
            //reading photo1 Exif data
            String dataString1=getDataString(uri1);
            if(dataString1!=null) {//this if else structure is to prevent empty String[] for photos w/o data
                dataArray1 = dataString1.split("\\s+");
            }else{
                dataString1="0 0 0";
                dataArray1 = dataString1.split("\\s+");
            }
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String)getDate(uri1), 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[0]+"lbs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[2]+"BMIs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray1[1]+"ins", 12));
                CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter.setTextColor(Color.MAGENTA);
                data1.setAdapter(textAdapter);
            }catch(NullPointerException e) {
                e.printStackTrace();
            }

            try {
                Bitmap bitmap1= MediaStore.Images.Media.getBitmap(getContentResolver(), uri1);
                photo1.setImageBitmap(bitmap1);

            } catch (IOException e) {
                e.printStackTrace();
            }
            //if uri2 is already picked, calculate difference
            if(uri1!=null&&uri2!=null) {
                //Relaunch itself to remember comparison for orientation change
                finish();
                Intent comparePhotos=new Intent(this,Compare.class);
                comparePhotos.putExtra("photo1", getUriRealPath(this, uri1));
                comparePhotos.putExtra("photo2",getUriRealPath(this, uri2));
                startActivity(comparePhotos);
            }else{
                //wait for the other photo to be picked
            }
        }
        //display the 2nd select photo, and its data
        if (requestCode == PICK_PHOTO2 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            photo2text.setText("");
            uri2 = data.getData();
            String dataString2=getDataString(uri2);
            if(dataString2!=null) {//this if else structure is to prevent empty String[] for photos w/o data
                dataArray2 = dataString2.split("\\s+");
            }else{
                dataString2="0 0 0";
                dataArray2 = dataString2.split("\\s+");
            }
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String)getDate(uri2), 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[0]+"lbs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[2]+"BMIs", 12));
                textItems.add(new CarouselPicker.TextItem(dataArray2[1]+"ins", 12));
                CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter.setTextColor(Color.MAGENTA);
                data2.setAdapter(textAdapter);
            }catch(NullPointerException e) {
                e.printStackTrace();
            }
            try {
                Bitmap bitmap2= MediaStore.Images.Media.getBitmap(getContentResolver(), uri2);
                photo2.setImageBitmap(bitmap2);

            } catch (IOException e) {
                e.printStackTrace();
            }

            if(uri1!=null&&uri2!=null) {
                //Relaunch itself to remember comparison for orientation change
                finish();
                Intent comparePhotos=new Intent(this,Compare.class);
                comparePhotos.putExtra("photo1", getUriRealPath(this, uri1));
                comparePhotos.putExtra("photo2",getUriRealPath(this, uri2));
                startActivity(comparePhotos);
            }else{
                //wait for the other photo to be picked
            }

        }

    }//OnActivityResult() ends

    /**
     * content:// style uri for photos canNOT be used to make change back to the photo
     * Use this method to get the REAL uri to make changes to the photo
     * @param context
     * @param uri
     * @return
     */
    private String getUriRealPath(Context context, Uri uri){
        String real="";
        if(isAboveKitKat()){//from KitKat above, the uri returned is not real path uri
            //sdk 19 (KitKat) or above
            real=getUriRealPathAboveKitkat(context, uri);
        }else{
            //below sdk 19
            real=getImageRealPath(getContentResolver(), uri, null);
        }
        return real;
    }//getUriRealPath() ends
    @TargetApi(19)
    private String getUriRealPathAboveKitkat(Context context, Uri uri){
        String real="";
        if(isGooglePhotoDoc(uri.getAuthority() )){//check if uri has authority "com.google.android.apps.photos.content"
            real=uri.getLastPathSegment();//gets the decoded last segment
        }else{
            real=getImageRealPath(getContentResolver(),uri,null);//ContentResolver provides access to content model
        }
        return real;
    }//getUriRealPathAboveKitkat() ends
    /**
     * Check if current android is above Kitkat sdk 19
     */
    private boolean isAboveKitKat(){
        boolean real=false;
        real= Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT;
        return real;
    }
    /**
     * Check if this document is provided by google photos
     */
    private boolean isGooglePhotoDoc(String uriAuthority){
        boolean ret=false;
        if("com.google.android.apps.photos.content".equals(uriAuthority)){
            ret=true;
        }
        return ret;
    }
    /**
     * For sdk lower than 19(KitKat)
     * Return uri that represent document file real local path
     */
    private String getImageRealPath(ContentResolver contentResolver, Uri uri, String whereClause){
        String ret="";
        //Query the uri with condition
        Cursor cursor=contentResolver.query(uri,null,whereClause,null,null);
        if(cursor!=null){
            boolean moveToFirst=cursor.moveToFirst();
            if(moveToFirst){
                //Get column name by uri type
                String columnName=MediaStore.Images.Media.DATA;
                if(uri==MediaStore.Images.Media.EXTERNAL_CONTENT_URI){
                    columnName=MediaStore.Images.Media.DATA;
                }else if(uri==MediaStore.Video.Media.EXTERNAL_CONTENT_URI){
                    columnName=MediaStore.Video.Media.DATA;
                }
                //get column index
                int imageColumnIndex=cursor.getColumnIndex(columnName);
                //get column value which is the uri related to local file path
                ret=cursor.getString(imageColumnIndex);

            }
        }
        return ret;
    }

    /**
     * For getting the date taken of photo with that uri
     * @param photoUri photo uri passed from imageview button handler
     * @return string MM/dd/yyyy
    */
    public CharSequence getDate(Uri photoUri){
        Long longDate=null;
        String[] projection=new String[] {MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur=managedQuery(photoUri,projection,null,null,null);
        if(cur.moveToFirst()){//when cursor is empty
            int dateColumn=cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            longDate = cur.getLong(dateColumn);
        }
        Date d=new Date(longDate);
        java.text.DateFormat formatter=new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

    /**
     *
     * @param uri1
     * @param uri2
     * @return String difference in days (Date of uri2-Date of uri1)
     */
    public String getDateDifference(Uri uri1, Uri uri2){
        Long longDate1=null;//UNIX time in millisec
        Long longDate2=null;
        Long differenceInDays=null;

        String[] projection=new String[] {MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur1=managedQuery(uri1,projection,null,null,null);

        //get UNIX time for first photo
        if(cur1.moveToFirst()){
            int dateColumn=cur1.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do{
                longDate1=cur1.getLong(dateColumn);
            }while(cur1.moveToNext());
        }

        //get UNIX time for second photo
        Cursor cur2=managedQuery(uri2,projection,null,null,null);
        if(cur2.moveToFirst()){
            int dateColumn=cur2.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            do{
                longDate2=cur2.getLong(dateColumn);
            }while(cur2.moveToNext());
        }

        differenceInDays=(longDate2-longDate1)/1000/60/60/24;
        return differenceInDays.toString();
    }

    /**
     * returns the string difference of string2-string1
     * @param index 0 weight, 1 height, 2 BMI
     * @return
     */
    public String getDifference(int index){
        double[] intDifference={1,2,3};
        String[] stringDifference={"1","2","3"};
        for (int j=0; j<3; j++){//4 because there are only 3 convertible string, the rest is words
            intDifference[j]=Double.parseDouble(dataArray2[j])-Double.parseDouble(dataArray1[j]);
        }
        stringDifference[0] = Integer.toString((int)intDifference[0]);
        stringDifference[2]=String.format("%.1f", intDifference[2]);
        stringDifference[1] = Integer.toString((int)intDifference[1]);

        return stringDifference[index];
    }

    /**
     * find the distance between two fingers
     * @param event
     * @return
     */
    private float findDistance(MotionEvent event){
        float x=event.getX(0)-event.getX(1);
        float y=event.getY(0)-event.getX(1);
        float d=x*x+y*y;
        return (float) Math.sqrt(d);
    }

    /**
     * find the midpt between two fingers, used for Matrix.postScale()
     * @param point
     * @param event
     */
    private void findMidPoint(PointF point, MotionEvent event){
        float x=event.getX(0)+event.getX(1);
        float y=event.getY(0)+event.getY(1);
        point.set(x/2,y/2);
    }

    /**
     * Dragging and Zooming using imageview matrix scaleType
     * @param v which imageview
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event){
        switch(v.getId()){
            case R.id.photo1:
                switch(event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_DOWN://first pointer down
                        savedMatrix1.set(matrix1);
                        start1.set(event.getX(), event.getY());
                        mode1=DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist1=findDistance(event);
                        if(oldDist1>10f){
                            savedMatrix1.set(matrix1);
                            findMidPoint(mid1,event);
                            mode1=ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        mode1=NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mode1==DRAG) {
                            matrix1.set(savedMatrix1);
                            float dx = event.getX() - start1.x;
                            float dy = event.getY() - start1.y;
                            matrix1.postTranslate(dx, dy);
                            break;
                        }else if (mode1==ZOOM){
                            float newDist=findDistance(event);
                            if(newDist>10f){
                                matrix1.set(savedMatrix1);
                                float scale=(newDist/oldDist1);
                                matrix1.postScale(scale,scale,mid1.x,mid1.y);
                            }
                        }
                }//switch
                photo1.setImageMatrix(matrix1);
                bmap1 = Bitmap.createBitmap(photo1.getWidth(), photo1.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas1 = new Canvas(bmap1);
                photo1.draw(canvas1);
                break;

            case R.id.photo2:
                switch(event.getAction() & MotionEvent.ACTION_MASK){
                    case MotionEvent.ACTION_DOWN://first pointer down
                        savedMatrix2.set(matrix2);
                        start2.set(event.getX(), event.getY());
                        mode2=DRAG;
                        break;
                    case MotionEvent.ACTION_POINTER_DOWN:
                        oldDist2=findDistance(event);
                        if(oldDist2>10f){
                            savedMatrix2.set(matrix2);
                            findMidPoint(mid2,event);
                            mode2=ZOOM;
                        }
                        break;
                    case MotionEvent.ACTION_POINTER_UP:
                    case MotionEvent.ACTION_UP:
                        mode2=NONE;
                        break;
                    case MotionEvent.ACTION_MOVE:
                        if(mode2==DRAG) {
                            matrix2.set(savedMatrix2);
                            float dx = event.getX() - start2.x;
                            float dy = event.getY() - start2.y;
                            matrix2.postTranslate(dx, dy);
                            break;
                        }else if (mode2==ZOOM){
                            float newDist=findDistance(event);
                            if(newDist>10f){
                                matrix2.set(savedMatrix2);
                                float scale=(newDist/oldDist2);
                                matrix2.postScale(scale,scale,mid2.x,mid2.y);
                            }
                        }
                }//switch
                photo2.setImageMatrix(matrix2);
                bmap2 = Bitmap.createBitmap(photo2.getWidth(), photo2.getHeight(), Bitmap.Config.RGB_565);
                Canvas canvas2 = new Canvas(bmap2);
                photo2.draw(canvas2);
        }//switch for different IDs

        return true;
    }

    /**
     * Get the Bitmap screenshot of a view
     * @param v
     * @return
     */
    public static Bitmap getBitmapOfView(View v){
        Bitmap b=Bitmap.createBitmap(v.getWidth(),
                v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c=new Canvas(b);
        v.layout(v.getLeft(),v.getTop(),v.getRight(),v.getBottom());
        v.draw(c);
        return b;
    }
    /**
     *Get folder path
     * @return file path to a folder (not a file)
     */
    public File getPublicDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_DCIM),"collage");
        if (!file.mkdirs()) {
            Log.e("PUBLIC DIRECTORY", "Directory not created");
        }
        return file;
    }

}

