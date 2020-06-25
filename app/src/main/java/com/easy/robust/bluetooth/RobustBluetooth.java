package com.easy.robust.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import android.text.TextUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

import static com.easy.robust.bluetooth.BluetoothException.BLUETOOTH_CONNECTED_TIMEOUT;
import static com.easy.robust.bluetooth.BluetoothException.DEVICE_BLUETOOTH_DISABLED;
import static com.easy.robust.bluetooth.BluetoothException.DEVICE_NOT_SUPPORT_BLUETOOTH;

/**
 * Classic Bluetooth connect and write data
 *
 * @author zhuochangjing
 * @since 2020/6/24
 */
@SuppressWarnings("unused")
public final class RobustBluetooth {

    /**
     * SUCCESS code
     */
    public static final String SUCCESS = "SUCCESS";

    private String mBluetoothDeviceMacAddress;

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothSocket mBluetoothSocket;

    private CompositeDisposable mCompositeDisposable;

    public RobustBluetooth(String bluetoothDeviceMacAddress) throws Exception {
        if (TextUtils.isEmpty(bluetoothDeviceMacAddress)) {
            throw new IllegalArgumentException("bluetoothDeviceMacAddress is not allowed Empty");
        }
        mBluetoothDeviceMacAddress = bluetoothDeviceMacAddress;
        mBluetoothAdapter = getBluetoothAdapter();
    }

    /**
     * Connect Bluetooth device and transfer data to it
     *
     * @param transferData the transferred data
     * @param success      success callback
     * @param error        error callback
     */
    public void connectBluetoothDeviceAndTransferDataToIt(BluetoothTransferData transferData,
                                                          Consumer<String> success, Consumer<Throwable> error) {
        addDisposable(toDisposable(Single.just(transferData.data)
                .map(writeData -> {
                    mBluetoothAdapter.cancelDiscovery();

                    BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(mBluetoothDeviceMacAddress);
                    UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                    mBluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);

                    // try to connect 2s
                    int times = 10;
                    do {
                        if (times < 1) {
                            throw new BluetoothException(BLUETOOTH_CONNECTED_TIMEOUT);
                        }
                        try {
                            mBluetoothSocket.connect();
                            SystemClock.sleep(200);
                        } catch (Exception e) {
                            // do nothing
                        } finally {
                            times--;
                        }
                    } while ((!mBluetoothSocket.isConnected()));

                    // chunked transfer data
                    OutputStream outputStream = mBluetoothSocket.getOutputStream();
                    byte[] realWriteData = writeData.getBytes(transferData.charset);
                    int start = 0;
                    int dataLen = realWriteData.length;
                    while (start < dataLen) {
                        int len = dataLen - start;
                        if (len > 20) {
                            len = 20;
                        }
                        outputStream.write(realWriteData, start, len);
                        outputStream.flush();
                        start += len;
                    }

                    closeQuietly(outputStream);
                    closeQuietly(mBluetoothSocket);
                    mBluetoothSocket = null;
                    return SUCCESS;
                }), success, error));
    }

    /**
     * Release Bluetooth resource
     */
    public void releaseBluetoothResource() {
        if (mCompositeDisposable != null && mCompositeDisposable.size() > 0) {
            mCompositeDisposable.clear();
        }
        if (mBluetoothSocket != null && mBluetoothSocket.isConnected()) {
            closeQuietly(mBluetoothSocket);
            mBluetoothSocket = null;
        }
    }

    private BluetoothAdapter getBluetoothAdapter() throws BluetoothException {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            throw new BluetoothException(DEVICE_NOT_SUPPORT_BLUETOOTH);
        }
        if (!mBluetoothAdapter.isEnabled()) {
            mBluetoothAdapter.enable();
        }
        if (!mBluetoothAdapter.isEnabled()) {
            throw new BluetoothException(DEVICE_BLUETOOTH_DISABLED);
        }
        return mBluetoothAdapter;
    }

    private void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    private static <T> Disposable toDisposable(Single<T> single, Consumer<T> onSuccess, Consumer<Throwable> onError) {
        return single.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onSuccess, onError);
    }

    private void closeQuietly(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            // do nothing
        }
    }
}
