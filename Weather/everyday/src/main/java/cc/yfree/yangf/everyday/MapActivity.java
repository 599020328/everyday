package cc.yfree.yangf.everyday;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.pkmmte.view.CircularImageView;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MapActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    public static Activity MapActivity;

    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
//            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible,isShow;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private EditText mEditText;
    private FloatingActionButton fab1;
    private FloatingActionButton fab2;
    private InputMethodManager inputmanger;
    private View mbottom,floaticon,bottomView;
    private Button mbutton;
    private ObjectAnimator animator;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapActivity = this;
        setContentView(R.layout.drawerlayout_map);

        mVisible = true;
        isShow   = false;
//        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);
        mEditText = (EditText)findViewById(R.id.map_input);
        mEditText.setFocusable(true);

        fab1 = (FloatingActionButton)findViewById(R.id.fab1);
        fab2 = (FloatingActionButton)findViewById(R.id.fab2);
        floaticon = findViewById(R.id.floaticon);
        mbottom = findViewById(R.id.bottom);
        bottomView = findViewById(R.id.bottom_all);
        inputmanger = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        mbutton = (Button)findViewById(R.id.select);




        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        //这是一个奇怪的方法实现drawbleleft的监听
        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //getCompoundDrawables() 可以获取一个长度为4的数组，
                //存放drawableLeft，Right，Top，Bottom四个图片资源对象
                //index=2 表示的是 drawableRight 图片资源对象
                Drawable drawableLeft = mEditText.getCompoundDrawables()[0];
                Drawable drawableRight = mEditText.getCompoundDrawables()[2];
                if (drawableLeft == null)
                    return false;

                if (event.getAction() != MotionEvent.ACTION_UP)
                    return false;

                //drawable.getIntrinsicWidth() 获取drawable资源图片呈现的宽度
                if (event.getX() > mEditText.getPaddingLeft() &&
                        event.getX() < mEditText.getPaddingLeft()+drawableLeft.getIntrinsicWidth() ) {
                    final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                    mEditText.setInputType(InputType.TYPE_NULL);
                    drawer.openDrawer(Gravity.LEFT);
                    inputmanger.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);//隐藏键盘
                }
                else if(event.getX() > mEditText.getPaddingLeft()+drawableLeft.getIntrinsicWidth() &&
                        event.getX() < mEditText.getWidth()-mEditText.getPaddingRight()-drawableRight.getIntrinsicWidth()){
                    mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                    inputmanger.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                    mEditText.requestFocus();//获取焦点
                }
                return false;
            }
        });

        mbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleSelect();
            }
        });


    }

    /*销毁其他activity*/

    @Override
    protected void onResume() {
        super.onResume();
        Activity.onDestory()
        getLocalActivityManager().destroyActivity("string id", true);
    }
    /*默认隐藏*/
//    @Override
//    protected void onPostCreate(Bundle savedInstanceState) {
//        super.onPostCreate(savedInstanceState);
//
//        // Trigger the initial hide() shortly after the activity has been
//        // created, to briefly hint to the user that UI controls
//        // are available.
//        delayedHide(100);
//    }

    /*默认取消焦点,隐藏bottom*/
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mEditText.clearFocus();//失去焦点
    }

    private void toggle() {
        inputmanger.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);//隐藏键盘
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void toggleSelect() {
        if (isShow) {
            WindowManager wm = this.getWindowManager();
            int height = wm.getDefaultDisplay().getHeight();
            float heightChange = height*0.116f;
            animator = ObjectAnimator.ofFloat(bottomView, "translationY",  0);
            animator.setDuration(400);
            animator.start();
            isShow = false;
        } else {
            WindowManager wm = this.getWindowManager();
            int height = wm.getDefaultDisplay().getHeight();
            float heightChange = height*0.116f;
            animator = ObjectAnimator.ofFloat(bottomView, "translationY", -heightChange);
            animator.setDuration(400);
            animator.start();
            if (!mVisible) {
                show();
            }
            isShow = true;
        }
    }

    private void hide() {

//        mControlsView.setVisibility(View.GONE);

        /*设置动画效果*/
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_down_to_up);
        mEditText.startAnimation(animation);
        mEditText.setVisibility(View.GONE);
        animator = ObjectAnimator.ofFloat(floaticon, "alpha", 1f, 0f);
        animator.setDuration(300);
        animator.start();

        if (isShow) {
            WindowManager wm = this.getWindowManager();
            int height = wm.getDefaultDisplay().getHeight();
            float heightChange = height * 0.116f;
            animator = ObjectAnimator.ofFloat(bottomView, "translationY", 0);
            animator.setDuration(400);
            animator.start();
            isShow = false;
        }
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        /*设置动画效果*/
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.animation_up_to_down);
        mEditText.startAnimation(animation);
        mEditText.setVisibility(View.VISIBLE);

        animator = ObjectAnimator.ofFloat(floaticon, "alpha", 0f, 1f);
        animator.setDuration(300);
        animator.start();

//        if (isShow){
//            RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
//                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//            lp1.addRule(RelativeLayout.ABOVE, R.id.bottom);//设置在bottom上方
//            lp1.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);//设置贴紧右边
//            floaticon.setLayoutParams(lp1);//动态改变布局
//        }



        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
