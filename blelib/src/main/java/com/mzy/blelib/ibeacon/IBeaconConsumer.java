package com.mzy.blelib.ibeacon;

import java.util.List;

/**
 * Created by mozhenyong on 3/20/16.
 * LBeacon信号的消费者，需要调用LBeaconManager.bind方法绑定。绑定后，每次LBeaconManager检测到蓝牙信号，会回调onLBeaconDetect方法。
 */
public interface IBeaconConsumer {

    /**
     * 每当LBeaconManager检测到有iBeacon信号，会调用LBeaconConsumer的这个方法。
     * @param iBeacon － LBeaconParser解析蓝牙信号得到的LBeacon信号对象
     */
    //void onIBeaconDetect(IBeacon iBeacon);

    void onIBeaconNew(IBeacon iBeacon);

    void onIBeaconUpdate(List<IBeacon> iBeaconList);

    void onIBeaconLeave(IBeacon iBeacon);

    void onError(IBeaconError error);

}
