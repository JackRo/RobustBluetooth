package com.easy.robust.bluetooth;

/**
 * BluetoothException
 *
 * @author zhuochangjing
 * @since 2020/6/24
 */
public class BluetoothException extends Exception {

    /**
     * device not support Bluetooth
     */
    public static final int DEVICE_NOT_SUPPORT_BLUETOOTH = 0;

    /**
     * device Bluetooth disabled
     */
    public static final int DEVICE_BLUETOOTH_DISABLED = 1;

    /**
     * Bluetooth connected timeout
     */
    public static final int BLUETOOTH_CONNECTED_TIMEOUT = 2;

    private int code;

    public BluetoothException(int code) {
        super();
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
