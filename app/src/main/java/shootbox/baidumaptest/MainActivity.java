package shootbox.baidumaptest;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMapClickListener;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationConfiguration.LocationMode;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;


public class MainActivity extends Activity {
    private MapView mMapView = null;
    private BaiduMap mBaiduMap;


    private Context context;
    //location
    private LocationClient mLocationClient;
    private MyLocationListener myLocationListener;
    private boolean isFirstIn=true;
    private double mLatitude;
    private double mLongtitude;
    // update location icon
    private BitmapDescriptor mIconLocation;
    private MyOrientationListener myOrientationListener;
    private float mCurrentX;
    private MyLocationConfiguration.LocationMode mLocationMode;
    // marker
    private BitmapDescriptor mMarker;
    private RelativeLayout mMarkerLy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //在使用SDK各组件之前初始化context信息，传入ApplicationContext
        //注意该方法要再setContentView方法之前实现
        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        this.context=this;
        initView();
        initLocation();
        initMarker();
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                Bundle extraInfo = marker.getExtraInfo();
                Info info = (Info) extraInfo.getSerializable("info");
                ImageView iv = (ImageView) mMarkerLy.findViewById(R.id.id_info_img);
                TextView distance = (TextView) mMarkerLy.findViewById(R.id.id_info_distance);
                TextView zan = (TextView) mMarkerLy.findViewById(R.id.id_info_zan);
                TextView name = (TextView) mMarkerLy.findViewById(R.id.id_info_name);

                iv.setImageResource(info.getImgId());
                distance.setText(info.getDistance());
                name.setText(info.getName());
                zan.setText(info.getZan()+"");

                InfoWindow infoWindow;
                TextView tv = new TextView(context);
                tv.setBackgroundResource(R.drawable.location_tips);
                tv.setPadding(30, 20, 30, 50);
                tv.setText(info.getName());
                tv.setTextColor(Color.parseColor("#ffffff"));

                final LatLng latLng = marker.getPosition();
                Point p = mBaiduMap.getProjection().toScreenLocation(latLng);
                p.y -= 47;
                LatLng ll = mBaiduMap.getProjection().fromScreenLocation(p);

                BitmapDescriptor lobd = BitmapDescriptorFactory.fromView(tv);
                infoWindow = new InfoWindow(lobd, ll,0,
                        new OnInfoWindowClickListener()
                        {
                            @Override
                            public void onInfoWindowClick()
                            {
                                mBaiduMap.hideInfoWindow();
                            }
                        });
                mBaiduMap.showInfoWindow(infoWindow);
                mMarkerLy.setVisibility(View.VISIBLE);
                return true;
            }
        });
        mBaiduMap.setOnMapClickListener(new BaiduMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMarkerLy.setVisibility(View.GONE);
                mBaiduMap.hideInfoWindow();

            }

            @Override
            public boolean onMapPoiClick(MapPoi mapPoi) {

                return false;
            }
        });

    }

    private void initMarker() {
        mMarker=BitmapDescriptorFactory.fromResource(R.drawable.maker);
        mMarkerLy= (RelativeLayout) findViewById(R.id.id_marker_layout);
    }

    private void initLocation() {
    //    mLocationMode
        mLocationClient=new LocationClient(this);
        myLocationListener=new MyLocationListener();
        mLocationClient.registerLocationListener(myLocationListener);

        LocationClientOption option=new LocationClientOption();
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);
        option.setOpenGps(true);
        option.setScanSpan(1000);
        mLocationClient.setLocOption(option);

        // init icon
      //  mIconLocation= BitmapDescriptorFactory.fromResource(R.drawable.navi_map_gps_locked2);
        mIconLocation= BitmapDescriptorFactory.fromResource(R.drawable.navi);
        myOrientationListener=new MyOrientationListener(context);
        myOrientationListener.setOnOrientationListener(new MyOrientationListener.OnOrientationListener() {
            @Override
            public void onOrientationChanged(float x) {
                mCurrentX=x;
            }
        });
    }

    private void initView() {
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap=mMapView.getMap();
        MapStatusUpdate msu= MapStatusUpdateFactory.zoomTo(15.0f);
        mBaiduMap.setMapStatus(msu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        // 开启定位
        mBaiduMap.setMyLocationEnabled(true);
        if(!mLocationClient.isStarted())
        mLocationClient.start();
        //open Orientaion
        myOrientationListener.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }
    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }
    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 关闭定位
        mBaiduMap.setMyLocationEnabled(false);
        mLocationClient.stop();
        // close Orientation
        myOrientationListener.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case R.id.id_map_common:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
                break;
            case R.id.id_map_site:
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.id_map_traffic:
                if(!mBaiduMap.isTrafficEnabled())
                {
                    mBaiduMap.setTrafficEnabled(true);
                    item.setTitle("实时交通 off");
                }
                else {

                    mBaiduMap.setTrafficEnabled(false);
                    item.setTitle("实时交通 on");
                }
                break;
            case R.id.id_map_location:
                centerToMyLocation();
                break;
            case R.id.id_map_mode_common:
                mLocationMode= MyLocationConfiguration.LocationMode.NORMAL;
                break;
            case R.id.id_map_mode_following:
                mLocationMode= MyLocationConfiguration.LocationMode.FOLLOWING;
                break;
            case R.id.id_map_mode_compass:
                mLocationMode=MyLocationConfiguration.LocationMode.COMPASS;
                break;
            case R.id.id_add_overlay:
                addOverlays(Info.infos);
                break;
            default:
                break;

        }

        return true;
    }

    private void centerToMyLocation() {
        LatLng latLng=new LatLng(mLatitude,mLongtitude);
        MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.animateMapStatus(msu);
    }

    private void addOverlays(List<Info> infos) {
        mBaiduMap.clear();
        LatLng latLng=null;
        Marker marker=null;
        OverlayOptions options;
        for(Info info:infos){
            //经纬度
            latLng=new LatLng(info.getLatitude(),info.getLangtitude());
            //icon
            options=new MarkerOptions().position(latLng).icon(mMarker).zIndex(5);
            marker= (Marker) mBaiduMap.addOverlay(options);
            Bundle arg0=new Bundle();
            arg0.putSerializable("info",info);
            marker.setExtraInfo(arg0);

        }
        MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);
        mBaiduMap.setMapStatus(msu);
    }

    private class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            MyLocationData data=new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(mCurrentX)
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(data);
//            MyLocationConfiguration configuration
            //change  location  icon
     //       MyLocationConfiguration configuration=new MyLocationConfiguration(mLocationMode,true,mIconLocation);

            MyLocationConfiguration configuration=new MyLocationConfiguration(mLocationMode,true,mIconLocation);

            mBaiduMap.setMyLocationConfigeration(configuration);
            mLatitude=location.getLatitude();
            mLongtitude=location.getLongitude();
            if(isFirstIn){
                LatLng latLng=new LatLng(location.getLatitude(),location.getLongitude());
                MapStatusUpdate msu=MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(msu);
                isFirstIn=false;
                Toast.makeText(context,"我的位置是："+location.getAddrStr(),Toast.LENGTH_SHORT).show();
            }

        }
    }
}
