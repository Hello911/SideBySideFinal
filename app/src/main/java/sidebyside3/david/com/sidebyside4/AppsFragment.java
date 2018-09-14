package sidebyside3.david.com.sidebyside4;


import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 */
public class AppsFragment extends Fragment {


    public AppsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
       View rootView=inflater.inflate(R.layout.word_list2,container,false);
        final ArrayList<String> words=new ArrayList<String>();
        words.add("apple");
        words.add("berry");
        words.add("cinder");
        words.add("deria");
        words.add("ericia");
        words.add("furia");
        words.add("ghast");
        words.add("hoist");
        words.add("ioso");
        words.add("johal");
        words.add("kolin");
        words.add("molan");
        words.add("nolan");
        ArrayAdapter adapter=new ArrayAdapter(getActivity(), R.layout.listitem, words);
        ListView listView=(ListView)rootView.findViewById(R.id.list);
        listView.setAdapter(adapter);
        return rootView;
    }

}
