package sidebyside3.david.com.sidebyside4;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Gongwei (David) Chen on 9/10/2018.
 */

public class CategoryAdapter extends FragmentPagerAdapter{
    /**
     * Context of the app
     */
    private Context mContext;
    /**
     *
     * @param fm fragment manager keeping each fragment's state in the adapter across swipes
     */
    public CategoryAdapter(Context context, FragmentManager fm){
        super(fm);
        mContext=context;
    }

    /**
     * For labelling the tabs
     * @param position
     * @return
     */
    @Override
    public CharSequence getPageTitle(int position) {
        if(position==0){
            return mContext.getString(R.string.Photo);
        }else{
            return mContext.getString(R.string.Folder);
        }
    }

    @Override
    public Fragment getItem(int position) {
        if(position==0){
            return new PhonesFragment();
        }else{
            return new AppsFragment();
        }
    }

    /**
     * Determines number of tabs. If more than actual tabs, the last tab will repeat.
     * @return
     */
    @Override
    public int getCount() {
        return 2;
    }
}
