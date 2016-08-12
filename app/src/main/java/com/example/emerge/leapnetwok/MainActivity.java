package com.example.emerge.leapnetwok;

import android.app.PendingIntent;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button mTrackBtn;
    private WifiManager wifiManager;
    List<ScanResult> scanWifiResultList;
    private String WIFI_PREF = "wifi_pref";
    private String BLUETOOTH_PREF = "bluetooth_pref";
    private String GEOFENCE_BROADCAST_ACTION = "GEOFENCE_BROADCAST_ACTION";
    private List<MyBluetoothDevice> mBluetoothDeviceList = new ArrayList<MyBluetoothDevice>();

    class MyBluetoothDevice{
        public  BluetoothDevice mBluetoothDevice;
        public  int mRssi;
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            // 获得已经搜索到的蓝牙设备
            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    Log.d("chenhao", device.getName() + " " + device.getAddress() + " " + rssi);
                    MyBluetoothDevice myBluetoothDevice = new MyBluetoothDevice();
                    myBluetoothDevice.mBluetoothDevice = device;
                    myBluetoothDevice.mRssi = rssi;
                    mBluetoothDeviceList.add(myBluetoothDevice);
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                SharedPreferences.Editor editor = getSharedPreferences(BLUETOOTH_PREF,0).edit();

                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());

                for(MyBluetoothDevice device : mBluetoothDeviceList){
                    editor.putInt(date + " | " + device.mBluetoothDevice.getName() + " | " +
                            device.mBluetoothDevice.getAddress(), device.mRssi);
                }

                editor.commit();

                Toast.makeText(getApplicationContext(), "scan BT done", Toast.LENGTH_SHORT).show();
            }
        }
    };

    private BroadcastReceiver mGeoFenceReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };


    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient = null;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener = new AMapLocationListener() {
        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            if(aMapLocation == null){
                return;
            }
            if(aMapLocation.getErrorCode() == 0){
                Log.d("chenhao",aMapLocation.getLocationType() + " " +
                        aMapLocation.getLatitude() + " " + aMapLocation.getAltitude() + " " +
                        aMapLocation.getAccuracy() + " " + aMapLocation.getPoiName() + " " +aMapLocation.getAoiName()
                        );
            }else{
                Log.d("chenhao", "error code: " + aMapLocation.getErrorCode() + " " + aMapLocation.getErrorInfo());
            }
        }
    };
    //定位的模式和相关参数
    public AMapLocationClientOption mLocationOption = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTrackBtn = (Button)findViewById(R.id.track_id);
        mTrackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
                scanBluetooth();
                //setupLBS();
            }
        });

        // 注册用以接收到已搜索到的蓝牙设备的receiver
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);

        // 注册搜索完时的receiver
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
        unregisterReceiver(mGeoFenceReceiver);

        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    private void setupLBS(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setWifiActiveScan(true);
        mLocationClient.setLocationOption(mLocationOption);

        // Geo Fence
        /*IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction(GEOFENCE_BROADCAST_ACTION);
        registerReceiver(mGeoFenceReceiver,filter);

        Intent intent = new Intent(GEOFENCE_BROADCAST_ACTION);
        PendingIntent pendingIntent = null;
        pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        mLocationClient.addGeoFenceAlert("leap",0.0, 0.0, 100, 1000 * 60, pendingIntent);*/

        //启动定位
        mLocationClient.startLocation();

    }

    private void scanWifi() {
        SharedPreferences.Editor editor = getSharedPreferences(WIFI_PREF, MODE_PRIVATE).edit();

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String date = sDateFormat.format(new java.util.Date());

        scanWifiResultList = wifiManager.getScanResults();
        for (ScanResult scanResult : scanWifiResultList) {
            Log.d("chenhao", scanResult.BSSID + " " + scanResult.SSID + " " + scanResult.level);
            editor.putInt(scanResult.BSSID + " | " +scanResult.SSID + " | "+date, scanResult.level);
        }
        editor.commit();

        Toast.makeText(this, "scan wifi done", Toast.LENGTH_SHORT).show();

    }

    private void scanBluetooth() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) {
            if (!adapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivity(intent);
            }
            adapter.startDiscovery();
        } else {
            Toast.makeText(this, "can't open bluetooth", Toast.LENGTH_SHORT).show();
        }
    }

}
