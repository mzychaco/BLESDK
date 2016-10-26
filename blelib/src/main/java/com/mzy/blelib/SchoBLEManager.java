package com.mzy.blelib;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;


import com.mzy.blelib.ibeacon.DefaultLBeaconParser;
import com.mzy.blelib.ibeacon.IBeacon;
import com.mzy.blelib.ibeacon.IBeaconConsumer;
import com.mzy.blelib.ibeacon.IBeaconError;
import com.mzy.blelib.ibeacon.IBeaconParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by mozhenyong on 16/9/9.
 */
public class SchoBLEManager {
    private static SchoBLEManager bleManager;
    private static int BLE_SUPPORTED_STATUS = 0;

    private Context mContext;
    private BLEService mService;
    private BLEServiceConnection mServiceConnection;
    private boolean isBind;
    private boolean mScanning;//核心循环的执行标志
    private boolean mStopScan;//是否关闭蓝牙扫描
    private int scanPeriod = 1000;//默认1秒
    private int leaveTime = 8*1000;//默认定义8秒内没有扫描到则视为离开范围
    private Thread mProcessThread;

    private final ConcurrentHashMap<String,IBeacon> beaconsFoundInScanCycle ;

    private Handler mHandler;
    private List<IBeaconConsumer> consumerList = new ArrayList<IBeaconConsumer>(5);
    private IBeaconParser parser = new DefaultLBeaconParser();
    private BluetoothAdapter.LeScanCallback bleScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            IBeacon iBeacon = parser.parse(device, rssi, scanRecord);
            if(iBeacon==null){
                return;
            }
            iBeacon.setFoundTime(System.currentTimeMillis());
            synchronized (consumerList) {
                for (IBeaconConsumer consumer : consumerList) {
                    if(consumer!=null) {
//                        consumer.onIBeaconDetect(iBeacon);
                        if(!beaconsFoundInScanCycle.containsKey(iBeacon.getBluetoothAddress())){
                            consumer.onIBeaconNew(iBeacon.deepClone());
                        }
                    }
                }
            }
            beaconsFoundInScanCycle.put(iBeacon.getBluetoothAddress(),iBeacon);
        }
    };

    private Runnable mCoreScanningRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("SchoBLEManager", "core thread start");
            try {
                Thread.sleep(1500L);//先缓一秒确保能先检测蓝牙设备和更新蓝牙设备更新时间
                while (SchoBLEManager.this.mScanning){
                    long now = System.currentTimeMillis();
                    List<IBeacon> updateList=new ArrayList<>();

                    Set entrySet = beaconsFoundInScanCycle.entrySet();
                    Iterator iterator = entrySet.iterator();
                    while (iterator.hasNext()){
                        ConcurrentHashMap.Entry entry = (ConcurrentHashMap.Entry) iterator.next();
                        IBeacon iBeacon= (IBeacon) entry.getValue();
                        if(now - iBeacon.getFoundTime()>leaveTime){
                            //发现时间超过 视为离开
                            iterator.remove();
                            synchronized (consumerList) {
                                for (IBeaconConsumer consumer : consumerList) {
                                    if(consumer!=null) {
                                        consumer.onIBeaconLeave(iBeacon);
                                    }
                                }
                            }
                        }else{
                            updateList.add(iBeacon);
                        }
                    }
                    Collections.sort(updateList);
                    synchronized (consumerList) {
                        for (IBeaconConsumer consumer : consumerList) {
                            if(consumer!=null) {
                                consumer.onIBeaconUpdate(updateList);
                            }
                        }
                    }
                    Thread.sleep(SchoBLEManager.this.scanPeriod);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Log.d("SchoBLEManager", "core thread stop ");
        }
    };

    private final BroadcastReceiver mBleReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch(action) {
                case BLEService.UNDER_18:
                    SchoBLEManager.BLE_SUPPORTED_STATUS = IBeaconError.BUILD_VERSION_TOO_LOW;
                    Log.e("SchoBLEManager", "android build version is too low ");
                    break;
                case BLEService.NOT_SUPPORT:
                    SchoBLEManager.BLE_SUPPORTED_STATUS = IBeaconError.NOT_SUPPORTED;
                    Log.e("BRTBeaconManager", "Ble not supported by this device ");
                    break;
                case BLEService.UNOPEN:
                    SchoBLEManager.BLE_SUPPORTED_STATUS = IBeaconError.NOT_OPENED;
                    Log.e("BRTBeaconManager", "bluetooth is not opened ");
                    break;

            }

        }
    };

    private SchoBLEManager(Context context){
        this.mContext=context;
        this.beaconsFoundInScanCycle =new ConcurrentHashMap<>();
        this.mServiceConnection=new BLEServiceConnection();
        this.mHandler=new Handler();
    }

    public static SchoBLEManager getInstance(Context context){
        if(bleManager==null){
            bleManager=new SchoBLEManager(context);
        }
        return bleManager;
    }

    public void startBLEService(){
        Intent intent = new Intent(this.mContext,BLEService.class);
        this.mContext.bindService(intent,mServiceConnection, Service.BIND_AUTO_CREATE);
        this.mContext.registerReceiver(this.mBleReceiver, BLEService.getIntentFilter());
    }

    public void stopBLEService(){
        this.stopScanning();
        this.mContext.unregisterReceiver(this.mBleReceiver);
        Intent intent = new Intent(this.mContext,BLEService.class);
        this.mContext.unbindService(mServiceConnection);
        this.mContext.stopService(intent);
    }

    /**
     * 延时500ms，防止startBLEService() 和 startScanning()连续调用导致BLEService绑定未完成
     */
    public void startScanning(){
        mStopScan=false;
        if(mService == null){
            this.mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(mService == null){
                        synchronized (consumerList) {
                            for (IBeaconConsumer consumer : consumerList) {
                                if(consumer!=null) {
                                    consumer.onError(new IBeaconError(IBeaconError.NOT_CONNECTED));
                                }
                            }
                        }
                    }else if(BLE_SUPPORTED_STATUS != 0){
                        synchronized (consumerList) {
                            for (IBeaconConsumer consumer : consumerList) {
                                if(consumer!=null) {
                                    consumer.onError(new IBeaconError(BLE_SUPPORTED_STATUS));
                                }
                            }
                        }
                    }else if(mService.getBluetoothAdapter()!=null){
                        scanBLEDevice(true);
                    }
                }
            },500L);

        }else{
            if(BLE_SUPPORTED_STATUS!=0){
                Log.e("BRTBeaconManager", "扫描失败，Error：BLE_SUPPORTED_STATUS=" + BLE_SUPPORTED_STATUS);
                synchronized (consumerList) {
                    for (IBeaconConsumer consumer : consumerList) {
                        if(consumer!=null) {
                            consumer.onError(new IBeaconError(BLE_SUPPORTED_STATUS));
                        }
                    }
                }
                return;
            }
            scanBLEDevice(true);
        }
    }

    public void stopScanning(){
        mStopScan=true;
        if(mService==null){
            synchronized (consumerList) {
                for (IBeaconConsumer consumer : consumerList) {
                    if(consumer!=null) {
                        consumer.onError(new IBeaconError(IBeaconError.NOT_CONNECTED));
                    }
                }
            }
        }else{
            scanBLEDevice(false);
        }
    }

    private void scanBLEDevice(boolean enable){
        if(mService==null){
            Log.e("SchoBLEManager", "scanBLEDevice:BLEService未开启，请先调用startService方法");
            synchronized (consumerList) {
                for (IBeaconConsumer consumer : consumerList) {
                    if(consumer!=null) {
                        consumer.onError(new IBeaconError(IBeaconError.NOT_CONNECTED));
                    }
                }
            }
        }else{
            if(enable){
                this.mScanning=true;
                startCoreScanning();
                startCoreLoop();
            }else{
                this.mScanning=false;
                stopCoreScanning();
                clearCoreLoop();
            }
        }
    }

    private void startCoreScanning(){
        if(mService.getBluetoothAdapter()!=null && mService.getBluetoothAdapter().isEnabled()) {
            mService.getBluetoothAdapter().startLeScan(bleScanCallback);
        }else{
            synchronized (consumerList) {
                for (IBeaconConsumer consumer : consumerList) {
                    if(consumer!=null) {
                        consumer.onError(new IBeaconError(BLE_SUPPORTED_STATUS));
                    }
                }
            }
        }
    }

    private void stopCoreScanning(){
        if(mService.getBluetoothAdapter()!=null){
            mService.getBluetoothAdapter().stopLeScan(bleScanCallback);
        }
    }

    private void startCoreLoop(){
        this.clearCoreLoop();
        if(this.mProcessThread == null || !this.mProcessThread.isAlive()) {
            this.mProcessThread = new Thread(this.mCoreScanningRunnable);
            this.mProcessThread.start();
        }
    }

    private void clearCoreLoop() {
        if(this.mProcessThread != null && this.mProcessThread.isAlive()) {
            this.mProcessThread = null;
            this.mScanning = false;
        }

    }

    public void bindConsumer(IBeaconConsumer consumer){
        synchronized (this.consumerList) {
            if(consumer!=null){
                this.consumerList.add(consumer);
            }
        }
    }

    public void unbindConsumer(IBeaconConsumer consumer){
        synchronized (this.consumerList) {
            if(consumer!=null){
                this.consumerList.remove(consumer);
            }
        }
    }

    public int getScanPeriod() {
        return scanPeriod;
    }

    /**
     * 信息update间隔
     * @param scanPeriod  单位：ms
     */
    public void setScanPeriod(int scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    public int getLeaveTime() {
        return leaveTime;
    }

    public void setLeaveTime(int leaveTime) {
        this.leaveTime = leaveTime;
    }

    private class BLEServiceConnection implements ServiceConnection {
        private BLEServiceConnection() {
        }

        public void onServiceConnected(ComponentName className, IBinder rawBinder) {
            Log.d("SchoBLEManager", "onServiceConnected");
            SchoBLEManager.this.isBind=true;
            try {
                BLEService.LocalBinder e = (BLEService.LocalBinder)rawBinder;
                SchoBLEManager.this.mService = e.getService();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public void onServiceDisconnected(ComponentName classname) {
            Log.e("BRTBeaconManager", "onServiceDisconnected");
            SchoBLEManager.this.isBind=false;
            SchoBLEManager.this.mService = null;
        }
    }
}
