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

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    List<ScanResult> scanWifiResultList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scanWifi();
        scanBluetooth();

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
    }

    private void scanWifi() {
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
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
