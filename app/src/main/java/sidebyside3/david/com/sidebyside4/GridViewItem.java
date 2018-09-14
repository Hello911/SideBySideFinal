package sidebyside3.david.com.sidebyside4;

import android.graphics.Bitmap;

import java.io.File;

/**
 * Created by Gongwei (David) Chen on 9/12/2018.
 */

public class GridViewItem {

    private String path;
    private boolean isDirectory;
    private Bitmap image;

    public GridViewItem(String path, boolean isDirectory, Bitmap image){
        this.path=path;
        this.isDirectory=isDirectory;
        this.image=image;
    }

    public String getPath(){
        return path;
    }

    public boolean isDirectory(){
        return isDirectory;
    }

    public Bitmap getImage(){
        return image;
    }
}
