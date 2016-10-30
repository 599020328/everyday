package cc.yfree.yangf.everyday;


import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class BusFragment extends Fragment {
    private FloatingActionButton fab;
    private EditText mEditText;
    private ObjectAnimator animator;
    private View view,mContentView;
    private boolean mVisible;


    public BusFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_bus, null, false);
        mEditText = (EditText)view.findViewById(R.id.map_input);
        fab = (FloatingActionButton)view.findViewById(R.id.fab2);
        mContentView = view.findViewById(R.id.fullscreen_content);
        mVisible = true;

        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WindowManager wm = getActivity().getWindowManager();
                int height = wm.getDefaultDisplay().getHeight();
                float heightChange = height * 0.11f;
                animator = ObjectAnimator.ofFloat(mEditText, "translationY", heightChange);
                animator.setDuration(400);
                animator.start();
                Log.d("show","show editview");
            }
        });
        return view;
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        if (mVisible) {
            /*设置动画效果*/
            animator = ObjectAnimator.ofFloat(mEditText, "translationY", 0);
            animator.setDuration(400);
            animator.start();
        }
        mVisible = false;
    }

    @SuppressLint("InlinedApi")
    private void show() {
        if (mVisible){
            /*设置动画效果*/
            WindowManager wm = getActivity().getWindowManager();
            int height = wm.getDefaultDisplay().getHeight();
            float heightChange = height * 0.11f;
            animator = ObjectAnimator.ofFloat(mEditText, "translationY", heightChange);
            animator.setDuration(400);
            animator.start();
        }
        mVisible = true;
    }
}
