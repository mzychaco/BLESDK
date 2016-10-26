package com.mzy.blesdk;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;


import com.mzy.blelib.SchoBLEManager;
import com.mzy.blelib.ibeacon.IBeacon;
import com.mzy.blelib.ibeacon.IBeaconConsumer;
import com.mzy.blelib.ibeacon.IBeaconError;

import java.util.List;


/**
 * Created by mozhenyong on 16/9/13.
 */
public class NewBLESKDKActivity extends Activity implements IBeaconConsumer {
    private final String TAG=NewBLESKDKActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LinearLayout ll=new LinearLayout(this);
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        ListView listView=new ListView(this);
//        ll.addView(listView,lp);
        TextView tv=new TextView(this);
        tv.setText("dafasdfas");
        ll.addView(tv,lp);
        setContentView(ll);

        SchoBLEManager schoBLEManager=SchoBLEManager.getInstance(this);
        schoBLEManager.bindConsumer(this);
        schoBLEManager.startBLEService();


    }

    @Override
    protected void onResume() {
        super.onResume();
        SchoBLEManager schoBLEManager=SchoBLEManager.getInstance(this);
        schoBLEManager.startScanning();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SchoBLEManager schoBLEManager=SchoBLEManager.getInstance(this);
        schoBLEManager.stopScanning();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SchoBLEManager schoBLEManager=SchoBLEManager.getInstance(this);
        schoBLEManager.unbindConsumer(this);
//        schoBLEManager.setScanPeriod(60*1000);
        schoBLEManager.stopScanning();
        schoBLEManager.stopBLEService();
    }

    @Override
    public void onIBeaconNew(IBeacon iBeacon) {
        Log.d(TAG+" new",iBeacon.toString());
    }

    @Override
    public void onIBeaconUpdate(List<IBeacon> iBeaconList) {
        for (IBeacon c:iBeaconList) {
            Log.d(TAG + " update", c.toString());
        }
        Log.d(TAG + " update", "====================");
    }

    @Override
    public void onIBeaconLeave(IBeacon iBeacon) {
        Log.d(TAG+" leave",iBeacon.toString());
    }

    @Override
    public void onError(IBeaconError error) {
        Log.d(TAG+" error",error.getMsg());
    }
}

class myAdapter extends SchoBaseAdapter<IBeacon>{

    public myAdapter(Context context) {
        super(context);
    }

    @Override
    public void setData(List<IBeacon> list) {

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return null;
    }
}


