package cc.yfree.yangf.everyday;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.GroundOverlayOptions;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

import static cc.yfree.yangf.everyday.R.id.relativeLayout;

public class CarAndBus extends AppCompatActivity {

    private MapView mMapView = null;
    BaiduMap  mBaiduMap;
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

    int areaNo;
    int timeNo, weatherNo, temperatureLevel, PM;
    int areaNum[] = new int[67];

    String ipAddress = "115.29.148.31";
    int portNum = 4700;

    private void InitMyLocation() {
        mLocationClient = new LocationClient(this);   //定位服务的客户端
        mMyLocationListener = new MyLocationListener();
        mLocationClient.registerLocationListener(mMyLocationListener);   //注册定位监听函数
        LocationClientOption option = new LocationClientOption();        //设置定位方式
        option.setOpenGps(true);   //打开GPS
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("BD09LL");         //坐标类型
        option.setScanSpan(10000);    //定位间隔
        option.setIsNeedAddress(true);
        option.setNeedDeviceDirect(true);
        mLocationClient.setLocOption(option);
    }

    private void InitOritationListener() {
        MyOrientationListener myOrientationListener = new MyOrientationListener(
                getApplicationContext());
        myOrientationListener
                .setOnOrientationListner(new OnOrientationListner() {
                    @Override
                    public void onOrientationChanged(float x) {
                        mXDirection = (int) x;

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

                Toast.makeText(CarAndBus.this, "Location null", Toast.LENGTH_SHORT).show();
                return ;
            }

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
                areaNo = FindArea();
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());  //定义坐标点
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(ll, 19);  //设置地图中心和缩放级别，最高21
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
        setContentView(R.layout.activity_main);
        mMapView = (MapView)findViewById(R.id.mapView);
        mBaiduMap = mMapView.getMap();
        mBaiduMap.setMyLocationEnabled(true);
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
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


        FindTaxi(areaNo, timeNo, weatherNo, temperatureLevel, PM);
        FloatingActionButton car = (FloatingActionButton) findViewById(R.id.fab2);
        car.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int j : areaNum) {
                    if(j == 0) {
                        //
                    } else {
                        drawAreaInMap(j);
                    }
                }
                LatLng ll = new LatLng(mCurrentLatitude, mCurrentLongitude);
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLngZoom(ll, 14);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
            }
        });

        FloatingActionButton loc = (FloatingActionButton) findViewById(R.id.fab1);
        loc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InitMyLocation();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        RelativeLayout la = (RelativeLayout) findViewById(relativeLayout);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if(la.getVisibility() == View.VISIBLE) {
                la.setVisibility(View.GONE);
            } else {
                Intent intent = new Intent(CarAndBus.this, HomeActivity.class);
                startActivity(intent);               //此处出现样式应设置为从左往右滑动！！！！！！！！！
            }
        }
        return false;
    }

    private void FindTaxi(int areaNo, int timeNo, int weatherNo, int temperatureLevel, int pm) {
        try {
            Socket socket = new Socket(ipAddress, portNum);
            OutputStream os = socket.getOutputStream();
            PrintWriter pw = new PrintWriter(os);
            pw.write(areaNo+"\n");
            pw.write(timeNo+"\n");
            pw.write(weatherNo+"\n");
            pw.write(temperatureLevel+"\n");
            pw.write(pm+"\n");
            pw.flush();
            socket.shutdownOutput();
            InputStream is = socket.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String info = null;
            int result;
            info = br.readLine();
            int i = 0;
            while(info != null) {
                result = Integer.parseInt(info);
//                drawAreaInMap(result);
                areaNum[i] = result;
                i++;
                info = br.readLine();
            }
            if (i < 1) {
                Toast.makeText(CarAndBus.this, "附近没有找到出租车", Toast.LENGTH_SHORT).show();
            }
            br.close();
            is.close();
            pw.close();
            os.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void drawAreaInMap(int result) {
        int drawRow, drawCol;
        drawCol = result % 8;
        if (drawCol == 0) {
            drawCol = 8;
        }
        drawRow = result / 8;
        if (drawCol != 0) {
            drawRow++;
        }
        LatLng pt1 = new LatLng(latS + (drawRow - 1) * addLa, lonS + (drawCol - 1) * addLon);
//        LatLng pt2 = new LatLng(latS + (drawRow - 1) * addLa, lonS + drawCol * addLon);
//        LatLng pt3 = new LatLng(latS + drawRow * addLa, lonS + (drawCol - 1) * addLon);
        LatLng pt4 = new LatLng(latS + drawRow * addLa, lonS + drawCol * addLon);

        LatLngBounds bounds = new LatLngBounds.Builder().include(pt1).include(pt4).build();

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

    double lonS = 112.839149, lonE= 112.962447, latS= 27.840285,latE = 27.928947;
    double addLa = (latE - latS) / 8;
    double addLon = (lonE - lonS) / 8;
    int row,col;

    private int FindArea() {
        if (mCurrentLatitude < latS || mCurrentLatitude >= latE || mCurrentLongitude < lonS || mCurrentLongitude >= lonE) {
            Toast.makeText(CarAndBus.this, "对不起，快速召车暂时只支持湘潭部分地区", Toast.LENGTH_SHORT).show();
            return -1;
        } else {
            col = (int)((mCurrentLatitude - latS) / addLa) + 1;
            row = (int) ((mCurrentLongitude - lonS) / addLon) + 1;
        }
        return (col - 1) * 8 + row;
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
        super.onDestroy();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }
}
