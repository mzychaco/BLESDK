package com.mzy.blelib.ibeacon;

import java.math.BigDecimal;

/**
 * Created by wanglei on 2/20/16.
 * iBeacon蓝牙信号解析后的对象。
 */
public class IBeacon implements Comparable<IBeacon> {

    private String name;
    private int major;
    private int minor;
    private String proximityUuid;
    private String bluetoothAddress;
    private int txPower;
    private int rssi;
    private long foundTime;

    public long getFoundTime() {
        return foundTime;
    }

    public void setFoundTime(long foundTime) {
        this.foundTime = foundTime;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public String getProximityUuid() {
        return proximityUuid;
    }

    public void setProximityUuid(String proximityUuid) {
        this.proximityUuid = proximityUuid;
    }

    public String getBluetoothAddress() {
        return bluetoothAddress;
    }

    public void setBluetoothAddress(String bluetoothAddress) {
        this.bluetoothAddress = bluetoothAddress;
    }

    public int getTxPower() {
        return txPower;
    }

    public void setTxPower(int txPower) {
        this.txPower = txPower;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public String toString() {
        StringBuilder str = new StringBuilder(this.getClass().getSimpleName());
        str.append("{");
        str.append("name:").append(this.name).append(",");
        str.append("major:").append(this.major).append(",");
        str.append("minor:").append(this.minor).append(",");
        str.append("proximityUuid:").append(this.proximityUuid).append(",");
        str.append("bluetoothAddress:").append(this.bluetoothAddress).append(",");
        str.append("txPower:").append(this.txPower).append(",");
        str.append("rssi:").append(this.rssi).append(",");
        str.append("found time:").append(this.foundTime).append("");
        str.append("}");
        return str.toString();
    }

    private final static int FACTOR_A = 59;
    private final static double FACTOR_N = 2.0;

    public Double getDistance() {
        return this.getDistance(FACTOR_A, FACTOR_N, 2);
    }

    public Double getDistance(int a, double n, int scale) {
        double rs = Math.abs(this.getRssi());
        rs = (rs - a) / (10 * n);
        return new BigDecimal(Math.pow(10, rs)).setScale(scale, BigDecimal.ROUND_HALF_UP).doubleValue();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) return false;
        if (!(o instanceof IBeacon)) return false;
        if (this.bluetoothAddress == null) return false;
        return this.bluetoothAddress.equals(((IBeacon) o).getBluetoothAddress());
    }

    public IBeacon deepClone(){
        IBeacon lBeacon = new IBeacon();
        lBeacon.setMajor(this.getMajor());
        lBeacon.setBluetoothAddress(this.getBluetoothAddress());
        lBeacon.setMinor(this.getMinor());
        lBeacon.setName(this.getName());
        lBeacon.setProximityUuid(this.getProximityUuid());
        lBeacon.setRssi(this.getRssi());
        lBeacon.setTxPower(this.getTxPower());
        lBeacon.setFoundTime(this.getFoundTime());
        return lBeacon;
    }


    @Override
    public int compareTo(IBeacon another) {
        return this.getRssi() == another.getRssi() ? 0 : this.getRssi() > another.getRssi() ? 1 : -1;
    }
}
