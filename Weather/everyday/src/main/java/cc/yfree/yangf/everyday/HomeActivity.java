package cc.yfree.yangf.everyday;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.view.LayoutInflaterCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ashokvarma.bottomnavigation.BottomNavigationBar;
import com.ashokvarma.bottomnavigation.BottomNavigationItem;
import com.mikepenz.iconics.context.IconicsLayoutInflater;
import com.pkmmte.view.CircularImageView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private String TAG = HomeActivity.class.getSimpleName();
    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //图标支持
        LayoutInflaterCompat.setFactory(getLayoutInflater(), new IconicsLayoutInflater(getDelegate()));
        super.onCreate(savedInstanceState);
        setContentView(R.layout.drawlayout_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        /*设置iconfont*/
//        TextView textview = (TextView)findViewById(R.id.didi);
//        Typeface font = Typeface.createFromAsset(getAssets(), "fonts/iconfont.ttf");
//        textview.setTypeface(font);

        /*抽屉开关*/
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        CircularImageView mCircularImageView = (CircularImageView) findViewById(R.id.headiamage);
        mCircularImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(Gravity.LEFT);
            }
        });

         /*Bottom bar*/
        BottomNavigationBar bottomNavigationBar = (BottomNavigationBar) findViewById(R.id.bottom_navigation_bar);
        //设置模式和风格
        bottomNavigationBar.setMode(BottomNavigationBar.MODE_FIXED);
        bottomNavigationBar.setBackgroundStyle(BottomNavigationBar.BACKGROUND_STYLE_RIPPLE);
        bottomNavigationBar
                .setActiveColor(R.color.white)  //导航栏背景颜色  （RIPPLE模式）
                .setInActiveColor(R.color.gray)      //未被选中的标签的颜色
                .setBarBackgroundColor(R.color.blue); //选中的标签的颜色（RIPPLE模式）

        //设置条目
        bottomNavigationBar
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_home, "主页"))
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_weather, "天气"))//.setActiveColorResource(R.color.color_white))//散开的波纹效果
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_todo, "Todo"))
                .addItem(new BottomNavigationItem(R.drawable.ic_menu_map, "地图"))
                .setFirstSelectedPosition(0)
                .initialise();
        //设置监听器
        bottomNavigationBar.setTabSelectedListener(new BottomNavigationBar.OnTabSelectedListener(){
            public void onTabSelected(int position) {
                Log.d(TAG, "onTabSelected() called with: " + "position = [" + position + "]");
                switch (position) {
                    case 0:
                        break;
                    case 1:
                        Intent intent = new Intent();
                        //Intent传递参数
                        //intent.putExtra("testIntent", "123");
                        intent.setClass(HomeActivity.this, WeatherActivity_.class);
                        startActivity(intent);
                        HomeActivity.this.overridePendingTransition(0, 0);
                        break;
                    case 2:
                        Intent intent2 = new Intent();
                        intent2.setClass(HomeActivity.this, TodoActivity.class);
                        startActivity(intent2);
                        HomeActivity.this.overridePendingTransition(0, 0);
                        break;
                    case 3:
                        Intent intent3 = new Intent();
                        intent3.setClass(HomeActivity.this, MapActivity.class);
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
        //设置默认Fragment
//        setDefaultFragment();


//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        /*设置home-list快速记录*/
//        //RecyclerView的初始化
//        mRecyclerView = (RecyclerView)findViewById(R.id.recycler_view_list);
//        //创建现行LinearLayoutManager
//        mLayoutManager = new LinearLayoutManager(this);
//        //设置LayoutMananger
//        mRecyclerView.setLayoutManager(mLayoutManager);
//        //设置item的动画，可以不设置
////        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
//        MyAdapter adapter = new MyAdapter(initDate());
//        //设置Adapter
//        mRecyclerView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
        return super.onOptionsItemSelected(item);
    }

    /*测试数据*/
    private List<String> initDate(){
        List<String> list = new ArrayList<>();
        for(int i=0;i<5;i++){
            list.add("测试用例：" + i);
        }
        return list;
    }

    class MyAdapter  extends RecyclerView.Adapter<MyAdapter.ViewHolder>{
        private List<String> items;

        public MyAdapter(List<String> items) {
            this.items = items;
        }

        /**
         * 创建ViewHolder的布局
         * @param parent
         * @param viewType
         * @return
         */
        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_home_list,parent,false);
            return new ViewHolder(view);
        }

        /**
         * 通过ViewHolder将数据绑定到界面上进行显示
         * @param holder
         * @param position
         */
        @Override
        public void onBindViewHolder(MyAdapter.ViewHolder holder, int position) {
            holder.mTextView.setText(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder{
            public TextView mTextView;
            public ViewHolder(View itemView) {
                super(itemView);
                mTextView = (TextView) itemView.findViewById(R.id.textView);
            }
        }
    }

}
