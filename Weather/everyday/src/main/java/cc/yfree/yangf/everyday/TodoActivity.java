package cc.yfree.yangf.everyday;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;

import io.karim.MaterialTabs;

public class TodoActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;


    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    public static Activity TodoActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TodoActivity = this;
        setContentView(R.layout.drawlayout_todo);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        //把tabs和viewpager相连
        MaterialTabs tabs = (MaterialTabs) findViewById(R.id.material_tabs);  // Use materialTabs
        tabs.setViewPager(mViewPager);


         /*Bottom bar*/
        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        //设置模式和风格
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
        bottomNavigationBar
                .setActiveColor(R.color.white)  //导航栏背景颜色  （RIPPLE模式）
                .setInActiveColor(R.color.gray)      //未被选中的标签的颜色
                .setBarBackgroundColor(R.color.blue); //选中的标签的颜色（RIPPLE模式）

        /*抽屉*/
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        //设置条目
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_home, "主页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_weather, "天气"))//.setActiveColorResource(R.color.color_white))//散开的波纹效果
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_todo, "Todo"))
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_map, "地图"))
                .setFirstSelectedPosition(2)
                .initialise();
        //设置监听器
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener(){
            public void onTabSelected(int position) {
//                Log.d(TAG, "onTabSelected() called with: " + "position = [" + position + "]");
                switch (position) {
                    case 0:
                        Intent intent = new Intent();
                        intent.setClass(TodoActivity.this, HomeActivity.class);
                        startActivity(intent);
                        TodoActivity.this.overridePendingTransition(0, 0);
                        break;
                    case 1:
                        Intent intent1 = new Intent();
                        //Intent传递参数
                        //intent.putExtra("testIntent", "123");
                        intent1.setClass(TodoActivity.this, WeatherActivity_.class);
                        startActivity(intent1);
                        TodoActivity.this.overridePendingTransition(0, 0);
                        break;
                    case 2:

                        break;
                    case 3:
                        Intent intent3 = new Intent();
                        intent3.setClass(TodoActivity.this, MapActivity.class);
                        startActivity(intent3);
                        break;
                    default:
                        break;
                }

            }
            @Override
            public void onTabUnselected(int position) {
            }
            @Override
            public void onTabReselected(int position) {
            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(TodoActivity.this, AddActivity.class);
                startActivity(intent);
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_todo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_todo, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 12;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "一月";
                case 1:
                    return "二月";
                case 2:
                    return "三月";
                case 3:
                    return "四月";
                case 4:
                    return "五月";
                case 5:
                    return "六月";
                case 6:
                    return "七月";
                case 7:
                    return "八月";
                case 8:
                    return "九月";
                case 9:
                    return "十月";
                case 10:
                    return "十一月";
                case 11:
                    return "十二月";
            }
            return null;
        }
    }
}
