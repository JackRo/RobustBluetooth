package com.easy.robust.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 经典蓝牙连接，写数据
 *
 * @author zhuochangjing
 * @since 2020/6/24
 */
@SuppressWarnings("unused")
public final class RxBluetooth {

    /**
     * 应用所在的设备不支持蓝牙
     */
    public static final int DEVICE_NOT_SUPPORT_BLUETOOTH = 0;

    /**
     * 应用所在的设备蓝牙未开启
     */
    public static final int DEVICE_BLUETOOTH_DISABLED = 1;

    /**
     * 蓝牙连接超时
     */
    public static final int BLUETOOTH_CONNECTED_TIMEOUT = 2;

    /**
     * 成功码
     */
    public static final String SUCCESS = "SUCCESS";

    private static BluetoothSocket mBluetoothSocket;

    private static CompositeDisposable mCompositeDisposable;

    /**
     * 连接蓝牙设备并向蓝牙设备写数据
     *
     * @param bluetoothDeviceMacAddress 蓝牙设备Mac地址
     * @param data                      向蓝牙设备写入的数据
     * @param charsetName               向蓝牙设备写入的数据编码成byte数组需要的编码规则，如：UTF-8，GBK，GB18030
     * @param success                   成功回调
     * @param error                     失败回调
     */
    public static void connectBluetoothDeviceAndWriteDataToIt(String bluetoothDeviceMacAddress, String data, String charsetName,
                                                              Consumer<String> success, Consumer<Throwable> error) {
        addDisposable(toDisposable(Single.just(bluetoothDeviceMacAddress)
                .map(address -> {
                    BluetoothAdapter bluetoothAdapter = getBluetoothAdapter();

                    bluetoothAdapter.cancelDiscovery();

                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    if (mBluetoothSocket == null) {
                        mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    }
                    // 如果已连接蓝牙设备，则关闭蓝牙连接
                    if (mBluetoothSocket.isConnected()) {
                        mBluetoothSocket.close();
                    }

                    long startConnectTime = System.currentTimeMillis();
                    while (!mBluetoothSocket.isConnected()) {
                        LogUtil.e(RxBluetooth.class, "connect::" + "尝试连接");
                        try {
                            mBluetoothSocket.connect();
                        } catch (IOException e) {
                            mBluetoothSocket.connect();
                            LogUtil.e(RxBluetooth.class, "connect::" + e.getMessage());
                        }
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - startConnectTime > 5000) {
                            throw new BluetoothException(BLUETOOTH_CONNECTED_TIMEOUT);
                        }
                    }

                    // 向蓝牙设备写数据
                    OutputStream outputStream = mBluetoothSocket.getOutputStream();

                    // 分包写数据
                    byte[] realWriteData = data.getBytes(charsetName);
                    int start = 0;
                    int dataLen = realWriteData.length;
                    while (start < dataLen) {
                        int len = dataLen - start;
                        if (len > 20) {
                            len = 20;
                        }
                        outputStream.write(realWriteData, start, len);
                        start += len;
                    }

                    return SUCCESS;
                }), success, error));
    }

    private static BluetoothAdapter getBluetoothAdapter() throws BluetoothException {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            throw new BluetoothException(DEVICE_NOT_SUPPORT_BLUETOOTH);
        }
        if (!bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
        if (!bluetoothAdapter.isEnabled()) {
            throw new BluetoothException(DEVICE_BLUETOOTH_DISABLED);
        }
        return bluetoothAdapter;
    }

    private static void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    private static <T> Disposable toDisposable(Single<T> single, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        return single
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError);
    }

    /**
     * 释放蓝牙资源
     */
    public static void releaseBluetoothResource() {
        if (mCompositeDisposable != null && mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
        if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
            try {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            } catch (IOException e) {
                // do nothing
            }
        }
    }
}
