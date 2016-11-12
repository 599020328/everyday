package cc.yfree.yangf.everyday;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.CircleOptions;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.Stroke;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.poi.PoiSortType;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.baidu.mapapi.utils.DistanceUtil;

import java.util.ArrayList;
import java.util.List;

import static cc.yfree.yangf.everyday.R.id.relativeLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class MapActivity extends FragmentActivity implements OnGetPoiSearchResultListener, OnGetSuggestionResultListener {

    private MapView mMapView = null;
    BDLocation location = null;
    MyLocationConfiguration.LocationMode mCurrentMode;
    private  boolean isFirstLoc = true;
    public MyLocationListener mMyLocationListener;
    private LocationClient mLocationClient;
    private int ACCESS_COARSE_LOCATION_REQUEST_CODE = 123;

    private int mXDirection;
    private float mCurrentAccracy;
    private double mCurrentLatitude;
    private double mCurrentLongitude;

    private PoiSearch mPoiSearch = null;
    private SuggestionSearch mSuggeestionSearch = null;
    private BaiduMap mBaiduMap = null;
    private List<String> suggest;

    String nowCity = new String();

    int radius = 500;

    int searchType = 0; //搜索的类型

    private Context context;
    private InfoWindow infoWindow;

    /*
    * 搜索关键字入口
    * */

//    private EditText editCity = null;
    private AutoCompleteTextView keyWorldsView = null;
    private ArrayAdapter<String> sugAdapter = null;
    private int loadIndex = 0;

    private void InitMyLocation() {
        mLocationClient = new LocationClient(this);   //定位服务的客户端
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);   //注册定位监听函数
        LocationClientOption option = new LocationClientOption();        //设置定位方式
        option.setOpenGps(true);   //打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");         //坐标类型
        option.setScanSpan(10000);    //定位间隔
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        mLocationClient.setLocOption(option);
    }

    private void InitOritationListener() {
//        Toast.makeText(MapActivity.this, "aaaaaaaa", Toast.LENGTH_SHORT).show();
        MyOrientationListener myOrientationListener = new MyOrientationListener(
                getApplicationContext());
        myOrientationListener
                .setOnOrientationListner(new OnOrientationListner() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mXDirection = (int) x;

//                        Toast.makeText(MapActivity.this, "sensor"+ x, Toast.LENGTH_SHORT).show();
                        MyLocationData locData = new MyLocationData.Builder()
                                .accuracy(mCurrentAccracy)
                                .direction(mXDirection)
                                .latitude(mCurrentLatitude)
                                .longitude(mCurrentLongitude).build();
                        mBaiduMap.setMyLocationData(locData);
                        BitmapDescriptor mCurrentMarker = BitmapDescriptorFactory
                                .fromResource(R.mipmap.sensor);
                        MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarker);
                        mBaiduMap.setMyLocationConfigeration(config);
                    }
                });
        myOrientationListener.start();
    }

    public class MyLocationListener implements BDLocationListener {             //封装定位结果
        public void onReceiveLocation(BDLocation location) {
            if (location == null || mMapView == null)
            {

                Toast.makeText(MapActivity.this, "Location null", Toast.LENGTH_SHORT).show();
                return ;
            }

//            Toast.makeText(MapActivity.this, "Latitude:" + location.getLatitude()+"    Longitude:"+location.getLongitude() +"\n error code:"+location.getLocType(), Toast.LENGTH_SHORT).show();
            MyLocationData locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())      //设置定位精度
                    .direction(mXDirection).latitude(location.getLatitude())   //方向和纬度
                    .longitude(location.getLongitude()).build();  //经度   构建数据对象
            mBaiduMap.setMyLocationData(locData);      //设置定位数据，必须先允许定位图层
            mCurrentAccracy = location.getRadius();
            mCurrentLatitude = location.getLatitude();
            mCurrentLongitude = location.getLongitude();
//            设置定位图标     定位地点为图标中央
            BitmapDescriptor mCurrentMarket = BitmapDescriptorFactory.fromResource(R.mipmap.sensor);
            MyLocationConfiguration config = new MyLocationConfiguration(mCurrentMode, true, mCurrentMarket);  //图标  是否允许显示方向  定位图层显示方式
            mBaiduMap.setMyLocationConfigeration(config);

            if(isFirstLoc)
            {
                /*
                *  定位地点为图标底部
                * */
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());  //定义坐标点
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(ll, 18);  //设置地图中心和缩放级别，最高21
//                BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.map54);  //设置标注图标
//                OverlayOptions option = new MarkerOptions()
//                        .position(ll)  //设置标注地点
//                        .icon(bitmap); //标注图标
//                MapStatusUpdate mapStatusUpdate1 = MapStatusUpdateFactory.zoomTo(19);
//                mBaiduMap.animateMapStatus(mapStatusUpdate1);
                mBaiduMap.animateMapStatus(mapStatusUpdate); //修改地图
//                mBaiduMap.addOverlay(option);  //增加标注
            }

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_map);
        context = getApplicationContext();


        //初始化搜索模块,注册监听事件
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);

        //初始化建议搜索模块，注册建议搜索事件监听
        mSuggeestionSearch = SuggestionSearch.newInstance();
        mSuggeestionSearch.setOnGetSuggestionResultListener(this);
        nowCity = "湘潭";
        keyWorldsView = (AutoCompleteTextView) findViewById(R.id.map_input);
        sugAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line);
        keyWorldsView.setAdapter(sugAdapter);
        keyWorldsView.setThreshold(1);

        mMapView = (MapView)findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        if(Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_COARSE_LOCATION}, ACCESS_COARSE_LOCATION_REQUEST_CODE);
            }else{
                InitMyLocation();
                InitOritationListener();
            }
        }else{
            InitMyLocation();
            InitOritationListener();
        }


