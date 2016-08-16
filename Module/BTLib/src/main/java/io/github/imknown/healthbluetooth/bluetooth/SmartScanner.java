package io.github.imknown.healthbluetooth.bluetooth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.util.SparseArray;

import io.github.imknown.healthbluetooth.communication.ISocketConnectStateListener;
import io.github.imknown.healthbluetooth.communication.ReceiverAnalysisCallback;
import io.github.imknown.healthbluetooth.communication.SmartSocket;
import io.github.imknown.healthbluetooth.device.MyBluetoothDevice;

// http://www.xuebuyuan.com/1732166.html
// http://www.cnblogs.com/fengzhblog/archive/2013/07/12/3187206.html
// http://blog.csdn.net/yudajun/article/details/8362916
// http://www.educity.cn/wenda/176311.html

// http://www.2cto.com/kf/201411/348863.html
// http://www.tuicool.com/articles/F3uaemQ
// http://developer.android.com/guide/topics/connectivity/bluetooth-le.html
public class SmartScanner {
    private final static String TAG = SmartScanner.class.getSimpleName();

    private Context context;

    private BluetoothAdapter bluetoothAdapter;

    /**
     * Classic 单独每一次扫描到的 最终结果
     */
    private List<MyBluetoothDevice> oneTimeTempClassicFoundDeviceList = new ArrayList<MyBluetoothDevice>();
    /**
     * Classic 单独每一次扫描到的 最终结果
     */
    private Map<String, BluetoothDevice> oneTimeTempClassicFoundDeviceMap = new LinkedHashMap<String, BluetoothDevice>();
    /**
     * 累计 扫描到的 结果
     */
    private Map<String, BluetoothDevice> foundDeviceMap = new LinkedHashMap<String, BluetoothDevice>();

    private IntentFilter bluetoothFilter;

    private ScannerListener scannerListener;

    /**
     * 当前的扫描方式, 默认 {@link #ScanType.BLE_ONLY}
     */
    public ScanType scanType = ScanType.BLE_ONLY;

    public static enum ScanType {
        // #region [ 量的定义 ================================== ]

        /**
         * 经典
         */
        CLASSIC_ONLY(1) {
            public String getDesc() {
                return "经典";
            }
        },

        /**
         * 低功耗
         */
        BLE_ONLY(2) {
            public String getDesc() {
                return "低功耗";
            }
        },

        /**
         * 经典与低功耗同时(可能需要 硬件或者ROM 支持)
         */
        @Deprecated CLASSIC_x_BLE(3) {
            public String getDesc() {
                return "经典与低功耗同时";
            }
        };

        // #endregion [ 量的定义 ================================== ]

        /**
         * 没啥用, 只是 代码顺序而已
         */
        private final int order;

        /**
         * 构造器默认也只能是private, 从而保证构造函数只能在内部使用
         */
        private ScanType(int order) {
            this.order = order;
        }

        public int getOrder() {
            return order;
        }

        /**
         * 简单描述
         */
        protected abstract String getDesc();
    }

    public ScannerListener getScannerListener() {
        return scannerListener;
    }

    public void setScannerListener(ScannerListener scannerListener) {
        this.scannerListener = scannerListener;
    }

    public class BluetoothAdapterCompatApi23 {
        public static final String ACTION_BLE_STATE_CHANGED = "android.bluetooth.adapter.action.BLE_STATE_CHANGED";
        public static final String ACTION_BLE_ACL_CONNECTED = "android.bluetooth.adapter.action.BLE_ACL_CONNECTED";
        public static final String ACTION_BLE_ACL_DISCONNECTED = "android.bluetooth.adapter.action.BLE_ACL_DISCONNECTED";
    }

