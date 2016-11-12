package cc.yfree.yangf.everyday;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption.DrivingPolicy;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRoutePlanOption.TransitPolicy;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.navisdk.adapter.BNOuterLogUtil;
import com.baidu.navisdk.adapter.BNOuterTTSPlayerCallback;
import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNaviSettingManager;
import com.baidu.navisdk.adapter.BaiduNaviManager;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by a on 2016/11/6.
 */

public class RoutePlanningActivity extends Activity implements OnClickListener {

    private MapView mapView;
    private BaiduMap bdMap;

    private EditText startEt;
    private EditText endEt;

    private String startPlace;// 开始地点
    private String endPlace;// 结束地点

    private Button driveBtn;// 驾车
    private Button walkBtn;// 步行
    private Button transitBtn;// 换成 （公交）
    private Button naviBtn;

    RelativeLayout relativeLayout;

    LinearLayout linearLayout2;

    LinearLayout linearLayout3;

    private Spinner drivingSpinner, transitSpinner;

    private RoutePlanSearch routePlanSearch;// 路径规划搜索接口

    private int index = -1;
    private int totalLine = 0;// 记录某种搜索出的方案数量
    private int drivintResultIndex = 0;// 驾车路线方案index
    private int transitResultIndex = 0;// 换乘路线方案index

    LatLng location;
    LatLng poi;

    LatLng startNode;
    LatLng endNode;

    Bundle bundle = new Bundle();


    //导航初始化
    public static List<Activity> activityList = new LinkedList<Activity>();

    private static final String APP_FOLDER_NAME = "BNSDKSimpleDemo";

    private Button mWgsNaviBtn = null;
    private Button mGcjNaviBtn = null;
    private Button mBdmcNaviBtn = null;
    private Button mDb06ll = null;
    private String mSDCardPath = null;

    public static final String ROUTE_PLAN_NODE = "routePlanNode";
    public static final String SHOW_CUSTOM_ITEM = "showCustomItem";
    public static final String RESET_END_NODE = "resetEndNode";
    public static final String VOID_MODE = "voidMode";


    String activityName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityList.add(this);
        setContentView(R.layout.activity_route_planning);
        bundle = this.getIntent().getExtras();


        activityName = bundle.getString("activityName");

        init();
        if(activityName.equals("MapActivity")) {
            EditText poiName = (EditText) findViewById(R.id.end_et);
            poiName.setText(bundle.getString("poiName"));


            poi = new LatLng(bundle.getDouble("poiLatitude"), bundle.getDouble("poiLongitude"));

            location = new LatLng(bundle.getDouble("locLatitude"), bundle.getDouble("locLongitude"));  //定义坐标点
            MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(location, 18);  //设置地图中心和缩放级别，最高21
            bdMap.animateMapStatus(mapStatusUpdate);

        } else {
//            Toast.makeText(RoutePlanningActivity.this, "from home", Toast.LENGTH_SHORT).show();
        }
        BNOuterLogUtil.setLogSwitcher(true);

