package com.example.emerge.leapnetwok;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;


import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    List<ScanResult> scanWifiResultList;

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

        scanWifi();
        scanBluetooth();
        setupLBS();

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

        mLocationClient.stopLocation();
        mLocationClient.onDestroy();
    }

    private void setupLBS(){
        //初始化定位
        mLocationClient = new AMapLocationClient(getApplicationContext());
        //设置定位回调监听
        mLocationClient.setLocationListener(mLocationListener);

        mLocationOption = new AMapLocationClientOption();
        //设置定位模式为AMapLocationMode.Hight_Accuracy，高精度模式。
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setOnceLocationLatest(true);
        mLocationOption.setWifiActiveScan(true);

        //给定位客户端对象设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        //启动定位
        mLocationClient.startLocation();

    }

    private void scanWifi() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        scanWifiResultList = wifiManager.getScanResults();

        Log.d("chenhao", "total: " + scanWifiResultList.size());
        for (ScanResult scanResult : scanWifiResultList) {
            Log.d("chenhao", scanResult.BSSID + " " + scanResult.SSID + " " + scanResult.level);
        }

        Log.d("chenhao", "--------------------------");

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
                }
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                //Log.d("chenhao","no bluetooth found");
            }
        }
    };


}
