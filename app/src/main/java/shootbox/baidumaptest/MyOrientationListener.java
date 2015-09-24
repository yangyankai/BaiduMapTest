package shootbox.baidumaptest;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by ykai on 2015/8/25.
 */
public class MyOrientationListener implements SensorEventListener {
    private SensorManager mSensorManager;
    private Context mContext;
    private Sensor mSensor;

    private float lastX;

    public MyOrientationListener(Context context){
        this.mContext=context;

    }

    public void start(){

        mSensorManager= (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager!=null){
            // get location sensor
        mSensor=    mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        }
        if(mSensor!=null){
            mSensorManager.registerListener(this,mSensor,SensorManager.SENSOR_DELAY_UI);
        }
    }

    public void stop(){
        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.getType()==Sensor.TYPE_ORIENTATION);{
            float x=sensorEvent.values[SensorManager.DATA_X];
            if(Math.abs(x-lastX)>1.0){
                if(mOnOrientationListener!=null){
                    mOnOrientationListener.onOrientationChanged(x);
                }

            }
            lastX=x;
        }


    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private OnOrientationListener mOnOrientationListener;

    public void setOnOrientationListener(OnOrientationListener mOnOrientationListener) {
        this.mOnOrientationListener = mOnOrientationListener;
    }


    public interface  OnOrientationListener{
        void onOrientationChanged(float x);
    }
}
