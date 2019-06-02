package sidebyside3.david.com.sidebyside5.offline;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import in.goodiebag.carouselpicker.CarouselPicker;
import sidebyside3.david.com.sidebyside5.R;

public class Edit extends AppCompatActivity implements View.OnTouchListener, View.OnClickListener{
    ImageView photo;
    Button enableZooming;
    boolean isEnabled=false;//for changing the scaleType of photo
    private final int PICK_PHOTO = 1;
    CarouselPicker carouselPicker;
    String dataArray[];
    Uri uri;

    //DRAGGING & ZOOMING
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;

    //set of touch parameters for photo1
    private int mode = NONE;
    private Matrix matrix = new Matrix();
    private Matrix savedMatrix = new Matrix();
    private PointF start = new PointF();//PointF holds 2 coordinates
    private PointF mid = new PointF();
    private Bitmap bmap;
    private float oldDist = 1f;

    //edit data layout views. For getting number from text field in onPermission
    EditText mWeight;
    EditText mHeight;
    EditText mNote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit);

        //If these 2 lines are not added, the toolbar will appear without icon
        android.support.v7.widget.Toolbar myToolbar = (android.support.v7.widget.Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#2E2E2E")));

        //The scroll wheel
        carouselPicker = (CarouselPicker) findViewById(R.id.carousel);
        List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
        //20 here represents the textSize in dp, change it to the value you want.
        textItems.add(new CarouselPicker.TextItem("date", 20));
        textItems.add(new CarouselPicker.TextItem("weight", 20));
        textItems.add(new CarouselPicker.TextItem("BMI", 20));
        textItems.add(new CarouselPicker.TextItem("height", 20));
        CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
        textAdapter.setTextColor(Color.MAGENTA);
        carouselPicker.setAdapter(textAdapter);

        photo=(ImageView)findViewById(R.id.photo);
        photo.setOnTouchListener(this);

        enableZooming=(Button)findViewById(R.id.enableZooming);
        enableZooming.setOnClickListener(this);

        /**Relaunching to remember choice for orientation change
         *Need to convert from filePath to uri because security problem with sending uri with Intent
         */
        Intent retrievePhotos = getIntent();
        String photoPath = retrievePhotos.getStringExtra("photo");
        if (photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            photo.setImageBitmap(bitmap);
            //read photo exif data from global uri variable
            uri=Compare.getImageContentUri(this, new File(photoPath));
            updateCarousel();
        }
    }

    /**
     * To make the toolbar appear
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit, menu);
        return true;
    }

    Runnable mTimer1;
    ExecutorService threadKiller = Executors.newSingleThreadExecutor();
    Future futureThread;
    Handler mHandler=new Handler();
    private int[] voltagePts=new int[40];
    private double lastTime=0;
    /**
     * What happen when a button in toolbar is clicked
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.selectPhoto:
                if(ContextCompat.checkSelfPermission(this
                        , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{//if granted, goes here
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO);
                }
                break;
            case R.id.seeComment:
                if(uri!=null) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                    String message = "Note: ";
                    for (int j = 3; j < dataArray.length; j++) {
                        message += dataArray[j] + " ";
                    }
                    alertDialogBuilder.setMessage(message);
                    AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                }else
                    Toast.makeText(this,"Select a photo first to see note.",Toast.LENGTH_SHORT).show();
                break;
            case R.id.editExif:
                if (uri != null) {
                    LayoutInflater factory = LayoutInflater.from(this);
                    final View view = factory.inflate(R.layout.editdata, null);
                    final EditText weight = (EditText) view.findViewById(R.id.weight);
                    final EditText height = (EditText) view.findViewById(R.id.height);
                    final EditText note = (EditText) view.findViewById(R.id.note);

                    mWeight=weight;
                    mHeight=height;
                    mNote=note;

                    AlertDialog.Builder exifDialog = new AlertDialog.Builder(this)
                            .setTitle("Edit Health Data")
                            .setView(view)
                            .setPositiveButton("save data", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if(weight.getText().toString().equals("")){
                                        weight.setText("0");
                                    }
                                    if(height.getText().toString().equals("")){
                                        height.setText("0");
                                    }
                                    double BMI = Double.parseDouble(weight.getText().toString()) / Double.parseDouble(height.getText().toString()) / Double.parseDouble(height.getText().toString()) * 703;
                                    String bmi = String.format("%.1f", BMI);
                                    if("".equals(note.getText().toString())){
                                        note.setText("nothing written");
                                    }
                                    String description = weight.getText() + " "
                                            + height.getText() + " "
                                            + bmi + " "
                                            + note.getText();
                                    try {
                                        File file = new File(getUriRealPath(Edit.this, uri));
                                        ExifInterface exifInterface = new ExifInterface(file.getCanonicalPath());
                                        exifInterface.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, description);
                                        exifInterface.saveAttributes();
                                        Toast.makeText(Edit.this, "Data successfully saved!", Toast.LENGTH_SHORT).show();
                                        updateCarousel();

                                    } catch (IOException e) {
                                        e.printStackTrace();
                                        Toast.makeText(Edit.this, "Oops, an error prevents saving: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                            }//onClick()
                            });
                    exifDialog.create().show();
                } else
                    Toast.makeText(this, "Select a photo first to see note.", Toast.LENGTH_SHORT).show();
        }//switch
        return true;
    }
    /**
     * Once permission is granted, do these things corresponding to each requestCode
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1://select photo
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Intent pickPhoto1 = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto1, PICK_PHOTO);
                }
                break;
        }
    }
    /**
     * Handle after user picks the photo
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //display the 1st select photo, and its data
        if (requestCode == PICK_PHOTO && resultCode == RESULT_OK && data != null && data.getData() != null) {

            uri = data.getData();
            //relaunch to remember its choice in case of orientation change
            finish();//other wise back button need to be pressed twice
            Intent relaunch=new Intent(this,Edit.class);
            relaunch.putExtra("photo",getUriRealPath(this,uri));
            startActivity(relaunch);

        }
    }//OnActivityResult() ends

    /**
     * Every time the data is updated, call this method to show the roller with new data
     */
    @TargetApi(24)
    public void updateCarousel(){
        InputStream in;
        String dataString = "Not found";
        try {
            in = getContentResolver().openInputStream(uri);
            ExifInterface exifInterface = new ExifInterface(in);
            dataString = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //this if else structure is to prevent empty String[] for photos w/o data
        if (dataString != null) {
            dataArray = dataString.split("\\s+");
        } else {
            dataString = "0 0 0 0";
            dataArray = dataString.split("\\s+");
        }
        if(dataArray.length>=4) {
            try {
                List<CarouselPicker.PickerItem> textItems = new ArrayList<>();
                textItems.add(new CarouselPicker.TextItem((String) getDate(uri), 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[0] + "lbs", 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[2] + "BMIs", 15));
                textItems.add(new CarouselPicker.TextItem(dataArray[1] + "ins", 15));
                CarouselPicker.CarouselViewAdapter textAdapter = new CarouselPicker.CarouselViewAdapter(this, textItems, 0);
                textAdapter.setTextColor(Color.MAGENTA);
                carouselPicker.setAdapter(textAdapter);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }else
            Toast.makeText(this,"Wrong # of parameters",Toast.LENGTH_SHORT).show();
    }
    /**
     * For getting the date taken of photo with that uri
     *
     * @param photoUri photo uri passed from imageview button handler
     * @return string MM/dd/yyyy
     */
    public CharSequence getDate(Uri photoUri) {
        Long longDate = null;
        String[] projection = new String[]{MediaStore.Images.Media.DATE_TAKEN};
        Cursor cur = managedQuery(photoUri, projection, null, null, null);
        if (cur.moveToFirst()) {//when cursor is empty
            int dateColumn = cur.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN);
            longDate = cur.getLong(dateColumn);
        }
        Date d = new Date(longDate);
        java.text.DateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        return formatter.format(d);
    }

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
     * find the distance between two fingers
     *
     * @param event
     * @return
     */
    private float findDistance(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getX(1);
        float d = x * x + y * y;
        return (float) Math.sqrt(d);
    }

    /**
     * find the midpt between two fingers, used for Matrix.postScale()
     *
     * @param point
     * @param event
     */
    private void findMidPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
    }

    /**
     * Dragging and Zooming using imageview matrix scaleType
     *
     * @param v     which imageview
     * @param event
     * @return
     */
    public boolean onTouch(View v, MotionEvent event) {

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN://first pointer down
                savedMatrix.set(matrix);
                start.set(event.getX(), event.getY());
                mode = DRAG;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = findDistance(event);
                if (oldDist > 10f) {
                    savedMatrix.set(matrix);
                    findMidPoint(mid, event);
                    mode = ZOOM;
                }
                break;
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_UP:
                mode = NONE;
                break;
            case MotionEvent.ACTION_MOVE:
                if (mode == DRAG) {
                    matrix.set(savedMatrix);
                    float dx = event.getX() - start.x;
                    float dy = event.getY() - start.y;
                    matrix.postTranslate(dx, dy);
                    break;
                } else if (mode == ZOOM) {
                    float newDist = findDistance(event);
                    if (newDist > 10f) {
                        matrix.set(savedMatrix);
                        float scale = (newDist / oldDist);
                        matrix.postScale(scale, scale, mid.x, mid.y);
                    }
                }
        }//switch
        photo.setImageMatrix(matrix);
        bmap = Bitmap.createBitmap(photo.getWidth(), photo.getHeight(), Bitmap.Config.RGB_565);
        Canvas canvas1 = new Canvas(bmap);
        photo.draw(canvas1);

        return true;
    }//onTouch() ends

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.enableZooming:
                if(isEnabled){
                    photo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    isEnabled=false;
                    enableZooming.setText("enable touch");
                }else{//if touching is turned off. state=false
                    photo.setScaleType(ImageView.ScaleType.MATRIX);
                    isEnabled=true;
                    enableZooming.setText("disable touch");
                }
        }
    }
}
