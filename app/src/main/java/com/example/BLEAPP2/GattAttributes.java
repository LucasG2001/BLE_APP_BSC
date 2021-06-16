package com.example.BLEAPP2;

import java.util.HashMap;

/*
 * This is where you keep all your UUIDs and some other items to differentiate BLE services
 * and characteristics.
 *
 * */
// UUID's ändern
public class GattAttributes {
    private static HashMap<String, String> attributes = new HashMap(); //Map ist Zuweisung, Hashmap aus Library
    //Base UUID: 0000xxxx-0000-1000-8000-00805F9B34FB

    public static String GENERIC_ACCESS = "00001800-0000-1000-8000-00805F9B34FB";
    public static String GENERIC_ATTRIBUTE = "00001801-0000-1000-8000-00805F9B34FB";
    public static String DEVICE_INFORMATION_SERVICE = "0000180A-0000-1000-8000-00805F9B34FB"; //(Name)

    public static String BATTERY_SERVICE = "0000180F-0000-1000-8000-00805F9B34FB";
    public static String BATTERY_LEVEL = "00002a19-0000-1000-8000-00805f9b34fb";
    public static String BATTERY_STATUS = "00002a1b-0000-1000-8000-00805f9b34fb";

    public static String GAS_DATA_SERVICE = "0000181A-0000-1000-8000-00805F9B34FB";
    public static String xCurrent_Measurement = "00002AE0-0000-1000-8000-00805F9B34FB";
    public static String yTimeMeasurement = "00002A59-0000-1000-8000-00805F9B34FB";
    public static String zGasConc = "00002A58-0000-1000-8000-00805F9B34FB";
    public static String ACCELEROMETER_TIME_MEASUREMENT = "00000038-627e-47e5-a3fc-ddabd97aa966";

    public static String DEVICE_NAME = "00002a00-0000-1000-8000-00805f9b34fb";
    public static String APPEARANCE = "00002a01-0000-1000-8000-00805f9b34fb";
    public static String PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS = "00002a04-0000-1000-8000-00805f9b34fb";

    public static String MODEL_NUMBER_STRING = "00002a24-0000-1000-8000-00805f9b34fb";
    public static String SERIAL_NUMBER_STRING = "00002a25-0000-1000-8000-00805f9b34fb";
    public static String FIRMWARE_REVISION_STRING = "00002a26-0000-1000-8000-00805f9b34fb";
    public static String HARDWARE_REVISION_STRING = "00002a27-0000-1000-8000-00805f9b34fb";
    public static String SOFTWARE_REVISION_STRING = "00002a28-0000-1000-8000-00805f9b34fb";
    public static String MANUFACTURER_NAME_STRING = "00002a29-0000-1000-8000-00805f9b34fb";

    public static String SERVICE_CHANGED = "00002a05-0000-1000-8000-00805f9b34fb";

    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    public static final int xCurrent_Read = 1;
    public static final int yTime_Read = 2;
    public static final int zGasConc_Read = 3;
    public static final int ACCELEROMETER_TIME_READ = 7;
    public static final int BATTERY_LEVEL_READ = 9;

    static { /** static connstructor*/
        // Services
        attributes.put(GAS_DATA_SERVICE, "Accele1rometer Service");
        attributes.put(BATTERY_SERVICE, "Battery Service"); //Namenszuweisung
        attributes.put(GENERIC_ACCESS, "Generic Access");
        attributes.put(DEVICE_INFORMATION_SERVICE, "Device Information Service");
        attributes.put(GENERIC_ATTRIBUTE, "Generic Attribute");

        // Characteristics
        attributes.put(xCurrent_Measurement, "X Accelerometer Type");
        attributes.put(yTimeMeasurement, "Y Accelerometer Measurement");
        attributes.put(zGasConc, "Z Accelerometer Measurement");
        attributes.put(ACCELEROMETER_TIME_MEASUREMENT, "Accelerometer Time Measurement");

        attributes.put(BATTERY_LEVEL, "Battery Level");
        attributes.put(BATTERY_STATUS, "Battery Status");

        attributes.put(DEVICE_NAME, "Device Name");
        attributes.put(APPEARANCE, "Appearance");
        attributes.put(PERIPHERAL_PREFERRED_CONNECTION_PARAMETERS, "Peripheral Preferred Connection Parameters");

        attributes.put(MANUFACTURER_NAME_STRING, "Manufacturer Name String");
        attributes.put(MODEL_NUMBER_STRING, "Model Number String");
        attributes.put(SERIAL_NUMBER_STRING, "Serial Number String");
        attributes.put(HARDWARE_REVISION_STRING, "Hardware Revision String");
        attributes.put(FIRMWARE_REVISION_STRING, "Firmware Revision String");
        attributes.put(SOFTWARE_REVISION_STRING, "Software Revision String");

        attributes.put(SERVICE_CHANGED, "Service Changed");
    }

    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name; //Ich gebe einem String einen Namen von oben
    }
}