//        String nowCity = BaiuDuUtil.getCity(mCurrentLatitude, mCurrentLongitude);

//        mBaiduMap = ((SupportMapFragment) (getSupportFragmentManager()
//                .findFragmentById(R.id.map))).getBaiduMap();

        /*
        * 当输入关键字变化时，动态更新建议列表
        * */

        final AutoCompleteTextView autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.map_input);

        autoCompleteTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                Drawable drawableLeft = autoCompleteTextView.getCompoundDrawables()[0];
                Drawable drawableRight = autoCompleteTextView.getCompoundDrawables()[2];
                if (drawableLeft == null) {
                    return  false;
                }

                if (event.getAction() != MotionEvent.ACTION_UP) {
                    return false;
                }

                if (event.getX() > autoCompleteTextView.getPaddingLeft() &&
                        event.getX() < autoCompleteTextView.getPaddingLeft() + drawableLeft.getIntrinsicWidth()) {
//                    Toast.makeText(MapActivity.this, "点击了左边", Toast.LENGTH_SHORT).show();
                } else if(event.getX() > autoCompleteTextView.getWidth()-autoCompleteTextView.getPaddingRight()-drawableRight.getIntrinsicWidth() &&
                        event.getX() < autoCompleteTextView.getWidth()){
                    autoCompleteTextView.setText("");
                }
                return false;
            }
        });

        autoCompleteTextView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    searchButtonProcess(v);                  //搜索栏可以有下一组

                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(getWindow().getDecorView().getWindowToken(), 0);
                }
                return false;
            }
        });

        keyWorldsView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() <= 0) {
                    return;
                }

                /*
                * 使用搜索服务获取建议列表，结果在onSuggestionResult()中更新
                * */
                mSuggeestionSearch.requestSuggestion((new SuggestionSearchOption())
//                        .keyword(charSequence.toString()));
                        .keyword(charSequence.toString()).city(nowCity));
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }


    //<****************修改********************************>

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        RelativeLayout la = (RelativeLayout) findViewById(relativeLayout);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(la.getVisibility() == View.VISIBLE) {
                la.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(MapActivity.this, HomeActivity.class);
                startActivity(intent);               //此处出现样式应设置为从左往右滑动！！！！！！！！！
            }
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResult) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResult);
        doNext(requestCode, grantResult);
    }

    private void doNext(int requestCode, int[] grantResults) {
        if(requestCode == ACCESS_COARSE_LOCATION_REQUEST_CODE){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                InitMyLocation();
                InitOritationListener();
            }else{
                super.onDestroy();
            }
        }
    }

    @Override
    protected void onStart() {
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted())
        {
            mLocationClient.start();
        }
        super.onStart();

    }

    @Override
    protected void onStop() {
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mPoiSearch.destroy();
        mSuggeestionSearch.destroy();
        super.onDestroy();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        this.finish();
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    int index = 0;

    /*
    * 响应城市内搜索按钮点击事件
    * */
    public void searchButtonProcess(View v) {
        searchType = 1;
        index = 0;
//        String cityStr = editCity.getText().toString();
        String keyStr = keyWorldsView.getText().toString();
        mPoiSearch.searchInCity(new PoiCitySearchOption().city(nowCity).keyword(keyStr).pageNum(loadIndex));
//        mPoiSearch.searchPoiDetail(new PoiDetailSearchOption().poiUid(nowCity));
//        mPoiSearch.searchInCity(new PoiCitySearchOption().city(cityStr).keyword(keyStr).pageNum(loadIndex));
    }

    LatLng center = new LatLng(mCurrentLatitude, mCurrentLongitude);
    LatLng southwest = new LatLng(mCurrentLatitude-0.01, mCurrentLongitude-0.01);
    LatLng northeast = new LatLng(mCurrentLatitude+0.01, mCurrentLongitude+0.01);
    LatLngBounds searchBound = new LatLngBounds.Builder().include(southwest).include(northeast).build();
    /*
    * 响应周边搜索按钮点击事件
    * */
    public void searchNearByProcess(View v) {
        searchType = 2;

        PoiNearbySearchOption nearbySearchOption = new PoiNearbySearchOption().keyword(keyWorldsView.getText()
                .toString()).sortType(PoiSortType.distance_from_near_to_far).location(center)
                .radius(radius).pageNum(loadIndex);
        mPoiSearch.searchNearby(nearbySearchOption);
    }

    public void goToNextPage(View v) {
        loadIndex++;
        searchButtonProcess(null);
//        searchNearByProcess(null);
//        searchBoundProcess(null);
    }


    /*
    * 响应区域搜索按钮点击事件
    * */
    public void searchBoundProcess(View v) {
        searchType = 3;

        mPoiSearch.searchInBound(new PoiBoundSearchOption().bound(searchBound)
                .keyword(keyWorldsView.getText().toString()));
    }


    /*
    * 获取POI搜索结果 包括searchInCity searchNearBy searchInBound返回的搜索结果
    * */

    @Override
    public void onGetPoiResult(PoiResult poiResult) {
        if (poiResult == null || poiResult.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            Toast.makeText(MapActivity.this, "未找到结果", Toast.LENGTH_SHORT).show();

            return;
        }

        if (poiResult.error == SearchResult.ERRORNO.NO_ERROR) {
            mBaiduMap.clear();
            PoiOverlay poiOverlay = new MyPoiOverlay(mBaiduMap);
            mBaiduMap.setOnMarkerClickListener(poiOverlay);
//            PoiInfo poiInfo = poiResult.getAllPoi().get(index);
//            Toast.makeText(MapActivity.this, poiInfo.name + "    " + poiInfo.address, Toast.LENGTH_SHORT).show();
//            mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
//                @Override
//                public boolean onMarkerClick(Marker marker) {
////                    LatLng la = marker.getPosition();
////                    double lat = la.latitude;           //////**********显示气泡
////                    double lon = la.longitude;
////                    Toast.makeText(MapActivity.this, "点击了标注la:"+lat+"   lon:"+lon+"titititi"+marker.getTitle(), Toast.LENGTH_SHORT).show();
//                    Toast.makeText(MapActivity.this, marker.getTitle(), Toast.LENGTH_SHORT).show();
////                    ShowDetail(marker);
//                    return false;
//                }
//            });
            poiOverlay.setData(poiResult);
            poiOverlay.addToMap();
            index++;
            poiOverlay.zoomToSpan();

            switch ( searchType ) {
                case 2:
                    showNearbyArea(center, radius);
                    break;
                case 3:
                    showBound(searchBound);
                    break;
                default:
                    break;
            }

            return;
        }

        if (poiResult.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            //当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";
            for (CityInfo cityInfo : poiResult.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(MapActivity.this, strInfo, Toast.LENGTH_LONG).show();
        }
    }
//
//    private void ShowDetail(Marker marker) {              /////**********  MapClick
//        LatLng loc = marker.getPosition();
//        GeoCoder geocoder = GeoCoder.newInstance();
//        ReverseGeoCodeOption op = new ReverseGeoCodeOption();
//        op.location(loc);
//        geocoder.reverseGeoCode(op);
//        Toast.makeText(MapActivity.this, "aaaaaaaaaa", Toast.LENGTH_SHORT).show();
//        geocoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
//            @Override
//            public void onGetGeoCodeResult(GeoCodeResult geoCodeResult) {
//
//            }
//
//            @Override
//            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult reverseGeoCodeResult) {
////                reverseGeoCodeResult.getAddress();
//                Toast.makeText(MapActivity.this, "点击了标注"+reverseGeoCodeResult.getAddress(), Toast.LENGTH_SHORT).show();
//                mBaiduMap.hideInfoWindow();
//                ShowItem(reverseGeoCodeResult.getLocation().latitude, reverseGeoCodeResult.getLocation().longitude, reverseGeoCodeResult.getAddress());
//            }
//        });
//    }

    private void ShowItem(double latitude, double longitude, String address) {

    }

    private void showBound(LatLngBounds bounds) {
        BitmapDescriptor bdGround = BitmapDescriptorFactory
                .fromResource(R.mipmap.ground_overlay);

        OverlayOptions ooGround = new GroundOverlayOptions()
                .positionFromBounds(bounds).image(bdGround).transparency(0.8f);
        mBaiduMap.addOverlay(ooGround);

        MapStatusUpdate u = MapStatusUpdateFactory
                .newLatLng(bounds.getCenter());
        mBaiduMap.setMapStatus(u);

        bdGround.recycle();
    }

    private void showNearbyArea(LatLng center, int radius) {
        BitmapDescriptor centerBitmap = BitmapDescriptorFactory
                .fromResource(R.mipmap.icon_geo);
        MarkerOptions ooMarker = new MarkerOptions().position(center).icon(centerBitmap);
        mBaiduMap.addOverlay(ooMarker);

        OverlayOptions ooCircle = new CircleOptions().fillColor(0xCCCCCC00)
                .center(center).stroke(new Stroke(5, 0xFFFF00FF))
                .radius(radius);
        mBaiduMap.addOverlay(ooCircle);
    }

    /*
    * 获取POI详情搜索结果， 得到searchPoiDetail返回的搜索结果
    * */

    @Override
    public void onGetPoiDetailResult(PoiDetailResult poiDetailResult) {
        if (poiDetailResult.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(MapActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MapActivity.this, poiDetailResult.getName() + "：" + poiDetailResult.getAddress(), Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult poiIndoorResult) {

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {
        if (suggestionResult == null || suggestionResult.getAllSuggestions() == null) {
            return;
        }

        suggest = new ArrayList<String>();
        for (SuggestionResult.SuggestionInfo info : suggestionResult.getAllSuggestions()) {
            if (info.key != null) {
                suggest.add(info.key);
            }
        }

        sugAdapter = new ArrayAdapter<String>(MapActivity.this, android.R.layout.simple_dropdown_item_1line, suggest);
        keyWorldsView.setAdapter(sugAdapter);
        sugAdapter.notifyDataSetChanged();
    }


    private class MyPoiOverlay extends PoiOverlay {
        public MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int i) {
            super.onPoiClick(i);
            final PoiInfo poi = getPoiResult().getAllPoi().get(i);

            LatLng pt = poi.location;
            LatLng loc = new LatLng(mCurrentLatitude, mCurrentLongitude);

            int distance = (int) DistanceUtil.getDistance(pt, loc);

            RelativeLayout relativeLayout =(RelativeLayout) findViewById(R.id.relativeLayout);

            TextView textViewName = (TextView) findViewById(R.id.poiName);
            TextView textViewDistance = (TextView) findViewById(R.id.poiDistance);
            TextView textViewDetail = (TextView) findViewById(R.id.poiDetail);

            textViewName.setText(i+1 + "." + poi.name);
            if(distance < 1000) {
                textViewDistance.setText("距您"+distance+"m");
            } else {
                distance = distance / 10;
                textViewDistance.setText("距您"+(float)distance/100+"km");
            }
            if (poi.type == PoiInfo.POITYPE.BUS_STATION) {
                textViewDetail.setText(poi.name);
            } else {
                textViewDetail.setText(poi.address);
            }


//            Toast.makeText(MapActivity.this, poi.type + "   " +PoiInfo.POITYPE.BUS_LINE, Toast.LENGTH_SHORT).show();
            relativeLayout.setVisibility(View.VISIBLE);


            Button toThere = (Button) findViewById(R.id.toThere);
            toThere.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MapActivity.this, RoutePlanningActivity.class);

                    Bundle bundle = new Bundle();
                    bundle.putString("activityName", "MapActivity");
                    bundle.putString("poiName", poi.name);
                    bundle.putDouble("poiLatitude", poi.location.latitude);
                    bundle.putDouble("poiLongitude", poi.location.longitude);
//                    bundle.putString("locName", "");
                    bundle.putDouble("locLatitude", mCurrentLatitude);
                    bundle.putDouble("locLongitude", mCurrentLongitude);


                    intent.putExtras(bundle);
                    startActivity(intent);
                }
            });


//            LatLng pt = poi.location;
//
//            View view = LayoutInflater.from(context).inflate(R.layout.map_search_item, null);
//            TextView textViewName = (TextView) view.findViewById(R.id.poiName);
//            textViewName.setText(poi.name);
//
//            TextView textViewDetail = (TextView) view.findViewById(R.id.poiDetail);
//            textViewDetail.setText(poi.address);
//
//            OnInfoWindowClickListener listener = new OnInfoWindowClickListener() {
//                @Override
//                public void onInfoWindowClick() {
//                    mBaiduMap.hideInfoWindow();
//                }
//            };
//
//            infoWindow = new InfoWindow(view, pt, 1);
//            mBaiduMap.showInfoWindow(infoWindow);

//            Toast.makeText(MapActivity.this, poi.name + "woqunima" +poi.address, Toast.LENGTH_SHORT).show();
//            mPoiSearch.searchPoiDetail((new PoiDetailSearchOption())
//                    .poiUid(poi.uid));

            return true;
        }
    }

}
