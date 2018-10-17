package sidebyside3.david.com.sidebyside4;

import android.annotation.TargetApi;
import android.app.Activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;


import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class Folder extends Activity implements AdapterView.OnItemClickListener
        ,View.OnClickListener{
    int SELECT_MULTIPLE=911;
    List<GridViewItem> gridItems;
    List<String> toCompare;
    MyGridAdapter adp;
    GridView gridView;
    FloatingActionButton but;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder);
        but = (FloatingActionButton)findViewById(R.id.button);
        but.setOnClickListener(this);
        toCompare = new ArrayList<>();
        setGridAdapter("/sdcard/DCIM/raspberry");
    }
    @Override
    public void onClick(View view) {
        switch(view.getId()){
            case R.id.button+1:
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
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*"); //allows any image file type. Change * to specific extension to limit it
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_MULTIPLE);
                break;
        }
    }
    @TargetApi(16)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SELECT_MULTIPLE) {
            if(resultCode == Activity.RESULT_OK) {
                ArrayList<Uri> selectedUris=new ArrayList<Uri>();
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for(int i = 0; i < count; i++){
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        //selectedUris.add(imageUri);
                        File f = new File(imageUri.getPath());
                        Bitmap image = BitmapFactory.decodeFile(f.getPath());
                        FileOutputStream out = null;
                        try {
                            out = new FileOutputStream("/sdcard/DCIM/raspberry/" + f.getName());
                            image.compress(Bitmap.CompressFormat.PNG, 100, out);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        finally {
                            if(out != null)
                            {
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            } else if(data.getData() != null) {
                String imagePath = data.getData().getPath();
                //do something with the image (save it to some directory or whatever you need to do with it here)
            }
        }

    }

    /**
     * This will create our GridViewItems and set the adapter
     *
     * @param path
     *            The directory in which to search for images
     */
    private void setGridAdapter(String path) {
        // Create a new grid adapter
        gridItems = createGridItems(path);
        adp = new MyGridAdapter(this, gridItems);

        // Set the grid adapter
        gridView = (GridView) findViewById(R.id.gridView);
        gridView.setAdapter(adp);

        // Set the onClickListener
        gridView.setOnItemClickListener(this);
    }


    /**
     * Go through the specified directory, and create items to display in our
     * GridView
     */
    private List<GridViewItem> createGridItems(String directoryPath) {
        List<GridViewItem> items = new ArrayList<GridViewItem>();

        // List all the items within the folder.
        File[] files = new File(directoryPath).listFiles();

        for (int i = 0; i < files.length; i++) {
            final File file = files[i];
            // Add the directories containing images or sub-directories
            if (file.isDirectory()
                    && file.listFiles(new ImageFileFilter()).length > 0) {
                items.add(new GridViewItem(file.getAbsolutePath(), true, null, false, i));
            }
            // Add the images
            else {
                Bitmap image = BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),
                        50,
                        50);

                items.add(new GridViewItem(file.getAbsolutePath(), false, image, false, i));
            }
        }

        return items;
    }


    /**
     * Checks the file to see if it has a compatible extension.
     */
    private boolean isImageFile(String filePath) {
        if (filePath.endsWith(".jpg") || filePath.endsWith(".png"))
        // Add other formats as desired
        {
            return true;
        }
        return false;
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
                    image.setColorFilter(Color.argb(150,200,200,200));
                }
            }
        }

    }

    /**
     * This can be used to filter files.
     */
    private class ImageFileFilter implements FileFilter {

        @Override
        public boolean accept(File file) {
            if (file.isDirectory()) {
                return true;
            }
            else if (isImageFile(file.getAbsolutePath())) {
                return true;
            }
            return false;
        }
    }
}
