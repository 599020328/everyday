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
public class BestFragment extends Fragment {
    View view;

    public BestFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_best, container, false);
        // Inflate the layout for this fragment
        /*首页cardview图片相关*/
        ImageView today_img = (ImageView)view.findViewById(R.id.best_img1);
        Picasso.with(getActivity())
                .load(R.drawable.todo13)
                .fit()
                .into(today_img);
        ImageView today_img1 = (ImageView)view.findViewById(R.id.best_img2);
        Picasso.with(getActivity())
                .load(R.drawable.todo3)
                .fit()
                .into(today_img1);
        ImageView today_img2 = (ImageView)view.findViewById(R.id.best_img3);
        Picasso.with(getActivity())
                .load(R.drawable.todo1)
                .fit()
                .into(today_img2);
        return view;
    }

}