    public class BluetoothDeviceCompatApi23 {
        public static final String ACTION_DISAPPEARED = "android.bluetooth.device.action.DISAPPEARED";
        public static final String ACTION_SDP_RECORD = "android.bluetooth.device.action.SDP_RECORD";
        public static final String ACTION_MAS_INSTANCE = "android.bluetooth.device.action.MAS_INSTANCE";
        public static final String ACTION_NAME_FAILED = "android.bluetooth.device.action.NAME_FAILED";
        public static final String ACTION_ALIAS_CHANGED = "android.bluetooth.device.action.ALIAS_CHANGED";
        public static final String ACTION_PAIRING_REQUEST = "android.bluetooth.device.action.PAIRING_REQUEST";
        public static final String ACTION_PAIRING_CANCEL = "android.bluetooth.device.action.PAIRING_CANCEL";
        public static final String ACTION_CONNECTION_ACCESS_REQUEST = "android.bluetooth.device.action.CONNECTION_ACCESS_REQUEST";
        public static final String ACTION_CONNECTION_ACCESS_REPLY = "android.bluetooth.device.action.CONNECTION_ACCESS_REPLY";
        public static final String ACTION_CONNECTION_ACCESS_CANCEL = "android.bluetooth.device.action.CONNECTION_ACCESS_CANCEL";

        /**
         * API 18 之前 无法 获取 对方的 扫描类型
         */
        public static final int DEVICE_TYPE_NOT_SUPPORT = 0x10;
    }

    public SmartScanner(Context context) {
        this.context = context;

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        bluetoothFilter = new IntentFilter();

        bluetoothFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapterCompatApi23.ACTION_BLE_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothAdapterCompatApi23.ACTION_BLE_ACL_CONNECTED);
        bluetoothFilter.addAction(BluetoothAdapterCompatApi23.ACTION_BLE_ACL_DISCONNECTED);

