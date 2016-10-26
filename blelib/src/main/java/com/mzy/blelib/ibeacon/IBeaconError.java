package com.mzy.blelib.ibeacon;

/**
 * Created by mozhenyong on 16/9/12.
 */
public class IBeaconError {
    public static final int BUILD_VERSION_TOO_LOW = -1;
    public static final int NOT_SUPPORTED = -2;
    public static final int NOT_OPENED = -3;
    public static final int NOT_CONNECTED = -4;

    private int bleSupportedStatus;
    private String msg;

    public IBeaconError(int bleSupportedStatus) {
        this.bleSupportedStatus = bleSupportedStatus;
        switch (bleSupportedStatus){
            case BUILD_VERSION_TOO_LOW:
                this.msg = "android build version is too low ";
                break;
            case NOT_SUPPORTED:
                this.msg = "Ble not supported by this device ";
                break;
            case NOT_OPENED:
                this.msg = "bluetooth is not opened ";
                break;
            case NOT_CONNECTED:
                this.msg = "BLEService is not connected，please startService first, and make sure the BLEService is connected";
                break;
        }
    }

    public IBeaconError(int bleSupportedStatus, String msg) {
        this.bleSupportedStatus = bleSupportedStatus;
        this.msg = msg;
    }

    public int getBleSupportedStatus() {
        return bleSupportedStatus;
    }

    public void setBleSupportedStatus(int bleSupportedStatus) {
        this.bleSupportedStatus = bleSupportedStatus;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
