package com.easy.robust.bluetooth;

/**
 * 自定义蓝牙操作异常
 *
 * @author zhuochangjing
 * @since 2020/6/24
 */
public class BluetoothException extends Exception {

    private int code;

    public BluetoothException(int code) {
        super();
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
