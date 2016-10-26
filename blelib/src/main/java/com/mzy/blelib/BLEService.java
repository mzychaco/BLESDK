package com.mzy.blelib;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

/**
 * Created by mozhenyong on 16/7/14.
 */
public class BLEService extends Service {
    public static final String UNDER_18 = "com.scho.ble.sdk.buildversion_toolow";//android版本过低
    public static final String NOT_SUPPORT = "com.scho.ble.sdk.device_not_supported";//蓝牙设备不支持BLE协议
    public static final String UNOPEN = "com.scho.ble.sdk.unopen";//蓝牙未开启

    private LocalBinder mBinder =new LocalBinder();
    private BluetoothAdapter mBluetoothAdapter;

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT < 18) {
            this.sendBroadcast(new Intent(UNDER_18));
            mBluetoothAdapter=null;
            return;
        }
        if (!this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            this.sendBroadcast(new Intent(NOT_SUPPORT));
            mBluetoothAdapter=null;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) this.getSystemService(Context.BLUETOOTH_SERVICE);
        this.mBluetoothAdapter = bluetoothManager.getAdapter();
        if(!this.getBluetoothAdapter().isEnabled()){
            this.sendBroadcast(new Intent(UNOPEN));
        }


    }

    public class LocalBinder extends Binder {
        public BLEService getService(){
            return BLEService.this;
        }
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public static IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(UNDER_18);
        intentFilter.addAction(NOT_SUPPORT);
        intentFilter.addAction(UNOPEN);
        return intentFilter;
    }
}
