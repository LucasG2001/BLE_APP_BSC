package com.example.BLEAPP2;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static com.example.BLEAPP2.GattAttributes.ACCELEROMETER_TIME_READ;
import static com.example.BLEAPP2.GattAttributes.BATTERY_LEVEL_READ;
import static com.example.BLEAPP2.GattAttributes.xCurrent_Read;
import static com.example.BLEAPP2.GattAttributes.yTime_Read;
import static com.example.BLEAPP2.GattAttributes.zGasConc_Read;

/*
 * This is a service to handle the BLE interactions.
 *
 * */
@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName(); //instantiates

    private BluetoothManager bleManager;    //instantiates
    private BluetoothAdapter bleAdapter;    //instantiates
    private String bleDeviceAddress;    //instantiates
    private BluetoothGatt bleGatt;  //instantiates object of type BluetoothGatt
    private BluetoothGattCharacteristic notifyCharacteristics;
    private int connectionState = STATE_DISCONNECTED;
    public List<BluetoothGattCharacteristic> chars = new ArrayList<>(); //Liste aus BLEGATTCharakterestiken "chars"
    /**here we create some Arraylist objects */
    private GasData gasData;
    private ArrayList<Integer> xCurrent = new ArrayList<Integer>();
    private ArrayList<Integer> yTime = new ArrayList<Integer>();
    private ArrayList<Integer> zGasConc = new ArrayList<Integer>();
    private ArrayList<Date> accTime = new ArrayList<Date>();

    private BatteryData batteryData;
    private int batteryLevel;               //field
    private boolean sweepComplete = false;  //field of BluetoothLeService

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_READ_COMPLETED = "ACTION_DATA_READ_COMPLETED";
    public final static String ACTION_BATTERY_LEVEL = "ACTION_BATTERY_LEVEL";
    public final static String ACTION_DATA_AVAILABLE = "ACTION_DATA_AVAILABLE";

    public final static UUID UUID_xCurrent = UUID.fromString(GattAttributes.xCurrent_Measurement);
    public final static UUID UUID_yTime = UUID.fromString(GattAttributes.yTimeMeasurement);
    public final static UUID UUID_zGasConc = UUID.fromString(GattAttributes.zGasConc);
    //public final static UUID UUID_ACCELERATION_TIME = UUID.fromString(GattAttributes.ACCELEROMETER_TIME_MEASUREMENT);

    public final static UUID UUID_BATTERY_LEVEL = UUID.fromString(GattAttributes.BATTERY_LEVEL); //wandle strings um in tatsächliche UUID
    public final static UUID UUID_BATTERY_STATUS = UUID.fromString(GattAttributes.BATTERY_STATUS);

    private boolean running = true;

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectionState = STATE_CONNECTED;

                broadcastUpdate(ACTION_GATT_CONNECTED);
                Log.i(TAG, "Attempting to start service discovery.");
                gatt.discoverServices();
                Log.i(TAG, "Connected to GATT server.");

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(ACTION_GATT_DISCONNECTED);

            } else {
                Log.i(TAG, "Other State");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) { //liest alle möglichen Charakteristiken aus

            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);

                clearDataArrays(); //19.04.2021

                List<BluetoothGattService> services = gatt.getServices();

                // Loops through available GATT Services.
                for (BluetoothGattService gattService : services) {
                    List<BluetoothGattCharacteristic> gattCharacteristicsList = gattService.getCharacteristics();

                    // Loops through available Characteristics.
                    for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristicsList) {
                        if (isDataCharacteristic(gattCharacteristic) != 0) {
                            chars.add(gattCharacteristic); //19.04.2021
                            Log.d("Lucas", "Adding Characteristic");
                            //readCharacteristic(gattCharacteristic);
                            //boolean a = gatt.readCharacteristic(gattCharacteristic);
                            //Log.d("atRead", "reading characteristic = " + a);


                            if (isDataCharacteristic(gattCharacteristic)== xCurrent_Read) {
                                setCharacteristicNotification(gattCharacteristic,true);
                                //gatt.setCharacteristicNotification(gattCharacteristic, true);

                                //DIESE Schleife im CODE ERREICHT ES NIE warum auch immer

                                /**for (BluetoothGattDescriptor bluetoothGattDescriptor : gattCharacteristic.getDescriptors()) {
                                    Log.d("Descriptor","DescriptorAddition");
                                    Log.d("Descriptor", "Characteristic descriptors: " + bluetoothGattDescriptor.getUuid().toString());
                                    bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                    gatt.writeDescriptor(bluetoothGattDescriptor);
                                }*/
                            }
                        }
                    } Log.d("characteristics","charac size = " + chars.size());
                }
                Log.d("Lucas", "OnRequest");
                requestCharacteristics(gatt);


            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }
        @Override
        public void onCharacteristicChanged (BluetoothGatt gatt, BluetoothGattCharacteristic characteristic){
            Log.d("char change","characteristic changed");
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }

        public void requestCharacteristics(BluetoothGatt gatt) {
                boolean a = gatt.readCharacteristic(chars.get(chars.size() - 1));
                Log.d("Lucas", "Reading Characteristic " + a);
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d("Lucas", "OnRead " + characteristic.getUuid().toString());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d("Lucas", "OnGattSuccess");
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                //added 19.04.2021
                //Log.d("Lucas", "Data Available " + characteristic.getUuid() + ", " + characteristic.getValue()); //zeige uns, dass wir angekommen sind, filtere mit Lucas
                //commented this part on 18.04.2021, context: continuous data read
                switch (isDataCharacteristic(characteristic)) { //we effectively say switch(1-9) depending of the characteristic we found
                    case xCurrent_Read:
                    case yTime_Read:
                    case zGasConc_Read:
                    //case ACCELEROMETER_TIME_READ:
                       
                    default:
                        chars.remove(chars.get(chars.size() - 1));
                        Log.d("Lucas", "Default"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
                        break;
                }

                if (chars.size() > 0) {
                    requestCharacteristics(gatt);

                    Log.d("Lucas", "request Data"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
                }
                 else {
                    Log.i(TAG, "Gatt server data read completed.");
                    saveAgmData();
                    Log.d("savedata", "saving data");
                    broadcastUpdate(ACTION_DATA_READ_COMPLETED);
                    Log.d("Lucas", "Data complete");
                    broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
                    gatt.discoverServices();
                    //disconnect();
                }
                //saveAgmData();
                }
            }
    };

    public int isDataCharacteristic(BluetoothGattCharacteristic characteristic) { //hier filtern wir Daten. Wir nehmen alle Services und dann die Daten die wir kennen
        Log.d("Lucas", "isDataCharacteristic ?"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
        if (UUID_BATTERY_LEVEL.equals(characteristic.getUuid())) {
            Log.d("Battery", "BTTRY_READ"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
            return BATTERY_LEVEL_READ; //These return a number between 1 and 9

        } else if (UUID_xCurrent.equals(characteristic.getUuid())) {
            Log.d("Lucas", "X"); //zeige uns, dass wir angekommen sind, filtere mit Lucas//modifiziere
            return xCurrent_Read;

        } else if (UUID_yTime.equals(characteristic.getUuid())) {
            Log.d("Lucas", "Y"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
            return yTime_Read;

        } else if (UUID_zGasConc.equals(characteristic.getUuid())) {
            Log.d("Lucas", "Z"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
            return zGasConc_Read;


        } /**else if (UUID_ACCELERATION_TIME.equals(characteristic.getUuid())) {
           // Log.d("Lucas", "Time"); //zeige uns, dass wir angekommen sind, filtere mit Lucas
            //return ACCELEROMETER_TIME_READ;

        }*/ else {
            return 0;
        }
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
        Log.d("Lucas", "BroadcastUpdate1");
    }

    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        Log.d("Lucas", "broadcastUpdate2");
        final Intent intent = new Intent(action);
        int charWhat = isDataCharacteristic(characteristic);

        switch (charWhat) {
            case BATTERY_LEVEL_READ:
                batteryLevel = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0);
                Log.d(TAG, String.format("Received battery level: %d", batteryLevel));
                intent.putExtra(ACTION_BATTERY_LEVEL, String.valueOf(batteryLevel));

                break;
            case xCurrent_Read:
                //parseInt(string) returns an Int
                //getStringValue(int offset) returns a char array, starting form an offset. This char array is the characteristic value of the string
                xCurrent.set(0,characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0));

                if (!xCurrent.contains(0)) {
                    sweepComplete = true;
                }
                Log.d(TAG, String.format("Received x acceleration level: %d", xCurrent.get(0)));

                break;
            case yTime_Read:
                yTime.set(0,characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0));
                if (!yTime.contains(0)) {
                    sweepComplete = true;
                }

                Log.d(TAG, String.format("Received Body Sensor Location: %d", yTime.get(0)));

                break;
            case zGasConc_Read:
                zGasConc.set(0,characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16,0));
                if (!zGasConc.contains(0)) {
                    sweepComplete = true; //this is just a bool set to false by default
                }
                Log.d(TAG, String.format("Received z acceleration level: %d", zGasConc.get(0)));

                break;

            /**case ACCELEROMETER_TIME_READ:
                /**count = Integer.parseInt(characteristic.getStringValue(0).split(",")[1]);
               accTime.set(count,Integer.parseInt(characteristic.getStringValue(0).split(",")[0]));

                accTime.set(0,new Date());

                if (!accTime.contains(null)) {
                    sweepComplete = true;
                } */

                //break;

            default:
                // For all other profiles, writes the data formatted in HEX.
                final byte[] data = characteristic.getValue();

                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);

                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }
                break;
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private void saveAgmData() {
        gasData = new GasData(xCurrent, yTime, zGasConc, accTime);

        SharedPreferences sharedPref = getSharedPreferences(getString(R.string.agm_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor prefBleDeviceEditor = sharedPref.edit();

        prefBleDeviceEditor.putString("x_acc_avg", String.valueOf(gasData.getCurrentAvg()));
        prefBleDeviceEditor.putString("y_acc_avg", String.valueOf(gasData.getTimeAvg()));
        prefBleDeviceEditor.putString("z_acc_avg", String.valueOf(gasData.getGasConcentrationAvg()));

        /**prefBleDeviceEditor.putString("x_acc_avg", "1");
        prefBleDeviceEditor.putString("y_acc_avg", "2");
        prefBleDeviceEditor.putString("z_acc_avg", "3"); */

        prefBleDeviceEditor.apply();

    }

    private void clearDataArrays() {
        xCurrent = new ArrayList<Integer>();
        yTime = new ArrayList<Integer>();
        zGasConc = new ArrayList<Integer>();
        accTime = new ArrayList<Date>();

        for (int i=0; i < 5; i++) {
            xCurrent.add(i,null);
            yTime.add(i,null);
            zGasConc.add(i,null);
            accTime.add(i,null);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder binder = new LocalBinder();
    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (bleManager == null) {
            bleManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

            if (bleManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        bleAdapter = bleManager.getAdapter();

        if (bleAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }
    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (bleAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (bleDeviceAddress != null && address.equals(bleDeviceAddress)
                && bleGatt != null) {
            Log.d(TAG, "Trying to use an existing bleGatt for connection.");

            if (bleGatt.connect()) {
                connectionState = STATE_CONNECTING;
                Log.d(TAG, "Connecting...");
                return true;

            } else {
                Log.d(TAG, "Can't connect...");
                return false;
            }
        }

        final BluetoothDevice device = bleAdapter.getRemoteDevice(address);

        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }

        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        bleGatt = device.connectGatt(this, false, gattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        bleDeviceAddress = address;
        connectionState = STATE_CONNECTING;
        return true;
    }
    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (bleAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        bleGatt.disconnect();
    }
    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (bleGatt == null) {
            return;
        }
        bleGatt.close();
        bleGatt = null;
    }
    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(final BluetoothGattCharacteristic characteristic) {
        if (bleAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        bleGatt.readCharacteristic(characteristic);
    }
    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (bleAdapter == null || bleGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        bleGatt.setCharacteristicNotification(characteristic, enabled);

        // For only characteristics that are meant to notify
        if (UUID_BATTERY_LEVEL.equals(characteristic.getUuid())
                || UUID_BATTERY_STATUS.equals(characteristic.getUuid()))
        {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(GattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            bleGatt.writeDescriptor(descriptor);
        }
    }
    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (bleGatt == null) return null;
        return bleGatt.getServices();
    }
}