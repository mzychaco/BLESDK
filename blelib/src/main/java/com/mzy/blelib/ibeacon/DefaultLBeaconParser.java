package com.mzy.blelib.ibeacon;

import android.bluetooth.BluetoothDevice;

/**
 * 默认的蓝牙信号解析器。
 */
public class DefaultLBeaconParser implements IBeaconParser {
    @Override
    public IBeacon parse(BluetoothDevice device, int rssi, byte[] scanData) {
        return fromScanData(device, rssi, scanData);
    }

    private static IBeacon fromScanData(BluetoothDevice device, int rssi, byte[] scanData) {

        int startByte = 2;
        boolean patternFound = false;
        while (startByte <= 5) {
            if (((int)scanData[startByte+2] & 0xff) == 0x02 &&
                    ((int)scanData[startByte+3] & 0xff) == 0x15) {
                // yes!  This is an ibeacon
                patternFound = true;
                break;
            }
            else if (((int)scanData[startByte] & 0xff) == 0x2d &&
                    ((int)scanData[startByte+1] & 0xff) == 0x24 &&
                    ((int)scanData[startByte+2] & 0xff) == 0xbf &&
                    ((int)scanData[startByte+3] & 0xff) == 0x16) {
                IBeacon ibeacon = new IBeacon();
                ibeacon.setMajor(0);
                ibeacon.setMinor(0);
                ibeacon.setProximityUuid("00000000-0000-0000-0000-000000000000");
                ibeacon.setTxPower(-55);
                return ibeacon;
            }
            else if (((int)scanData[startByte] & 0xff) == 0xad &&
                    ((int)scanData[startByte+1] & 0xff) == 0x77 &&
                    ((int)scanData[startByte+2] & 0xff) == 0x00 &&
                    ((int)scanData[startByte+3] & 0xff) == 0xc6) {

                IBeacon ibeacon = new IBeacon();
                ibeacon.setMajor(0);
                ibeacon.setMinor(0);
                ibeacon.setProximityUuid("00000000-0000-0000-0000-000000000000");
                ibeacon.setTxPower(-55);
                return ibeacon;
            }
            startByte++;
        }


        if (patternFound == false) {
            // This is not an ibeacon
            return null;
        }

        IBeacon ibeacon = new IBeacon();

        ibeacon.setMajor((scanData[startByte + 20] & 0xff) * 0x100 + (scanData[startByte + 21] & 0xff));
        ibeacon.setMinor((scanData[startByte + 22] & 0xff) * 0x100 + (scanData[startByte + 23] & 0xff));
        ibeacon.setTxPower((int) scanData[startByte + 24]); // this one is signed
        ibeacon.setRssi(rssi);

        // AirLocate:
        // 02 01 1a 1a ff 4c 00 02 15  # Apple's fixed ibeacon advertising prefix
        // e2 c5 6d b5 df fb 48 d2 b0 60 d0 f5 a7 10 96 e0 # ibeacon profile uuid
        // 00 00 # major
        // 00 00 # minor
        // c5 # The 2's complement of the calibrated Tx Power

        // Estimote:
        // 02 01 1a 11 07 2d 24 bf 16
        // 394b31ba3f486415ab376e5c0f09457374696d6f7465426561636f6e00000000000000000000000000000000000000000000000000

        byte[] proximityUuidBytes = new byte[16];
        System.arraycopy(scanData, startByte+4, proximityUuidBytes, 0, 16);
        String hexString = bytesToHexString(proximityUuidBytes);
        StringBuilder sb = new StringBuilder();
        sb.append(hexString.substring(0,8));
        sb.append("-");
        sb.append(hexString.substring(8,12));
        sb.append("-");
        sb.append(hexString.substring(12,16));
        sb.append("-");
        sb.append(hexString.substring(16,20));
        sb.append("-");
        sb.append(hexString.substring(20,32));
        ibeacon.setProximityUuid(sb.toString());

        if (device != null) {
            ibeacon.setBluetoothAddress(device.getAddress());
            ibeacon.setName(device.getName());
        }

        return ibeacon;
    }

    private static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

}
