package sidebyside3.david.com.sidebyside4;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class PhonesFragment extends Fragment implements AdapterView.OnItemClickListener{
    View rootView;
    List<GridViewItem> gridItems;

    public PhonesFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView=inflater.inflate(R.layout.word_list,container,false);
        setGridAdapter("/sdcard/DCIM/Raspberry");
        return rootView;
    }

    /**
     * create List<GridViewItem> gridItems Array and the adapter from Array
     * and bind the adapter to gridView in wordlist
     * @param path
     */
    private void setGridAdapter(String path){
        //create a new grid adapter
        gridItems=createGridItems(path);
        MyGridAdapter adapter=new MyGridAdapter(getActivity(), gridItems);

        //set the grid adapter;
        GridView gridView=(GridView)rootView.findViewById(R.id.gridView);
        gridView.setAdapter(adapter);

        //set the onclicklistener
        gridView.setOnItemClickListener(this);
    }

    /**
     * Go through the specified directory and create list items for adapter
     * @param directoryPath used to create Array of files
     * @return
     */
    private List<GridViewItem> createGridItems(String directoryPath){
        List<GridViewItem> items=new ArrayList<GridViewItem>();

        //List all the items within the folder
        File[] files=new File(directoryPath).listFiles();

        for (File file:files){

            //Add the directories containing images or sub-directories
            if(file.isDirectory() && file.listFiles(new ImageFileFilter()).length>0){
                items.add(new GridViewItem(file.getAbsolutePath(),true,null));
            }
            //Add the images
            else{
                Bitmap image=BitmapHelper.decodeBitmapFromFile(file.getAbsolutePath(),50,50);
                items.add(new GridViewItem(file.getAbsolutePath(),false,image));
            }
        }
        return items;
    }

    /**
     * Checks if file has compatible extension
     * @param filePath
     * @return
     */
    private boolean isImageFile(String filePath){
        if(filePath.endsWith(".jpg")||filePath.endsWith(".png")){
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        if(gridItems.get(position).isDirectory()){
            setGridAdapter(gridItems.get(position).getPath());
        }else{
            //Display the image
        }
    }

    private class ImageFileFilter implements FileFilter {
        @Override
        public boolean accept(File file){
            if(file.isDirectory()){
                return true;
            }else if(isImageFile(file.getAbsolutePath())){
                return true;
            }
            return false;
        }
    }



}
