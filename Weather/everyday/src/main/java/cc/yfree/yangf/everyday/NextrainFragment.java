package cc.yfree.yangf.everyday;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 */
public class NextrainFragment extends Fragment {
    View view;

    public NextrainFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_nextrain, container, false);
        // Inflate the layout for this fragment
        /*首页cardview图片相关*/
        ImageView today_img = (ImageView)view.findViewById(R.id.nextRain_img);
        Picasso.with(getActivity())
                .load(R.drawable.ml2)
                .fit()
                .into(today_img);
        return view;
    }

}
