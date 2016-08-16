package io.github.imknown.healthbluetooth.communication;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.MainThread;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;

import io.github.imknown.healthbluetooth.device.MyBluetoothDevice;

public class SmartSocket extends Thread {

    private Context context;

    private BluetoothDevice device;

    private BluetoothSocket socket;

    private InputStream inputStream;

    private boolean isRunning = false;

    private ISocketConnectStateListener iSocketConnectStateListener;

    public ReceiverAnalysisCallback receiverAnalysisCallback;

    private final static String TAG = SmartSocket.class.getSimpleName();

    /**
     * 仅仅是用来 消息跳转的, 并不刷新 UI
     */
    private Handler handlerOnWorkThread;

    @MainThread
    public SmartSocket(Context context, ISocketConnectStateListener iSocketConnectStateListener, MyBluetoothDevice myBluetoothDevice) {
        this.context = context;
        this.iSocketConnectStateListener = iSocketConnectStateListener;
        this.device = myBluetoothDevice.getDevice();
    }

    private void onDeviceConnected() {
        if (iSocketConnectStateListener != null) {
            iSocketConnectStateListener.onConnected();
        }
    }

    private void onDeviceConnectFailure() {
        if (iSocketConnectStateListener != null) {
            iSocketConnectStateListener.onError(new Exception("设备连接错误"));
        }
    }

    private void onDeviceTransferError() {
        if (receiverAnalysisCallback != null) {
            receiverAnalysisCallback.onTransferError(new Exception("设备传输错误"));
        }
    }

    private void onDeviceReadLine(String message) {
        if (receiverAnalysisCallback != null) {
            receiverAnalysisCallback.onProcessing(message);
        }
    }

    private void onDeviceDisconnected() {
        if (iSocketConnectStateListener != null) {
            iSocketConnectStateListener.onDisconnected();
        }
    }

    public void connect() {
        start();
    }

    /**
     * 停止运行, 断开 Socket
     */
    public void disconnect() {
        if (socket != null) {
            if (isRunning && socket.isConnected()) {
                isRunning = false;

                try {
                    if (socket.isConnected()) {
                        inputStream.close();

                        socket.close();

                        Log.e(TAG, "蓝牙socket 断开连接");
                        onDeviceDisconnected();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    @Override
    public void run() {
        Log.i(TAG, "创建 createRfcommSocket");

        try {
            Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            socket = (BluetoothSocket) method.invoke(device, 1);

            socket.connect();

            isRunning = true;

            Log.e(TAG, "蓝牙 socket 已连接");
            onDeviceConnected();

            inputStream = socket.getInputStream();

            if (receiverAnalysisCallback != null) {
                receiverAnalysisCallback.onParseInputStream(inputStream);
            }
        } catch (Exception ex) {
            // java.io.IOException: read failed, socket might closed or timeout, read ret: -1
            ex.printStackTrace();

            Log.e(TAG, "蓝牙 socket 连接失败");
            onDeviceConnectFailure();
        }
    }

    private void notBpParser() {
        try {
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

            BufferedReader in = new BufferedReader(reader);

            while (isRunning) {
                String line = in.readLine();

                sendResult(line);
            }
        } catch (Exception ex) {
            ex.printStackTrace();

            Log.e(TAG, "蓝牙 socket 数据传输失败");

            onDeviceTransferError();
        }
    }

    public void sendResult(String result) {
        Log.i(TAG, "socket result= " + result);

        // 蓝牙设备读取数据
        onDeviceReadLine(result);
    }
}
