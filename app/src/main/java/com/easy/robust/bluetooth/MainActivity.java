package com.easy.robust.bluetooth;

import android.os.Bundle;
import android.os.SystemClock;
import android.text.Editable;
import android.util.Log;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.nio.charset.Charset;

import easy.robust.bluetooth.BluetoothException;
import easy.robust.bluetooth.BluetoothTransferData;
import easy.robust.bluetooth.RobustBluetooth;

import static easy.robust.bluetooth.BluetoothException.BLUETOOTH_CONNECTED_TIMEOUT;
import static easy.robust.bluetooth.BluetoothException.DEVICE_BLUETOOTH_DISABLED;
import static easy.robust.bluetooth.BluetoothException.DEVICE_NOT_SUPPORT_BLUETOOTH;

/**
 * MainActivity
 *
 * @author zhuochangjing
 */
public class MainActivity extends AppCompatActivity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private EditText macAddressEt;

    private RobustBluetooth mRobustBluetooth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        macAddressEt = findViewById(R.id.mac_address_et);

        /*
        * if you scan the MacAddress's barcode, the barcode may be like 'aabbccddeeff',
        * you could insert colon every 2 chars like below.
        * */
        macAddressEt.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void afterTextChanged(Editable s) {
                String tempMacAddress1 = s.toString();
                String tempMacAddress2 = tempMacAddress1.replaceAll("(.{2})", "$1:");
                String finalMacAddress = tempMacAddress2.substring(0, tempMacAddress2.length() - 1);
                Log.e(TAG, "afterTextChanged finalMacAddress: " + finalMacAddress);

                macAddressEt.removeTextChangedListener(this);
                macAddressEt.setText(finalMacAddress);
                macAddressEt.setSelection(finalMacAddress.length());
                macAddressEt.addTextChangedListener(this);
            }
        });

        findViewById(R.id.print_btn).setOnClickListener(v -> connectAndPrint());
    }

    private void connectAndPrint() {
        try {
            mRobustBluetooth = new RobustBluetooth(macAddressEt.getText().toString());
            /*
             * I used the Zebra ZQ520 Printer,
             * the data below is used by CPCL instruct,
             * you can also used ZPL instruct
             * */
            String data = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
            String charsetName = "GB18030";
            BluetoothTransferData bluetoothTransferData = new BluetoothTransferData();
            bluetoothTransferData.data = data;
            bluetoothTransferData.charset = Charset.forName(charsetName);
            mRobustBluetooth.connectBluetoothDeviceAndTransferDataToIt(bluetoothTransferData,
                    this::connectAndPrintSuccess, this::connectAndPrintError);
        } catch (Exception e) {
            String errorMessage = e.getMessage();
            if (e instanceof BluetoothException) {
                BluetoothException bluetoothException = (BluetoothException) e;
                int code = bluetoothException.getCode();
                switch (code) {
                    case DEVICE_NOT_SUPPORT_BLUETOOTH:
                        errorMessage = "device not support bluetooth";
                        break;
                    case DEVICE_BLUETOOTH_DISABLED:
                        errorMessage = "device bluetooth disabled";
                        break;
                    default:
                        break;
                }
            }
            Log.e(TAG, "initRobustBluetooth：" + errorMessage);
        }
    }

    private void connectAndPrintSuccess(String s) {
        if (RobustBluetooth.SUCCESS.equals(s)) {
            Log.e(TAG, "connectAndPrintSuccess: " + s);
            SystemClock.sleep(1500L);
            mRobustBluetooth.releaseBluetoothResource();
        }
    }

    private void connectAndPrintError(Throwable throwable) {
        String errorMessage = throwable.getMessage();
        if (throwable instanceof BluetoothException) {
            BluetoothException bluetoothException = (BluetoothException) throwable;
            int code = bluetoothException.getCode();
            if (code == BLUETOOTH_CONNECTED_TIMEOUT) {
                errorMessage = "bluetooth connected timeout";
            }
        }
        Log.e(TAG, "connectAndPrint：" + errorMessage);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mRobustBluetooth != null) {
            mRobustBluetooth.releaseBluetoothResource();
        }
    }
}