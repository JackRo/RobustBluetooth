package com.easy.robust.bluetooth;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class MainActivity extends Activity {

    EditText macAddressEt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddressEt = findViewById(R.id.mac_address_et);
        findViewById(R.id.print_btn).setOnClickListener(v -> print());
    }

    private void print() {
        String address = macAddressEt.getText().toString();
        String data = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
        String charsetName = "GB18030";
        RxBluetooth.connectBluetoothDeviceAndWriteDataToIt(address, data, charsetName, s -> {
            LogUtil.e(MainActivity.class, s);
        }, throwable -> {
            // LogUtil.e(MainActivity.class, throwable.getMessage());
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        RxBluetooth.releaseBluetoothResource();
    }
}