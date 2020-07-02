# RobustBluetooth

Robust Android Classic Bluetooth connect and transfer data to Bluetooth device

[ ![Download](https://api.bintray.com/packages/robust-bluetooth/robustBluetooth/robustBluetooth/images/download.svg) ](https://bintray.com/robust-bluetooth/robustBluetooth/robustBluetooth/_latestVersion)

## Usage

1. Add maven repo url


        repositories {
            maven {
                url 'https://dl.bintray.com/robust-bluetooth/robustBluetooth'
            }
        }

2. Add dependency


        implementation 'easy.robust.bluetooth:robustBluetooth:1.0.4'

3. Init RobustBluetooth


        try {
            mRobustBluetooth = new RobustBluetooth("aa:bb:cc:dd:ee:ff");
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
            Log.e("MainActivity", "initRobustBluetooth：" + errorMessage);
        }

4. Connect Bluetooth and transfer data to it


        /*
        * I used the Zebra ZQ520 Printer,
        * the data below is used by CPCL instruct,
        * you could also used ZPL instruct
        * */
        String data = "! 0 200 200 406 1\r\n" + "ON-FEED IGNORE\r\n" + "BOX 20 20 380 380 8\r\n" + "T 0 6 137 177 TEST\r\n" + "PRINT\r\n";
        String charsetName = "GB18030";
        BluetoothTransferData bluetoothTransferData = new BluetoothTransferData();
        bluetoothTransferData.data = data;
        bluetoothTransferData.charset = Charset.forName(charsetName);
        mRobustBluetooth.connectBluetoothDeviceAndTransferDataToIt(bluetoothTransferData,
                this::connectAndPrintSuccess, this::connectAndPrintError);
                
        private void connectAndPrintSuccess(String s) {
            if (RobustBluetooth.SUCCESS.equals(s)) {
                Log.e("MainActivity", "connectAndPrintSuccess: " + s);
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
            Log.e("MainActivity", "connectAndPrint：" + errorMessage);
        }
        
5. Release Bluetooth Resource


        @Override
        protected void onDestroy() {
            super.onDestroy();
            if (mRobustBluetooth != null) {                  
                mRobustBluetooth.releaseBluetoothResource(); 
            }                                                
        }
