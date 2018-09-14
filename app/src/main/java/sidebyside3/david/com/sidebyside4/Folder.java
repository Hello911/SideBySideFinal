package sidebyside3.david.com.sidebyside4;

import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class Folder extends AppCompatActivity {
    private static final int NUM_PAGES=2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.folder);
        ViewPager viewPager=(ViewPager) findViewById(R.id.viewpager);
        CategoryAdapter adapter=new CategoryAdapter(this,getSupportFragmentManager());
        viewPager.setAdapter(adapter);

        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
    }
}