        bluetoothFilter.addAction(BluetoothDevice.ACTION_FOUND);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_DISAPPEARED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_CLASS_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_NAME_CHANGED);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_ALIAS_CHANGED);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_SDP_RECORD);
        bluetoothFilter.addAction(BluetoothDevice.ACTION_UUID);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_MAS_INSTANCE);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_NAME_FAILED);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_PAIRING_REQUEST);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_PAIRING_CANCEL);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_REQUEST);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_REPLY);
        bluetoothFilter.addAction(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_CANCEL);
    }

    public static boolean supportClassic() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        return bluetoothAdapter == null;
    }

    @SuppressLint("InlinedApi")
    public static boolean supportBle(Context context) {
        boolean result = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            result = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
        }

        return result;
    }

    /**
     * 设置 可被发现 时间
     *
     * @param duration 单位 <span style="color:red; font-weight:bold">秒</span>
     */
    public void setCanBeDiscoveredDuration(int duration) {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration);
        context.startActivity(discoverableIntent);
    }

    public boolean isBluetoothEnabled() {
        boolean result = false;

        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            result = true;
        }

        Log.i(TAG, "isBluetoothEnabled: " + result);

        return result;
    }

    public boolean enableBluetooth() {
        boolean result = true;

        if (!isBluetoothEnabled()) {
            result = bluetoothAdapter.enable();
        }

        Log.i(TAG, "enableBluetooth: " + result);

        return result;
    }

    public boolean disableBluetooth() {
        boolean result = true;

        if (isBluetoothEnabled()) {
            result = bluetoothAdapter.disable();
        }

        Log.i(TAG, "disableBluetooth: " + result);

        return result;
    }

    public boolean isClassicScanning() {
        boolean result = bluetoothAdapter.isDiscovering();

        return result;
    }

    public boolean beginClassicScan() {
        boolean result = true;

        if (!isClassicScanning()) {
            result = bluetoothAdapter.startDiscovery();
        }

        scanType = ScanType.CLASSIC_ONLY;

        Log.i(TAG, "beginClassicScan: " + result);

        return result;
    }

    public boolean stopClassicScan() {
        boolean result = bluetoothAdapter.cancelDiscovery();

        Log.i(TAG, "stopClassicScan: " + result);

        return result;
    }

    // #region [ BLE ============================= ]

    private ScanCallback scanCallback;
    private LeScanCallback leScanCallback;

    private List<ScanFilter> filters;
    private ScanSettings settings;

    /**
     * 是否在 扫描 BLE
     */
    public boolean mBleScanning = false;

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public boolean beginLeScan() {
        boolean result = false;

        oneTimeTempClassicFoundDeviceList = new ArrayList<MyBluetoothDevice>();
        oneTimeTempClassicFoundDeviceMap = new LinkedHashMap<String, BluetoothDevice>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filters = new ArrayList<ScanFilter>();
            settings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).build();

            scanCallback = new ScanCallback() {
                @Override
                public void onBatchScanResults(List<ScanResult> results) {
                    for (ScanResult result : results) {
                        bleApi21Log(result);

                        BluetoothDevice device = result.getDevice();
                        int rssi = result.getRssi();

                        showLogAndMakeMyBluetoothDevice(ScanType.BLE_ONLY, device, rssi);
                    }
                }

                @Override
                public void onScanResult(int callbackType, ScanResult result) {
                    Log.i(TAG, "callbackType: " + MyBluetoothDevice.getFriendCallbackTypeNameByCallbackType(callbackType));

                    bleApi21Log(result);

                    BluetoothDevice device = result.getDevice();
                    int rssi = result.getRssi();

                    showLogAndMakeMyBluetoothDevice(ScanType.BLE_ONLY, device, rssi);
                }

                @Override
                public void onScanFailed(int errorCode) {
                    Log.i(TAG, String.valueOf(errorCode));
                }
            };

            bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
            result = true;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            leScanCallback = new LeScanCallback() {

                @Override
                public void onLeScan(final BluetoothDevice device, final int rssi, byte[] scanRecord) {
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            showLogAndMakeMyBluetoothDevice(ScanType.BLE_ONLY, device, rssi);

                        }
                    });
                }

            };

            result = bluetoothAdapter.startLeScan(leScanCallback);
        }

        scanType = ScanType.BLE_ONLY;

        mBleScanning = true;

        return result;
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public void stopLeScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            bluetoothAdapter.stopLeScan(leScanCallback);
        }

        mBleScanning = false;
    }

    @SuppressWarnings("unused")
    @SuppressLint("NewApi")
    private void bleApi21Log(ScanResult result) {
        ScanRecord scanRecord = result.getScanRecord();

        String localName = scanRecord.getDeviceName();
        int advertiseFlags = scanRecord.getAdvertiseFlags();
        int transmissionPowerLevel = scanRecord.getTxPowerLevel();
        byte[] bytes = scanRecord.getBytes();
        SparseArray<byte[]> manufacturerSpecificData = scanRecord.getManufacturerSpecificData();
        Map<ParcelUuid, byte[]> serviceData = scanRecord.getServiceData();
        List<ParcelUuid> serviceUuids = scanRecord.getServiceUuids();

        long currentTimeMillis = System.currentTimeMillis();
        long elapsedRealtime = SystemClock.elapsedRealtime();
        long resultTimestampNanos = result.getTimestampNanos();
        long resultTimeInMillis = resultTimestampNanos / 1_000_000;
        long rxTimestampMillis = currentTimeMillis - elapsedRealtime + resultTimeInMillis;
        Log.d(TAG, new SimpleDateFormat("kk:mm:ss.SSS", Locale.getDefault()).format(new Date(rxTimestampMillis)));
    }

    // #endregion [ BLE ============================= ]

    // region [ Broadcast ============================= ]

    public void registerBroadcast() {
        context.registerReceiver(bluetoothBroadcast, bluetoothFilter);
    }

    public void unregisterBroadcast() {
        context.unregisterReceiver(bluetoothBroadcast);
    }

    private BroadcastReceiver bluetoothBroadcast = new BroadcastReceiver() {
        @Override

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED)) {
                Log.i(TAG, "Classic 扫描蓝牙设备已开始");

                oneTimeTempClassicFoundDeviceList = new ArrayList<MyBluetoothDevice>();
                oneTimeTempClassicFoundDeviceMap = new LinkedHashMap<String, BluetoothDevice>();

                scannerListener.onDiscoveryStarted();
            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                Log.i(TAG, "Classic 扫描蓝牙设备已结束");
                scannerListener.onDiscoveryFinished(bluetoothAdapter.getBondedDevices(), oneTimeTempClassicFoundDeviceList);
            } else if (action.equals(BluetoothAdapter.ACTION_CONNECTION_STATE_CHANGED)) {
                ACTION_CONNECTION_STATE_CHANGED(intent);
            } else if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                Log.i(TAG, "ACTION_STATE_CHANGED");

                // STATE_OFF, STATE_TURNING_ON, STATE_ON, STATE_TURNING_OFF,
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
                int previousState = intent.getIntExtra(BluetoothAdapter.EXTRA_PREVIOUS_STATE, BluetoothAdapter.STATE_OFF);

                scannerListener.onStateChanged(state, previousState);
            } else if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)) {
                Log.i(TAG, "ACTION_SCAN_MODE_CHANGED");
            } else if (action.equals(BluetoothAdapter.ACTION_LOCAL_NAME_CHANGED)) {
                Log.i(TAG, "ACTION_LOCAL_NAME_CHANGED");
            } else if (action.equals(BluetoothAdapterCompatApi23.ACTION_BLE_STATE_CHANGED)) {
                Log.i(TAG, "ACTION_BLE_STATE_CHANGED");
            } else if (action.equals(BluetoothAdapterCompatApi23.ACTION_BLE_ACL_CONNECTED)) {
                Log.i(TAG, "ACTION_BLE_ACL_CONNECTED");
            } else if (action.equals(BluetoothAdapterCompatApi23.ACTION_BLE_ACL_DISCONNECTED)) {
                Log.i(TAG, "ACTION_BLE_ACL_DISCONNECTED");
            }

            // ↓↓↓
            // ↓↓↓
            // ↓↓↓
            // ↓↓↓

            else if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                ACTION_FOUND(intent);
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_DISAPPEARED)) {
                ACTION_DISAPPEARED(intent);
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_PAIRING_REQUEST)) {
                ACTION_PAIRING_REQUEST(intent);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_CONNECTED)) {
                Log.i(TAG, "低级别连接通知");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyBluetoothDevice mbd = new MyBluetoothDevice(device);
                scannerListener.onAclConnected(mbd);
            } else if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                Log.i(TAG, "ACTION_BOND_STATE_CHANGED");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyBluetoothDevice mbd = new MyBluetoothDevice(device);
                scannerListener.onBondStateChanged(mbd);
            } else if (action.equals(BluetoothDevice.ACTION_UUID)) {
                Log.i(TAG, "ACTION_UUID");

                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyBluetoothDevice mbd = new MyBluetoothDevice(device);

                Parcelable[] parcels = intent.getParcelableArrayExtra(BluetoothDevice.EXTRA_UUID);
                for (Parcelable parcelable : parcels) {
                    ParcelUuid parcelUuid = (ParcelUuid) parcelable;
                    Log.i(TAG, "ACTION_UUID, parcelUuid from intent= " + parcelUuid.toString());
                }

                ParcelUuid[] parcelUuids = device.getUuids();
                for (ParcelUuid parcelUuid : parcelUuids) {
                    Log.i(TAG, "ACTION_UUID, parcelUuid from device= " + parcelUuid.toString());
                }

                scannerListener.onUuid(mbd, parcelUuids);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED)) {
                Log.i(TAG, "低级别断开连接请求");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyBluetoothDevice mbd = new MyBluetoothDevice(device);
                scannerListener.onAclDisconnectRequested(mbd);
            } else if (action.equals(BluetoothDevice.ACTION_ACL_DISCONNECTED)) {
                Log.i(TAG, "低级别断开通知");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                MyBluetoothDevice mbd = new MyBluetoothDevice(device);
                scannerListener.onAclDisconnected(mbd);
            } else if (action.equals(BluetoothDevice.ACTION_CLASS_CHANGED)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                BluetoothClass clazz = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);

                String majorFriendName = MyBluetoothDevice.getMajorFriendNameByMajorDeviceClass(clazz.getMajorDeviceClass());
                String friendName = MyBluetoothDevice.getFriendNameByDeviceClass(clazz.getDeviceClass());
                Log.i(TAG, "设备类型改变, majorFriendName: " + majorFriendName + ", friendName: " + friendName);

                MyBluetoothDevice mbd = new MyBluetoothDevice(device);
                scannerListener.onClassChanged(mbd, clazz);
            } else if (action.equals(BluetoothDevice.ACTION_NAME_CHANGED)) {
                Log.i(TAG, "ACTION_NAME_CHANGED");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_ALIAS_CHANGED)) {
                Log.i(TAG, "ACTION_ALIAS_CHANGED");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_SDP_RECORD)) {
                Log.i(TAG, "ACTION_SDP_RECORD");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_MAS_INSTANCE)) {
                Log.i(TAG, "ACTION_MAS_INSTANCE");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_NAME_FAILED)) {
                Log.i(TAG, "ACTION_NAME_FAILED");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_PAIRING_CANCEL)) {
                Log.i(TAG, "ACTION_PAIRING_CANCEL");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_REQUEST)) {
                Log.i(TAG, "ACTION_CONNECTION_ACCESS_REQUEST");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_REPLY)) {
                Log.i(TAG, "ACTION_CONNECTION_ACCESS_REPLY");
            } else if (action.equals(BluetoothDeviceCompatApi23.ACTION_CONNECTION_ACCESS_CANCEL)) {
                Log.i(TAG, "ACTION_CONNECTION_ACCESS_CANCEL");
            }
        }
    };

    private void ACTION_CONNECTION_STATE_CHANGED(Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_CONNECTION_STATE, BluetoothAdapter.STATE_DISCONNECTED);

        if (state == BluetoothAdapter.STATE_DISCONNECTED) {
        } else if (state == BluetoothAdapter.STATE_CONNECTING) {
        } else if (state == BluetoothAdapter.STATE_CONNECTED) {
        } else if (state == BluetoothAdapter.STATE_DISCONNECTING) {
        }

        Log.i(TAG, "蓝牙连接状态改变, " + state);

        scannerListener.onConnectionStateChanged(state);
    }

    @SuppressWarnings("deprecation")
    private void ACTION_DISAPPEARED(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        String deviceName = device.getName();
        String deviceAddress = device.getAddress();
        String key = deviceName + "@" + deviceAddress;

        Log.i(TAG, "蓝牙设备消失: " + key);

        MyBluetoothDevice mbd = new MyBluetoothDevice(device);
        scannerListener.onDisappeared(mbd);
    }

    private void ACTION_FOUND(Intent intent) {
        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // 这两个 Class 不是一个对象, 但是 值相等 ========================================
        BluetoothClass clazz = intent.getParcelableExtra(BluetoothDevice.EXTRA_CLASS);
        BluetoothClass deviceClazz = device.getBluetoothClass();
        Log.d(TAG, "clazz == deviceClazz :" + (clazz == deviceClazz));
        Log.d(TAG, "clazz.equals(deviceClazz): " + (clazz.equals(deviceClazz)));
        // ===========================================================

        // 这两个 Name 是一个对象 ========================================
        String friendlyName = intent.getExtras().getString(BluetoothDevice.EXTRA_NAME);
        String deviceName = device.getName();
        Log.d(TAG, "friendlyName: " + friendlyName + ", deviceName: " + deviceName);
        // ===========================================================

        short rssi = intent.getExtras().getShort(BluetoothDevice.EXTRA_RSSI);

        showLogAndMakeMyBluetoothDevice(ScanType.CLASSIC_ONLY, device, rssi);
    }

    private void showLogAndMakeMyBluetoothDevice(ScanType scanType, BluetoothDevice device, int rssi) {
        String scanTypeDesc = scanType.getDesc();

        String deviceName = device.getName();

        String deviceAddress = device.getAddress();
        String key = deviceName + "@" + deviceAddress;
        Log.i(TAG, "key: " + key);

        int bondState = device.getBondState();
        String friendlyBondState = MyBluetoothDevice.getFriendlyBondStateByDevice(bondState);

        String friendlyType = MyBluetoothDevice.getFriendlyTypeByDevice(device);

        String attrLog = deviceName + //
                ", Mac: " + deviceAddress + //
                ", 绑定状态: " + friendlyBondState + //
                ", RSSI: " + rssi + //
                ", 类型: " + friendlyType;

        Log.i(TAG, scanTypeDesc + "发现蓝牙设备: " + attrLog);

        MyBluetoothDevice mbd = new MyBluetoothDevice(device);
        mbd.setRssi(rssi);
        mbd.setBondState(bondState);

        if (!oneTimeTempClassicFoundDeviceMap.containsKey(key)) {
            oneTimeTempClassicFoundDeviceMap.put(key, device);

            oneTimeTempClassicFoundDeviceList.add(mbd);

            scannerListener.onFound(mbd, bondState);
        } else {
            // 之所以 在 else 这么写, 可以尽快保证 第一个次发现的 设备 已经缓存 在 oneTimeTempClassicFoundDeviceMap
            oneTimeTempClassicFoundDeviceMap.put(key, device);
        }

        if (!foundDeviceMap.containsKey(key)) {
            foundDeviceMap.put(key, device);
            Log.i(TAG, "新增蓝牙设备: " + attrLog);
            scannerListener.onFoundNew(mbd, bondState);
        }
    }

    @SuppressLint("NewApi")
    public boolean pair(BluetoothDevice deviceToPair) {
        boolean result = false;

        if (!isBluetoothEnabled()) {
            return result;
        }

        // stopClassicScan(); // 配对的时候, 好像会 自动停止扫描

        if (!BluetoothAdapter.checkBluetoothAddress(deviceToPair.getAddress())) {
            Log.w("Mac Address", "地址无效");

            return result;
        }

        String deviceName = deviceToPair.getName();

        if (deviceToPair.getBondState() == BluetoothDevice.BOND_NONE) {
            try {
                Log.i("Bond State", deviceName + " 未配对, 正在配对");

                boolean createBoundResult = AutoPairMachine.createBound(deviceToPair);
                // boolean pinResult = AutoPairMachine.setPin(deviceToPair, AutoPairMachine.DEFAULT_PIN_CODE_1234);
                // boolean cancelPairUserInputResult = AutoPairMachine.cancelPairUserInput(deviceToPair);

                result = createBoundResult;
                // result = (pinResult && createBoundResult && cancelPairUserInputResult);
            } catch (Exception e) {
                Log.e("Bond State", deviceName + " 配对失败");

                e.printStackTrace();
            }
        }

        return result;
    }

    public boolean unpair(BluetoothDevice deviceToUnpair) {
        boolean result = false;

        if (!isBluetoothEnabled()) {
            return result;
        }

        // stopClassicScan(); // 解除配对的时候, 好像会 自动停止扫描

        if (!BluetoothAdapter.checkBluetoothAddress(deviceToUnpair.getAddress())) {
            Log.w("Mac Address", "地址无效");

            return result;
        }

        String deviceName = deviceToUnpair.getName();

        if (deviceToUnpair.getBondState() == BluetoothDevice.BOND_BONDED) {
            Log.i("Bond State", deviceName + " 已配对");

            try {
                result = AutoPairMachine.removeBound(deviceToUnpair);
            } catch (Exception e) {
                Log.e("Unbond State", "解除绑定失败");

                e.printStackTrace();
            }
        }

        return result;
    }

    private void ACTION_PAIRING_REQUEST(Intent intent) {
        Log.i(TAG, "配对请求");

        BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

        // 并不能 起到网上 说的 不弹框
        // Util.playPairSound(context, R.raw.bluetooth_pairing_ceres);
        //
        // try {
        // boolean setPairingConfirmationResult = AutoPairMachine.setPairingConfirmation(device, true);
        // } catch (Exception e) {
        // Log.e("■", "配对请求失败");
        //
        // e.printStackTrace();
        // }

        MyBluetoothDevice mbd = new MyBluetoothDevice(device);
        scannerListener.onPairingRequest(mbd);
    }

    // #endregion [ Broadcast ============================= ]

    public SmartSocket createClassicSmartSocket(ISocketConnectStateListener iSocketConnectStateListener, ReceiverAnalysisCallback receiverAnalysisCallback, MyBluetoothDevice myBluetoothDevice) {
        SmartSocket socket = new SmartSocket(context, iSocketConnectStateListener, myBluetoothDevice);
        socket.receiverAnalysisCallback = receiverAnalysisCallback;

        Log.i(TAG, "创建socket");
        socket.connect();

        return socket;
    }

    public void closeClassicSmartSocket(SmartSocket socket) {
        if (socket != null) {
            socket.disconnect();
        }
    }

    // #region [ BLE Conn ============================= ]

    @SuppressLint("NewApi")
    public BluetoothGatt connectGatt(Context context, BluetoothDevice device, BluetoothGattCallback bluetoothGattCallback) {
        stopLeScan();// will stop after first device detection

        BluetoothGatt gatt = device.connectGatt(context, false, bluetoothGattCallback);

        return gatt;
    }

    @SuppressLint("NewApi")
    public void disconnectAndCloseGatt(BluetoothGatt gatt) {
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            gatt = null;
        }
    }

    // #endregion [ BLE Conn ============================= ]
}