        initListener();
        if (initDirs()) {
            initNavi();
        }

    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            this.finish();
            if(activityName.equals("MapActivity")) {
                Intent intent = new Intent(RoutePlanningActivity.this, MapActivity.class);
                startActivity(intent);              //此处出现样式应设置为从左往右滑动！！！！！！！！！
            } else {
                Intent intent = new Intent(RoutePlanningActivity.this, HomeActivity.class);
                startActivity(intent);               //此处出现样式应设置为从左往右滑动！！！！！！！！！
            }
        }
        return false;
    }

    /**
     *
     */
    private void init() {
        mapView = (MapView) findViewById(R.id.mapview);
        bdMap = mapView.getMap();
        bdMap.setTrafficEnabled(true);

        relativeLayout = (RelativeLayout) findViewById(R.id.walk);
        linearLayout2 = (LinearLayout) findViewById(R.id.result2);
        linearLayout3 = (LinearLayout) findViewById(R.id.result3);

        startEt = (EditText) findViewById(R.id.start_et);
        endEt = (EditText) findViewById(R.id.end_et);
        driveBtn = (Button) findViewById(R.id.drive_btn);
        transitBtn = (Button) findViewById(R.id.transit_btn);
        walkBtn = (Button) findViewById(R.id.walk_btn);
        naviBtn = (Button) findViewById(R.id.navi_btn);
        driveBtn.setOnClickListener(this);
        transitBtn.setOnClickListener(this);
        walkBtn.setOnClickListener(this);

        drivingSpinner = (Spinner) findViewById(R.id.driving_spinner);
        String[] drivingItems = getResources().getStringArray(
                R.array.driving_spinner);
        ArrayAdapter<String> drivingAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, drivingItems);
        drivingSpinner.setAdapter(drivingAdapter);
        drivingSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (index == 0) {
                    drivintResultIndex = 0;
                    drivingSearch(drivintResultIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        transitSpinner = (Spinner) findViewById(R.id.transit_spinner);
        String[] transitItems = getResources().getStringArray(
                R.array.transit_spinner);
        ArrayAdapter<String> transitAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, transitItems);
        transitSpinner.setAdapter(transitAdapter);
        transitSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                if (index == 1) {
                    transitResultIndex = 0;
                    transitSearch(transitResultIndex);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        if(activityName.equals("MapActivity")) {
            drivingSpinner.setVisibility(View.INVISIBLE);
            transitSpinner.setVisibility(View.INVISIBLE);
            startEt.setEnabled(false);
            endEt.setEnabled(false);
        } else {
            drivingSpinner.setVisibility(View.VISIBLE);
            transitSpinner.setVisibility(View.VISIBLE);
            startEt.setEnabled(true);
            endEt.setEnabled(true);
        }

        routePlanSearch = RoutePlanSearch.newInstance();
        routePlanSearch.setOnGetRoutePlanResultListener(routePlanResultListener);


        if (activityName.equals("MapActivity")) {
            index = 0;
            drivintResultIndex = 0;
//                startPlace = startEt.getText().toString();
//                endPlace = endEt.getText().toString();
            driveBtn.setEnabled(false);
            transitBtn.setEnabled(true);
            walkBtn.setEnabled(true);
            naviBtn.setEnabled(true);
            drivingSearch(drivintResultIndex);
        } else {
            relativeLayout.setVisibility(View.GONE);
            linearLayout2.setVisibility(View.GONE);
            linearLayout3.setVisibility(View.GONE);
        }
    }

    /**
     * 路线规划结果回调
     */
    OnGetRoutePlanResultListener routePlanResultListener = new OnGetRoutePlanResultListener() {

        /**
         * 步行路线结果回调
         */
        @Override
        public void onGetWalkingRouteResult(
                WalkingRouteResult walkingRouteResult) {
            bdMap.clear();
            if (walkingRouteResult == null
                    || walkingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // TODO
                return;
            }
            if (walkingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {

                relativeLayout.setVisibility(View.VISIBLE);
                linearLayout2.setVisibility(View.INVISIBLE);
                linearLayout3.setVisibility(View.INVISIBLE);

                TextView title = (TextView) findViewById(R.id.walk_title);
                title.setText("步行方案");

                int second, min, houru, minu;
                float hour;
                TextView time = (TextView) findViewById(R.id.walk_Time);
                second = walkingRouteResult.getRouteLines().get(0).getDuration();
                if(second < 60) {
                    time.setText("需要"+walkingRouteResult.getRouteLines().get(0).getDuration()+"秒");
                } else {
                    min = second/60;
                    if (min < 60) {
                        time.setText("需要"+min+"分钟");
                    } else {
                        hour = min/60;
                        houru = (int) hour*10;
                        minu = houru % 10;
                        houru = houru/10;
                        time.setText("需要"+houru+"."+minu+"小时");
                    }
                }

                int distance;
                TextView distanceV = (TextView) findViewById(R.id.walk_Distance);
                distance = walkingRouteResult.getRouteLines().get(0).getDistance();
                if(distance < 1000) {
                    distanceV.setText("距您"+distance+"m");
                } else {
                    distance = distance / 10;
                    distanceV.setText("距您"+(float)distance/100+"km");
                }

                if (activityName.equals("HomeActivity")) {
                    startNode = walkingRouteResult.getRouteLines().get(0).getStarting().getLocation();
                    endNode = walkingRouteResult.getRouteLines().get(0).getTerminal().getLocation();
                }
                WalkingRouteOverlay walkingRouteOverlay = new WalkingRouteOverlay(bdMap);
                walkingRouteOverlay.setData(walkingRouteResult.getRouteLines().get(0));
                bdMap.setOnMarkerClickListener(walkingRouteOverlay);
                walkingRouteOverlay.addToMap();
                walkingRouteOverlay.zoomToSpan();
//                totalLine = walkingRouteResult.getRouteLines().size();
//                Toast.makeText(RoutePlanningActivity.this,"共查询出" + totalLine + "条符合条件的线路11111", Toast.LENGTH_SHORT).show();
            }
        }

        /**
         * 公交信息
         */
        @Override
        public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {
            bdMap.clear();
            Toast.makeText(RoutePlanningActivity.this, "暂未提供公交路径规划功能", Toast.LENGTH_SHORT).show();
//            if (transitRouteResult == null || transitRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
//                Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
//            }
//            if (transitRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
//                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
//                // drivingRouteResult.getSuggestAddrInfo()
//                return;
//            }
//            if (transitRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
//                TransitRouteOverlay transitRouteOverlay = new TransitRouteOverlay(bdMap);
//                transitRouteOverlay.setData(transitRouteResult.getRouteLines().get(transitResultIndex));// 设置一条驾车路线方案
//
//                bdMap.setOnMarkerClickListener(transitRouteOverlay);
//                transitRouteOverlay.addToMap();
//                transitRouteOverlay.zoomToSpan();
//
//                totalLine = transitRouteResult.getRouteLines().size();
//                Toast.makeText(RoutePlanningActivity.this, transitRouteResult.getRouteLines().get(0).getDuration()+ "   " + totalLine + "条符合条件的线路222", Toast.LENGTH_SHORT).show();
//                // 通过getTaxiInfo()可以得到很多关于打车的信息
//                Toast.makeText(RoutePlanningActivity.this, "该路线打车总路程" + transitRouteResult.getTaxiInfo().getDistance(), Toast.LENGTH_SHORT).show();
//                transitResultIndex++;  //下一条按钮的点击次数不能超过这个数字.否则会抛出异常.
//            }
        }

        /**
         * 驾车路线结果回调 查询的结果可能包括多条驾车路线方案
         */
        @Override
        public void onGetDrivingRouteResult(final DrivingRouteResult drivingRouteResult) {
            bdMap.clear();
            if (drivingRouteResult == null
                    || drivingRouteResult.error != SearchResult.ERRORNO.NO_ERROR) {
                Toast.makeText(RoutePlanningActivity.this, "抱歉，未找到结果",
                        Toast.LENGTH_SHORT).show();
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
                // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
                // drivingRouteResult.getSuggestAddrInfo()
                return;
            }
            if (drivingRouteResult.error == SearchResult.ERRORNO.NO_ERROR) {
                final DrivingRouteOverlay drivingRouteOverlay = new DrivingRouteOverlay(bdMap);

                totalLine = drivingRouteResult.getRouteLines().size();
                if(totalLine == 1) {
                    TextView title = (TextView) findViewById(R.id.walk_title);
                    title.setText("唯一方案");

                    int second, min, houru, minu;
                    float hour;
                    TextView time = (TextView) findViewById(R.id.walk_Time);
                    second = drivingRouteResult.getRouteLines().get(0).getDuration();
                    if(second < 60) {
                        time.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    int distance;
                    TextView distanceV = (TextView) findViewById(R.id.walk_Distance);
                    distance = drivingRouteResult.getRouteLines().get(0).getDistance();
                    if(distance < 1000) {
                        distanceV.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV.setText("距您"+(float)distance/100+"km");
                    }
                    relativeLayout.setVisibility(View.VISIBLE);
                    linearLayout2.setVisibility(View.INVISIBLE);
                    linearLayout3.setVisibility(View.INVISIBLE);
                } else if (totalLine == 2) {
                    TextView title = (TextView) findViewById(R.id.plan1_1_title);
                    title.setText("躲避拥堵");

                    int second, min, houru, minu;
                    float hour;
                    TextView time = (TextView) findViewById(R.id.plan1_1_Time);
                    second = drivingRouteResult.getRouteLines().get(0).getDuration();
                    if(second < 60) {
                        time.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    int distance;
                    TextView distanceV = (TextView) findViewById(R.id.plan1_1_Distance);
                    distance = drivingRouteResult.getRouteLines().get(0).getDistance();
                    if(distance < 1000) {
                        distanceV.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV.setText("距您"+(float)distance/100+"km");
                    }

                    TextView title2 = (TextView) findViewById(R.id.plan2_1_title);
                    title2.setText("方案二");

                    TextView time2 = (TextView) findViewById(R.id.plan2_1_Time);
                    second = drivingRouteResult.getRouteLines().get(1).getDuration();
                    if(second < 60) {
                        time2.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time2.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time2.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    TextView distanceV2 = (TextView) findViewById(R.id.plan2_1_Distance);
                    distance = drivingRouteResult.getRouteLines().get(1).getDistance();
                    if(distance < 1000) {
                        distanceV2.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV2.setText("距您"+(float)distance/100+"km");
                    }
                    relativeLayout.setVisibility(View.INVISIBLE);
                    linearLayout2.setVisibility(View.VISIBLE);
                    linearLayout3.setVisibility(View.INVISIBLE);
                } else if (totalLine >= 3) {
                    TextView title = (TextView) findViewById(R.id.plan1_title);
                    title.setText("躲避拥堵");

                    int second, min, houru, minu;
                    float hour;
                    TextView time = (TextView) findViewById(R.id.plan1_Time);
                    second = drivingRouteResult.getRouteLines().get(0).getDuration();
                    if(second < 60) {
                        time.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    int distance;
                    TextView distanceV = (TextView) findViewById(R.id.plan1_Distance);
                    distance = drivingRouteResult.getRouteLines().get(0).getDistance();
                    if(distance < 1000) {
                        distanceV.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV.setText("距您"+(float)distance/100+"km");
                    }

                    TextView title2 = (TextView) findViewById(R.id.plan2_title);
                    title2.setText("方案二");

                    TextView time2 = (TextView) findViewById(R.id.plan2_Time);
                    second = drivingRouteResult.getRouteLines().get(1).getDuration();
                    if(second < 60) {
                        time2.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time2.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time2.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    TextView distanceV2 = (TextView) findViewById(R.id.plan2_Distance);
                    distance = drivingRouteResult.getRouteLines().get(1).getDistance();
                    if(distance < 1000) {
                        distanceV2.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV2.setText("距您"+(float)distance/100+"km");
                    }


                    TextView title3 = (TextView) findViewById(R.id.plan3_title);
                    title3.setText("方案三");

                    TextView time3 = (TextView) findViewById(R.id.plan3_Time);
                    second = drivingRouteResult.getRouteLines().get(2).getDuration();
                    if(second < 60) {
                        time3.setText("需要"+second+"秒");
                    } else {
                        min = second/60;
                        if (min < 60) {
                            time3.setText("需要"+min+"分钟");
                        } else {
                            hour = min/60;
                            houru = (int) hour*10;
                            minu = houru % 10;
                            houru = houru/10;
                            time3.setText("需要"+houru+"."+minu+"小时");
                        }
                    }

                    TextView distanceV3 = (TextView) findViewById(R.id.plan3_Distance);
                    distance = drivingRouteResult.getRouteLines().get(2).getDistance();
                    if(distance < 1000) {
                        distanceV3.setText("距您"+distance+"m");
                    } else {
                        distance = distance / 10;
                        distanceV3.setText("距您"+(float)distance/100+"km");
                    }

                    relativeLayout.setVisibility(View.INVISIBLE);
                    linearLayout2.setVisibility(View.INVISIBLE);
                    linearLayout3.setVisibility(View.VISIBLE);
                }
//                Toast.makeText(RoutePlanningActivity.this, "dr"+drivintResultIndex+" size"+drivingRouteResult.getRouteLines().size(), Toast.LENGTH_SHORT).show();


                RelativeLayout layout = (RelativeLayout) findViewById(R.id.plan1);
                RelativeLayout layout1 = (RelativeLayout) findViewById(R.id.plan2);
                RelativeLayout layout2 = (RelativeLayout) findViewById(R.id.plan3);

                layout.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) findViewById(R.id.plan1_title);
                        TextView time = (TextView) findViewById(R.id.plan1_Time);
                        TextView distanceV = (TextView) findViewById(R.id.plan1_Distance);

                        TextView title2 = (TextView) findViewById(R.id.plan2_title);
                        TextView time2 = (TextView) findViewById(R.id.plan2_Time);
                        TextView distanceV2 = (TextView) findViewById(R.id.plan2_Distance);

                        TextView title3 = (TextView) findViewById(R.id.plan3_title);
                        TextView time3 = (TextView) findViewById(R.id.plan3_Time);
                        TextView distanceV3 = (TextView) findViewById(R.id.plan3_Distance);

                        title.setTextColor(Color.parseColor("#1b82d2"));
                        time.setTextColor(Color.parseColor("#1b82d2"));
                        distanceV.setTextColor(Color.parseColor("#1b82d2"));

                        title2.setTextColor(Color.parseColor("#757575"));
                        time2.setTextColor(Color.parseColor("#000000"));
                        distanceV2.setTextColor(Color.parseColor("#757575"));

                        title3.setTextColor(Color.parseColor("#757575"));
                        time3.setTextColor(Color.parseColor("#000000"));
                        distanceV3.setTextColor(Color.parseColor("#757575"));

                        drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));
                        bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                    }
                });

                layout1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) findViewById(R.id.plan1_title);
                        TextView time = (TextView) findViewById(R.id.plan1_Time);
                        TextView distanceV = (TextView) findViewById(R.id.plan1_Distance);

                        TextView title2 = (TextView) findViewById(R.id.plan2_title);
                        TextView time2 = (TextView) findViewById(R.id.plan2_Time);
                        TextView distanceV2 = (TextView) findViewById(R.id.plan2_Distance);

                        TextView title3 = (TextView) findViewById(R.id.plan3_title);
                        TextView time3 = (TextView) findViewById(R.id.plan3_Time);
                        TextView distanceV3 = (TextView) findViewById(R.id.plan3_Distance);

                        title2.setTextColor(Color.parseColor("#1b82d2"));
                        time2.setTextColor(Color.parseColor("#1b82d2"));
                        distanceV2.setTextColor(Color.parseColor("#1b82d2"));

                        title.setTextColor(Color.parseColor("#757575"));
                        time.setTextColor(Color.parseColor("#000000"));
                        distanceV.setTextColor(Color.parseColor("#757575"));

                        title3.setTextColor(Color.parseColor("#757575"));
                        time3.setTextColor(Color.parseColor("#000000"));
                        distanceV3.setTextColor(Color.parseColor("#757575"));

                        drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(1));
                        bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                    }
                });

                layout2.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) findViewById(R.id.plan1_title);
                        TextView time = (TextView) findViewById(R.id.plan1_Time);
                        TextView distanceV = (TextView) findViewById(R.id.plan1_Distance);

                        TextView title2 = (TextView) findViewById(R.id.plan2_title);
                        TextView time2 = (TextView) findViewById(R.id.plan2_Time);
                        TextView distanceV2 = (TextView) findViewById(R.id.plan2_Distance);

                        TextView title3 = (TextView) findViewById(R.id.plan3_title);
                        TextView time3 = (TextView) findViewById(R.id.plan3_Time);
                        TextView distanceV3 = (TextView) findViewById(R.id.plan3_Distance);

                        title3.setTextColor(Color.parseColor("#1b82d2"));
                        time3.setTextColor(Color.parseColor("#1b82d2"));
                        distanceV3.setTextColor(Color.parseColor("#1b82d2"));

                        title2.setTextColor(Color.parseColor("#757575"));
                        time2.setTextColor(Color.parseColor("#000000"));
                        distanceV2.setTextColor(Color.parseColor("#757575"));

                        title.setTextColor(Color.parseColor("#757575"));
                        time.setTextColor(Color.parseColor("#000000"));
                        distanceV.setTextColor(Color.parseColor("#757575"));

                        drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(2));
                        bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                    }
                });

                RelativeLayout layout_1 = (RelativeLayout) findViewById(R.id.plan1_1);
                RelativeLayout layout1_1 = (RelativeLayout) findViewById(R.id.plan2_1);

                layout_1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) findViewById(R.id.plan1_1_title);
                        TextView time = (TextView) findViewById(R.id.plan1_1_Time);
                        TextView distanceV = (TextView) findViewById(R.id.plan1_1_Distance);

                        TextView title2 = (TextView) findViewById(R.id.plan2_1_title);
                        TextView time2 = (TextView) findViewById(R.id.plan2_1_Time);
                        TextView distanceV2 = (TextView) findViewById(R.id.plan2_1_Distance);

                        title.setTextColor(Color.parseColor("#1b82d2"));
                        time.setTextColor(Color.parseColor("#1b82d2"));
                        distanceV.setTextColor(Color.parseColor("#1b82d2"));

                        title2.setTextColor(Color.parseColor("#757575"));
                        time2.setTextColor(Color.parseColor("#000000"));
                        distanceV2.setTextColor(Color.parseColor("#757575"));

                        drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));
                        bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                    }
                });

                layout1_1.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        TextView title = (TextView) findViewById(R.id.plan1_1_title);
                        TextView time = (TextView) findViewById(R.id.plan1_1_Time);
                        TextView distanceV = (TextView) findViewById(R.id.plan1_1_Distance);

                        TextView title2 = (TextView) findViewById(R.id.plan2_1_title);
                        TextView time2 = (TextView) findViewById(R.id.plan2_1_Time);
                        TextView distanceV2 = (TextView) findViewById(R.id.plan2_1_Distance);

                        title2.setTextColor(Color.parseColor("#1b82d2"));
                        time2.setTextColor(Color.parseColor("#1b82d2"));
                        distanceV2.setTextColor(Color.parseColor("#1b82d2"));

                        title.setTextColor(Color.parseColor("#757575"));
                        time.setTextColor(Color.parseColor("#000000"));
                        distanceV.setTextColor(Color.parseColor("#757575"));

                        drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(1));
                        bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                        drivingRouteOverlay.addToMap();
                        drivingRouteOverlay.zoomToSpan();
                    }
                });

                TextView title = (TextView) findViewById(R.id.plan1_title);
                TextView time = (TextView) findViewById(R.id.plan1_Time);
                TextView distanceV = (TextView) findViewById(R.id.plan1_Distance);

                TextView title2 = (TextView) findViewById(R.id.plan2_title);
                TextView time2 = (TextView) findViewById(R.id.plan2_Time);
                TextView distanceV2 = (TextView) findViewById(R.id.plan2_Distance);

                TextView title3 = (TextView) findViewById(R.id.plan3_title);
                TextView time3 = (TextView) findViewById(R.id.plan3_Time);
                TextView distanceV3 = (TextView) findViewById(R.id.plan3_Distance);

                title.setTextColor(Color.parseColor("#1b82d2"));
                time.setTextColor(Color.parseColor("#1b82d2"));
                distanceV.setTextColor(Color.parseColor("#1b82d2"));

                title2.setTextColor(Color.parseColor("#757575"));
                time2.setTextColor(Color.parseColor("#000000"));
                distanceV2.setTextColor(Color.parseColor("#757575"));

                title3.setTextColor(Color.parseColor("#757575"));
                time3.setTextColor(Color.parseColor("#000000"));
                distanceV3.setTextColor(Color.parseColor("#757575"));

                TextView title11 = (TextView) findViewById(R.id.plan1_1_title);
                TextView time11 = (TextView) findViewById(R.id.plan1_1_Time);
                TextView distanceV11 = (TextView) findViewById(R.id.plan1_1_Distance);

                TextView title22 = (TextView) findViewById(R.id.plan2_1_title);
                TextView time22 = (TextView) findViewById(R.id.plan2_1_Time);
                TextView distanceV22 = (TextView) findViewById(R.id.plan2_1_Distance);

                title11.setTextColor(Color.parseColor("#1b82d2"));
                time11.setTextColor(Color.parseColor("#1b82d2"));
                distanceV11.setTextColor(Color.parseColor("#1b82d2"));

                title22.setTextColor(Color.parseColor("#757575"));
                time22.setTextColor(Color.parseColor("#000000"));
                distanceV22.setTextColor(Color.parseColor("#757575"));

                if (activityName.equals("HomeActivity")) {
                    startNode = drivingRouteResult.getRouteLines().get(0).getStarting().getLocation();
                    endNode = drivingRouteResult.getRouteLines().get(0).getTerminal().getLocation();
                }

                drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));
                bdMap.setOnMarkerClickListener(drivingRouteOverlay);
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
//                Toast.makeText(RoutePlanningActivity.this, "共查询出" + totalLine + "条符合条件的线路333" , Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onGetBikingRouteResult(BikingRouteResult bikingRouteResult) {

        }
    };

    /**
     * 驾车线路查询 下拉菜单的四个属性：躲避拥堵,最短距离,较少费用,时间优先
     */
    private void drivingSearch(int index) {
        DrivingRoutePlanOption drivingOption = new DrivingRoutePlanOption();
        drivingOption.policy(DrivingPolicy.values()[drivingSpinner.getSelectedItemPosition()]);// 设置驾车路线策略
        if (activityName.equals("MapActivity")) {
            drivingOption.from(PlanNode.withLocation(location));// 设置起点
            drivingOption.to(PlanNode.withLocation(poi));// 设置终点
        } else {
            drivingOption.from(PlanNode.withCityNameAndPlaceName("湘潭",startPlace));
            drivingOption.to(PlanNode.withCityNameAndPlaceName("湘潭",endPlace));
        }
        routePlanSearch.drivingSearch(drivingOption);// 发起驾车路线规划
    }

    /**
     * 换乘路线查询
     */
    private void transitSearch(int index) {
        TransitRoutePlanOption transitOption = new TransitRoutePlanOption();
        transitOption.city("湘潭");// 设置换乘路线规划城市，起终点中的城市将会被忽略
        if (activityName.equals("MapActivity")) {
            transitOption.from(PlanNode.withLocation(location));// 设置起点
            transitOption.to(PlanNode.withLocation(poi));// 设置终点
        } else {
            transitOption.from(PlanNode.withCityNameAndPlaceName("湘潭",startPlace));
            transitOption.to(PlanNode.withCityNameAndPlaceName("湘潭",endPlace));

        }
        transitOption.policy(TransitPolicy.values()[transitSpinner.getSelectedItemPosition()]);// 设置换乘策略
        routePlanSearch.transitSearch(transitOption);
    }

    /**
     * 步行路线查询
     */
    private void walkSearch() {
        WalkingRoutePlanOption walkOption = new WalkingRoutePlanOption();
        if (activityName.equals("MapActivity")) {
            walkOption.from(PlanNode.withLocation(location));// 设置起点
            walkOption.to(PlanNode.withLocation(poi));// 设置终点
        } else {
            walkOption.from(PlanNode.withCityNameAndPlaceName("湘潭",startPlace));
            walkOption.to(PlanNode.withCityNameAndPlaceName("湘潭",endPlace));

        }
        routePlanSearch.walkingSearch(walkOption);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.drive_btn:// 驾车
                index = 0;
                drivintResultIndex = 0;
                if (activityName.equals("HomeActivity")) {
                    startPlace = startEt.getText().toString();
                    endPlace = endEt.getText().toString();
                }
                driveBtn.setEnabled(false);
                transitBtn.setEnabled(true);
                walkBtn.setEnabled(true);
                naviBtn.setEnabled(true);
                drivingSearch(drivintResultIndex);
                break;
            case R.id.transit_btn:// 换乘
                index = 1;
                transitResultIndex = 0;
                if (activityName.equals("HomeActivity")) {
                    startPlace = startEt.getText().toString();
                    endPlace = endEt.getText().toString();
                }
                transitBtn.setEnabled(false);
                driveBtn.setEnabled(true);
                walkBtn.setEnabled(true);
                naviBtn.setEnabled(false);
                transitSearch(transitResultIndex);
                break;
            case R.id.walk_btn:// 步行
                index = 2;
                if (activityName.equals("HomeActivity")) {
                    startPlace = startEt.getText().toString();
                    endPlace = endEt.getText().toString();
                }
                walkBtn.setEnabled(false);
                driveBtn.setEnabled(true);
                transitBtn.setEnabled(true);
                naviBtn.setEnabled(false);
                walkSearch();
                break;

        }
    }

    private void initListener() {
        if (naviBtn != null) {
            naviBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (BaiduNaviManager.isNaviInited()) {
                        routeplanToNavi(BNRoutePlanNode.CoordinateType.BD09LL);
                    }
                }
            });
        }
    }


    private boolean initDirs() {
        mSDCardPath = getSdcardDir();
        if (mSDCardPath == null) {
            return false;
        }
        File f = new File(mSDCardPath, APP_FOLDER_NAME);
        if (!f.exists()) {
            try {
                f.mkdir();
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    String authinfo = null;

    /**
     * 内部TTS播报状态回传handler
     */
    private Handler ttsHandler = new Handler() {
        public void handleMessage(Message msg) {
            int type = msg.what;
            switch (type) {
                case BaiduNaviManager.TTSPlayMsgType.PLAY_START_MSG: {
                    showToastMsg("Handler : TTS play start");
                    break;
                }
                case BaiduNaviManager.TTSPlayMsgType.PLAY_END_MSG: {
                    showToastMsg("Handler : TTS play end");
                    break;
                }
                default :
                    break;
            }
        }
    };

    /**
     * 内部TTS播报状态回调接口
     */
    private BaiduNaviManager.TTSPlayStateListener ttsPlayStateListener = new BaiduNaviManager.TTSPlayStateListener() {

        @Override
        public void playEnd() {
            showToastMsg("TTSPlayStateListener : TTS play end");
        }

        @Override
        public void playStart() {
            showToastMsg("TTSPlayStateListener : TTS play start");
        }
    };

    public void showToastMsg(final String msg) {
        RoutePlanningActivity.this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
//                Toast.makeText(RoutePlanningActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initNavi() {

        BNOuterTTSPlayerCallback ttsCallback = null;

        BaiduNaviManager.getInstance().init(this, mSDCardPath, APP_FOLDER_NAME, new BaiduNaviManager.NaviInitListener() {
            @Override
            public void onAuthResult(int status, String msg) {
                if (0 == status) {
//                    authinfo = "key校验成功!";
                } else {
                    authinfo = "key校验失败, " + msg;
                }
                RoutePlanningActivity.this.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
//                        Toast.makeText(RoutePlanningActivity.this, authinfo, Toast.LENGTH_LONG).show();
                    }
                });
            }

            public void initSuccess() {
                Toast.makeText(RoutePlanningActivity.this, "导航功能暂时只支持驾车导航", Toast.LENGTH_SHORT).show();
                initSetting();
            }

            public void initStart() {
//                Toast.makeText(RoutePlanningActivity.this, "百度导航引擎初始化开始", Toast.LENGTH_SHORT).show();
            }

            public void initFailed() {
                Toast.makeText(RoutePlanningActivity.this, "百度导航引擎初始化失败", Toast.LENGTH_SHORT).show();
            }


        },  null, ttsHandler, ttsPlayStateListener);

    }

    private String getSdcardDir() {
        if (Environment.getExternalStorageState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)) {
            return Environment.getExternalStorageDirectory().toString();
        }
        return null;
    }

    private void routeplanToNavi(BNRoutePlanNode.CoordinateType coType) {
        BNRoutePlanNode sNode = null;
        BNRoutePlanNode eNode = null;
        switch (coType) {
            case GCJ02: {
                sNode = new BNRoutePlanNode(116.30142, 40.05087, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.39750, 39.90882, "北京天安门", null, coType);
                break;
            }
            case WGS84: {
                sNode = new BNRoutePlanNode(116.300821, 40.050969, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(116.397491, 39.908749, "北京天安门", null, coType);
                break;
            }
            case BD09_MC: {
                sNode = new BNRoutePlanNode(12947471, 4846474, "百度大厦", null, coType);
                eNode = new BNRoutePlanNode(12958160, 4825947, "北京天安门", null, coType);
                break;
            }
            case BD09LL: {
                if (activityName.equals("MapActivity")) {
                    sNode = new BNRoutePlanNode(location.longitude, location.latitude, "百度大厦", null, coType);
                    eNode = new BNRoutePlanNode(poi.longitude, poi.latitude, "北京天安门", null, coType);
                    Toast.makeText(RoutePlanningActivity.this, "lon"+poi.longitude+"  la"+poi.latitude, Toast.LENGTH_SHORT).show();
                } else {
                    double start_lon = Double.valueOf(String.format("%.6f",startNode.longitude));
                    double start_la = Double.valueOf(String.format("%.6f",startNode.latitude));
                    double end_lon = Double.valueOf(String.format("%.6f",endNode.longitude));
                    double end_la = Double.valueOf(String.format("%.6f",endNode.latitude));
                    sNode = new BNRoutePlanNode(start_lon, start_la, "百度大厦", null, coType);
                    eNode = new BNRoutePlanNode(end_lon, end_la, "北京天安门", null, coType);
                    Toast.makeText(RoutePlanningActivity.this, "la"+end_la+"  lon"+end_lon, Toast.LENGTH_SHORT).show();
                }
                break;
            }
            default:
                ;
        }
        if (sNode != null && eNode != null) {
            List<BNRoutePlanNode> list = new ArrayList<BNRoutePlanNode>();
            list.add(sNode);
            list.add(eNode);
            BaiduNaviManager.getInstance().launchNavigator(this, list, 1, true, new RoutePlanningActivity.DemoRoutePlanListener(sNode));
        }
    }

    public class DemoRoutePlanListener implements BaiduNaviManager.RoutePlanListener {

        private BNRoutePlanNode mBNRoutePlanNode = null;

        public DemoRoutePlanListener(BNRoutePlanNode node) {
            mBNRoutePlanNode = node;
        }

        @Override
        public void onJumpToNavigator() {
			/*
			 * 设置途径点以及resetEndNode会回调该接口
			 */

            for (Activity ac : activityList) {

                if (ac.getClass().getName().endsWith("BNDemoGuideActivity")) {

                    return;
                }
            }
            Intent intent = new Intent(RoutePlanningActivity.this, BNDemoGuideActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(ROUTE_PLAN_NODE, (BNRoutePlanNode) mBNRoutePlanNode);
            intent.putExtras(bundle);
            startActivity(intent);

        }

        @Override
        public void onRoutePlanFailed() {
            // TODO Auto-generated method stub
            Toast.makeText(RoutePlanningActivity.this, "算路失败", Toast.LENGTH_SHORT).show();
        }
    }

    private void initSetting(){
        // 设置是否双屏显示
        BNaviSettingManager.setShowTotalRoadConditionBar(BNaviSettingManager.PreViewRoadCondition.ROAD_CONDITION_BAR_SHOW_ON);
        // 设置导航播报模式
        BNaviSettingManager.setVoiceMode(BNaviSettingManager.VoiceMode.Veteran);
        // 是否开启路况
        BNaviSettingManager.setRealRoadCondition(BNaviSettingManager.RealRoadCondition.NAVI_ITS_ON);
    }

    private BNOuterTTSPlayerCallback mTTSCallback = new BNOuterTTSPlayerCallback() {

        @Override
        public void stopTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "stopTTS");
        }

        @Override
        public void resumeTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "resumeTTS");
        }

        @Override
        public void releaseTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "releaseTTSPlayer");
        }

        @Override
        public int playTTSText(String speech, int bPreempt) {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "playTTSText" + "_" + speech + "_" + bPreempt);

            return 1;
        }

        @Override
        public void phoneHangUp() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneHangUp");
        }

        @Override
        public void phoneCalling() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "phoneCalling");
        }

        @Override
        public void pauseTTS() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "pauseTTS");
        }

        @Override
        public void initTTSPlayer() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "initTTSPlayer");
        }

        @Override
        public int getTTSState() {
            // TODO Auto-generated method stub
            Log.e("test_TTS", "getTTSState");
            return 1;
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        routePlanSearch.destroy();// 释放检索实例
        mapView.onDestroy();
    }

}
