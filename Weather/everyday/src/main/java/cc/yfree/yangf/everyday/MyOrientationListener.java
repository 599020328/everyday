package cc.yfree.yangf.everyday;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by a on 2016/7/14.
 */
public class MyOrientationListener implements SensorEventListener {

    private Context context;    //抽象基类
    private SensorManager sensorManager;   //管理传感器
    private Sensor sensor;      //传感器对象

    private float lastX;

    private OnOrientationListner onOrientationListner ;

    public MyOrientationListener(Context context) {
        this.context = context;
    }

    public void start() {
        sensorManager = (SensorManager) context
                .getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }

        if (sensor != null) {
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI);   //第三个参数是延迟时间的精度密度
        }
    }

    public void stop() {
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            float x = event.values[SensorManager.DATA_X];

            if (Math.abs(x - lastX) > 1.0) {
                onOrientationListner.onOrientationChanged(x);
            }

            lastX = x;
        }
    }

    public void setOnOrientationListner(OnOrientationListner onOrientationListner) {
        this.onOrientationListner = onOrientationListner ;
    }
}
