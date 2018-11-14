package sidebyside3.david.com.sidebyside4;

import android.graphics.Bitmap;


/**
 * Created by irpei on 9/7/2018.
 */

public class GridViewItem {

    private String path;
    private boolean isDirectory;
    private Bitmap image;
    private boolean isSelected;
    private int position;
    private long millis;//date taken of this item photo

    public GridViewItem(String path, boolean isDirectory, Bitmap image, boolean isSelected, int position) {
        this.path = path;
        this.isDirectory = isDirectory;
        this.image = image;
        this.isSelected = isSelected;
        this.position = position;
    }


    public String getPath() {
        return path;
    }


    public boolean isDirectory() {
        return isDirectory;
    }


    public Bitmap getImage() {
        return image;
    }

    public boolean isSelected() {return isSelected; }

    public void setSelected(boolean s)
    {
        isSelected = s;
    }
    public int getPosition() { return position;}
}