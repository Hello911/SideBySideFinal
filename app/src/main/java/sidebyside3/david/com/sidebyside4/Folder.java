package sidebyside3.david.com.sidebyside4;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Folder extends Activity implements AdapterView.OnItemClickListener
        ,View.OnClickListener{
    List<GridViewItem> gridItems;
    List<String> toCompare;
    public static MyGridAdapter adp;
    public static GridView gridView;
    FloatingActionButton but;
    String folderPath;
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    MyAsyncTask createItems;

    //Import
    int PICK_IMAGE_MULTIPLE = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder);
        gridView = (GridView) findViewById(R.id.gridView);
        gridItems=new ArrayList<>();
        but = (FloatingActionButton)findViewById(R.id.button);
        but.setOnClickListener(this);
        toCompare = new ArrayList<>();
        folderPath=Environment.getExternalStorageDirectory()+ getResources().getString(R.string.file);
        if(ContextCompat.checkSelfPermission(this
                , Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
            Toast.makeText(this,"Enable permission in Setting->App",Toast.LENGTH_SHORT).show();

        }else{
            setGridAdapter(folderPath);
        }
        pref=getApplicationContext().getSharedPreferences("photoNum",MODE_PRIVATE);
        editor=pref.edit();
    }

    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button+1://changes the button ID to make it Compare
                if(toCompare.size()==2){
                    Intent comparePhotos=new Intent(this,Compare.class);
                    comparePhotos.putExtra("photo1",toCompare.get(0));
                    comparePhotos.putExtra("photo2",toCompare.get(1));
                    startActivity(comparePhotos);
                }else{
                    Toast.makeText(this, "Pick one more photo",Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.button:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT
                    , MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
                break;
        }
    }



    @TargetApi(19)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        try{
            if(requestCode==PICK_IMAGE_MULTIPLE && resultCode==RESULT_OK && data!=null){

                if (data.getClipData() != null) {//when user pick many photos
                    ClipData mClipData = data.getClipData();
                    for (int i = 0; i < mClipData.getItemCount(); i++) {
                        ClipData.Item item = mClipData.getItemAt(i);
                        /**copied starts**/
                        Uri selectedImage = item.getUri();
                        String wholeID = DocumentsContract.getDocumentId(selectedImage);

                        // Split at colon, use second item in the array
                        String id = wholeID.split(":")[1];

                        String[] column = {MediaStore.Images.Media.DATA};

                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";

                        Cursor cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                                , column
                                , sel
                                , new String[]{id}
                                , null);

                        String filePath = "";

                        int columnIndex = cursor.getColumnIndex(column[0]);

                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                        /**copied ends**/
                        //gets the date of the original photo and later store it in the DATE column of the copy
                        File originalFile=new File(filePath);
                        Date d=new Date(originalFile.lastModified());
                        long millis=d.getTime();

                        //increment count of photo taken on a day
                        int num=pref.getInt("photos",0);
                        Calendar cal=Calendar.getInstance();
                        int today=cal.get(Calendar.DAY_OF_MONTH);
                        cal.setTime(d);
                        int dateTaken=cal.get(Calendar.DAY_OF_MONTH);
                        if(dateTaken==today){
                            num++;
                            editor.putInt("photos",num);
                            editor.putInt("day",today);
                            editor.apply();
                        }


                        Bitmap bmp1 = BitmapFactory.decodeFile(filePath);
                        Bitmap bmp2 = bmp1.copy(bmp1.getConfig(), true);
                        //store the newly made copy in another folder
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat sdformat = new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                        String DateString = sdformat.format(calendar.getTime());
                        File file=null;
                        try {
                            file = new File(getPublicDir(), "SideBySide4" + DateString + "_Copy_" + (i + 1) + ".jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            bmp2.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                            MediaScannerConnection.scanFile(this,
                                    new String[]{file.getPath()},
                                    null,
                                    null);
                            Toast.makeText(this,mClipData.getItemCount()+" photos successfully imported!", Toast.LENGTH_SHORT).show();
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this,mClipData.getItemCount()+" photos NOT imported", Toast.LENGTH_SHORT).show();
                        }
                        ContentValues mUpdateValues=new ContentValues();
                        mUpdateValues.put(MediaStore.Images.Media.DATE_TAKEN,millis);
                        int mRowsUpdated=getContentResolver().update(
                                getImageContentUri(this,file)
                                ,mUpdateValues
                                ,null
                                ,null
                        );
                        //Finally, delete the original photo and refresh Folder
                        originalFile.delete();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(originalFile)));
                        GridViewItem toAdd=new GridViewItem(file.toString(), false, bmp2,false,adp.getCount()-1);
                        adp.add(toAdd);
                        adp.notifyDataSetChanged();
                    }

                }else{//when user picks 1 photo
                    if(data.getData()!=null){
                        /**copied starts**/
                        Uri selectedImage = data.getData();
                        String wholeID = DocumentsContract.getDocumentId(selectedImage);

                        // Split at colon, use second item in the array
                        String id = wholeID.split(":")[1];

                        String[] column = { MediaStore.Images.Media.DATA };

                        // where id is equal to
                        String sel = MediaStore.Images.Media._ID + "=?";

                        Cursor cursor = getContentResolver().
                                query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                        column, sel, new String[]{ id }, null);

                        String filePath = "";

                        int columnIndex = cursor.getColumnIndex(column[0]);

                        if (cursor.moveToFirst()) {
                            filePath = cursor.getString(columnIndex);
                        }
                        cursor.close();
                        /**copied ends**/
                        //gets the date of the original photo and later store it in the DATE column of the copy
                        File originalFile=new File(filePath);
                        Date d=new Date(originalFile.lastModified());
                        Long millis=d.getTime();

                        //increment count of photo taken on a day
                        int num=pref.getInt("photos",0);
                        Calendar cal=Calendar.getInstance();
                        int today=cal.get(Calendar.DAY_OF_MONTH);
                        cal.setTime(d);
                        int dateTaken=cal.get(Calendar.DAY_OF_MONTH);
                        if(dateTaken==today){
                            num++;
                            editor.putInt("photos",num);
                            editor.putInt("day",today);
                            editor.apply();
                        }

                        Bitmap bmp1=BitmapFactory.decodeFile(filePath);
                        Bitmap bmp2=bmp1.copy(bmp1.getConfig(),true);
                        //store the newly made copy in another folder
                        Calendar calendar=Calendar.getInstance();
                        SimpleDateFormat sdformat=new SimpleDateFormat("MM_dd_yyyy_HH:mm:ss");
                        String DateString=sdformat.format(calendar.getTime());
                        File file=null;
                        try {
                            file = new File(getPublicDir(), "SideBySide4"+ DateString +"_Copy_1.jpg");
                            FileOutputStream fos = new FileOutputStream(file);
                            bmp2.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                            fos.close();
                            MediaScannerConnection.scanFile(this,
                                    new String[]{file.getPath()},
                                    null,
                                    null);
                            Toast.makeText(this,"1 photo successfully imported!", Toast.LENGTH_SHORT).show();
                        }catch(IOException e) {
                            e.printStackTrace();
                            Toast.makeText(this, "1 photo import FAILED", Toast.LENGTH_SHORT).show();
                        }
                        ContentValues mUpdateValues=new ContentValues();
                        mUpdateValues.put(MediaStore.Images.Media.DATE_TAKEN,millis);
                        int mRowsUpdated=getContentResolver().update(
                                getImageContentUri(this,file)
                                ,mUpdateValues
                                ,null
                                ,null
                        );
                        Date date=new Date(millis);
                        java.text.DateFormat formatter=new SimpleDateFormat("MM/dd/yyyy");
                        Log.i("date stored", formatter.format(date));
                        //Finally, delete the original photo and refresh Folder
                        originalFile.delete();
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(originalFile)));
                        GridViewItem toAdd=new GridViewItem(file.toString(), false, bmp2,false,adp.getCount()-1);
                        adp.add(toAdd);
                        adp.notifyDataSetChanged();
                    }
                }//if else ends

            }
        }catch(Exception e){
            Toast.makeText(this, e.toString(),Toast.LENGTH_LONG).show();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static Uri getImageContentUri(Context context, File imageFile) {
        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "" + id);
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

    /**
     * This will create our GridViewItems and set the adapter
     *
     * @param path
     *            The directory in which to search for images
     */
    private void setGridAdapter(String path) {
        // Create a new grid adapter
        createGridItems(path);
    }



    /**
     * Go through the specified directory, and create items to display in our
     * GridView
     */
    private void createGridItems(String directoryPath) {

        MyAsyncTask createItems = new MyAsyncTask(this,new MyAsyncTask.AsyncResponse() {
            @Override
            public void processFinish(List<GridViewItem> list) {
                gridItems=list;
                adp = new MyGridAdapter(Folder.this, gridItems);
                // Set the grid adapter

                //gridView.setAdapter(adp);

                // Set the onClickListener
                gridView.setOnItemClickListener(Folder.this);
            }
        });
        //createItems.execute(folderPath);
        createItems.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,folderPath);
        //sometimes, the thread executed last time Folder was opened isn't done.
        //THREAD_POOL_EXECUTE make this thread run in parallel with the previous unfinished ones

    }



    /**
     * Chnage FAB to compare icon
     * @param parent
     * @param view
     * @param position
     * @param id
     */
    @Override
    public void
    onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //if item clicked is a folder, initialize adp adapter with what's in the folder
        if (gridItems.get(position).isDirectory()) {
            setGridAdapter(gridItems.get(position).getPath());
        }
        //if item clicked is just a photo
        else {
            GridViewItem item=gridItems.get(position);
            adp.getView(position, view, null);
            if(item.isSelected()){//if item is already selected
                if(toCompare.size()==1){
                    but.setImageResource(R.drawable.add);
                    but.setId(R.id.button);
                }
                item.setSelected(false);
                ViewGroup group=(ViewGroup)view;
                ImageView image=(ImageView)group.getChildAt(0);
                image.setColorFilter(null);
                if(toCompare.contains(item.getPath())){
                    toCompare.remove(item.getPath());
                }
            }else{//if item is NOT selected yet
                if(toCompare.size()>=2){
                    Toast.makeText(this, "Cannot compare more than 2 photos", Toast.LENGTH_SHORT).show();
                }else{
                    but.setId(R.id.button+1);
                    but.setImageResource(R.drawable.compare);
                    item.setSelected(true);
                    toCompare.add(item.getPath());
                    ViewGroup group=(ViewGroup)view;
                    ImageView image=(ImageView)group.getChildAt(0);
                    image.setColorFilter(Color.argb(150,0,128,255));
                }
            }
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults){
        switch(requestCode){
            case 1:{
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    setGridAdapter("/sdcard/DCIM/raspberry");
                }else{

                }
            }
        }
    }

}
