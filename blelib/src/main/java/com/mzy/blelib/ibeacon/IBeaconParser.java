package com.mzy.blelib.ibeacon;

import android.bluetooth.BluetoothDevice;

/**
 * Created by wanglei on 4/24/16.
 * 蓝牙信号解析接口，在LBeaconManager可以改变默认的信号解析处理。
 */
public interface IBeaconParser {
    /**
     * LBeaconManager调用这个方法对获得的蓝牙信号进行解析，并返回信号对象LBeacon。
     * @param device － 蓝牙设备对象
     * @param rssi － 信号强弱参数
     * @param scanData － 蓝牙信号字节流
     * @return － 解析蓝牙信号字节流得到的对象
     */
    IBeacon parse(BluetoothDevice device, int rssi, byte[] scanData);
}
