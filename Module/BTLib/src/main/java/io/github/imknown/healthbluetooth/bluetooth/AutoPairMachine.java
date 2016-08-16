package io.github.imknown.healthbluetooth.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import java.lang.reflect.Method;

public class AutoPairMachine {

    private final static String TAG = AutoPairMachine.class.getSimpleName();

    public final static String DEFAULT_PIN_CODE_0000 = "0000";
    public final static String DEFAULT_PIN_CODE_1234 = "1234";

    /**
     * 创建绑定
     */
    public static boolean createBound(BluetoothDevice device) throws Exception {
        Method createBoundMethod = BluetoothDevice.class.getMethod("createBond");
        Boolean returnValue = (Boolean) createBoundMethod.invoke(device);
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "createBond: " + result);
        return result;
    }

    /**
     * 取消绑定
     */
    public static boolean cancelBoundProcess(BluetoothDevice device) throws Exception {
        Method cancelMethod = BluetoothDevice.class.getMethod("cancelBondProcess");
        Boolean returnValue = (Boolean) cancelMethod.invoke(device);
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "cancelBondProcess: " + result);
        return result;
    }

    /**
     * 解除绑定
     */
    public static boolean removeBound(BluetoothDevice device) throws Exception {
        Method removeBoundMethod = BluetoothDevice.class.getMethod("removeBond");
        Boolean returnValue = (Boolean) removeBoundMethod.invoke(device);
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "removeBond: " + result);
        return result;
    }

    /**
     * 发送配对请求
     */
    static public boolean setPairingConfirmation(BluetoothDevice device, boolean confirm) throws Exception {
        Method setPairingConfirmationMethod = BluetoothDevice.class.getMethod("setPairingConfirmation", boolean.class);
        Boolean returnValue = (Boolean) setPairingConfirmationMethod.invoke(device, confirm);
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "setPairingConfirmation: " + result);
        return result;
    }

    /**
     * 设置 pin
     */
    public static boolean setPin(BluetoothDevice device, String pin) throws Exception {
        Method setPinMethod = BluetoothDevice.class.getDeclaredMethod("setPin", new Class[]{byte[].class});
        Boolean returnValue = (Boolean) setPinMethod.invoke(device, new Object[]{pin.getBytes()});
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "setPin: " + pin + ", " + result);
        return result;
    }

    /**
     * 取消用户提示框
     */
    public static boolean cancelPairUserInput(BluetoothDevice device) throws Exception {
        Method cancelPair = BluetoothDevice.class.getMethod("cancelPairingUserInput");
        Boolean returnValue = (Boolean) cancelPair.invoke(device);
        boolean result = returnValue.booleanValue();
        Log.i(TAG, "cancelPairingUserInput: " + result);
        return result;
    }
}
