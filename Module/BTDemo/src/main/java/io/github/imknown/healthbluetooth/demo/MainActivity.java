package io.github.imknown.healthbluetooth.demo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Chronometer;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import io.github.imknown.healthbluetooth.bluetooth.ScannerListener;
import io.github.imknown.healthbluetooth.bluetooth.SmartScanner;
import io.github.imknown.healthbluetooth.bluetooth.SmartScanner.ScanType;
import io.github.imknown.healthbluetooth.communication.ISocketConnectStateListener;
import io.github.imknown.healthbluetooth.communication.SmartSocket;
import io.github.imknown.healthbluetooth.core.Util;
import io.github.imknown.healthbluetooth.demo.analysis.Classic1ReceiverAnalysis;
import io.github.imknown.healthbluetooth.device.MyBluetoothDevice;

public class MainActivity extends Activity {

    private final String TAG = MainActivity.class.getSimpleName();

    private UIMaker uiMaker;

    private SmartScanner smartScanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uiMaker = new UIMaker(this);

        initSmartScanner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_scan) {
            // TODO change scan type here
            uiMaker.startClassicScan();
            // uiMaker.startBleScan();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void initSmartScanner() {
        smartScanner = ((HealthDeviceApplication) getApplication()).smartScanner;
        smartScanner.setScannerListener(new ScannerListener() {

            @Override
            public void onDiscoveryStarted() {
            }

            @Override
            public void onDiscoveryFinished(Set<BluetoothDevice> bondedDeviceSet, List<MyBluetoothDevice> foundMyBluetoothDeviceList) {
                uiMaker.stopClassicScan(bondedDeviceSet, foundMyBluetoothDeviceList);
            }

            @Override
            public void onConnectionStateChanged(int state) {
            }

            @Override
            public void onStateChanged(int state, int previousState) {
            }

            @Override
            public void onScanModeChanged() {
            }

            @Override
            public void onLocalNameChanged() {
            }

            @Override
            public void onBleStateChanged() {
            }

            @Override
            public void onBleAclConnected() {
            }

            @Override
            public void onBleAclDisconnected() {
            }

            // ↓↓↓
            // ↓↓↓
            // ↓↓↓
            // ↓↓↓

            @Override
            public void onFoundNew(MyBluetoothDevice myBluetoothDevice, int boudState) {
            }

            @Override
            public void onFound(MyBluetoothDevice myBluetoothDevice, int boudState) {
                uiMaker.adapter.addItem(myBluetoothDevice);
            }

            @Override
            public void onDisappeared(MyBluetoothDevice myBluetoothDevice) {
                uiMaker.adapter.removeItem(myBluetoothDevice);
            }

            @Override
            public void onPairingRequest(MyBluetoothDevice myBluetoothDevice) {
            }

            @Override
            public void onAclConnected(MyBluetoothDevice myBluetoothDevice) {
            }

            @Override
            public void onBondStateChanged(MyBluetoothDevice myBluetoothDevice) {
                BluetoothDevice bluetoothDevice = myBluetoothDevice.getDevice();

                int bondState = bluetoothDevice.getBondState();

                uiMaker.adapter.getItem(uiMaker.positionLastClick).setBondState(bondState);
                uiMaker.adapter.notifyDataSetChanged();

                if (bondState == BluetoothDevice.BOND_NONE) {
                    smartScanner.pair(bluetoothDevice);
                }
            }

            @Override
            public void onUuid(MyBluetoothDevice mbd, ParcelUuid[] parcelUuids) {
            }

            @Override
            public void onAclDisconnectRequested(MyBluetoothDevice myBluetoothDevice) {
            }

            @Override
            public void onAclDisconnected(MyBluetoothDevice myBluetoothDevice) {
            }

            @Override
            public void onClassChanged(MyBluetoothDevice myBluetoothDevice, BluetoothClass clazz) {
            }

            @Override
            public void onNameChanged() {
            }

            @Override
            public void onAliasChanged() {
            }

            @Override
            public void onSdpRecord() {
            }

            @Override
            public void onMasInstance() {
            }

            @Override
            public void onNameFailed() {
            }

            @Override
            public void onPairingCancel() {
            }

            @Override
            public void onConnectionAccessRequest() {
            }

            @Override
            public void onConnectionAccessReply() {
            }

            @Override
            public void onConnectionAccessCancel() {
            }

            @Override
            public void onError(Exception ex) {
            }
        });
    }

    public class UIMaker {
        private MainActivity mainActivity;

        private Chronometer classic_scan_time_past_chronometer;
        private ProgressBar classic_scan_time_past_prgrsbr;
        private Chronometer ble_scan_time_past_chronometer;
        private ProgressBar ble_scan_time_past_prgrsbr;

        private ListView listView;

        private DeviceListAdapter adapter;

        /**
         * 最后点击的位置
         */
        public int positionLastClick = -1;

        public UIMaker(MainActivity main) {
            this.mainActivity = main;

            init();
        }

        public void init() {
            listView = (ListView) mainActivity.findViewById(R.id.listView);
            listView.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    positionLastClick = position;

                    conn(parent, view, position, id);
                }
            });
            listView.setOnItemLongClickListener(new OnItemLongClickListener() {
                @Override
                public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                    positionLastClick = position;

                    MyBluetoothDevice myDevice = adapter.getItem(position);
                    BluetoothDevice device = myDevice.getDevice();

                    int state = device.getBondState();
                    if (state == BluetoothDevice.BOND_BONDED) {
                        /* boolean result = */
                        smartScanner.unpair(device);

                        myDevice.setBondState(BluetoothDevice.BOND_NONE);
                        adapter.notifyDataSetChanged();
                    }

                    return true;
                }
            });

            adapter = new DeviceListAdapter(mainActivity);
            listView.setAdapter(adapter);

            classic_scan_time_past_chronometer = (Chronometer) mainActivity.findViewById(R.id.classic_scan_time_past_chronometer);
            classic_scan_time_past_chronometer.setBase(SystemClock.elapsedRealtime());

            classic_scan_time_past_prgrsbr = (ProgressBar) mainActivity.findViewById(R.id.classic_scan_time_past_prgrsbr);
            classic_scan_time_past_prgrsbr.setVisibility(View.INVISIBLE);

            ble_scan_time_past_chronometer = (Chronometer) mainActivity.findViewById(R.id.ble_scan_time_past_chronometer);
            ble_scan_time_past_chronometer.setBase(SystemClock.elapsedRealtime());

            ble_scan_time_past_prgrsbr = (ProgressBar) mainActivity.findViewById(R.id.ble_scan_time_past_prgrsbr);
            ble_scan_time_past_prgrsbr.setVisibility(View.INVISIBLE);
        }

        private BluetoothGatt mBle1BluetoothGatt;
        private BluetoothGatt mBle2BluetoothGatt;


        @SuppressLint("NewApi")
        private BluetoothGattCallback mBle1BluetoothGattCallback = new BluetoothGattCallback() {

            private final UUID FFF0 = UUID.fromString("0000fff0-0000-1000-8000-008066666666");
            private final UUID FFF1 = UUID.fromString("0000fff1-0000-1000-8000-008066666666");

            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i(TAG, "status=" + status + ", onConnectionStateChange");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    Log.i(TAG, "newState=" + newState);

                    if (newState == BluetoothProfile.STATE_CONNECTED) {
                        Log.i(TAG, "Device connected");
                        mBle1BluetoothGatt.discoverServices();
                    }
                }
            }

            public void onServicesDiscovered(BluetoothGatt bluetoothGatt, int status) {
                Log.i(TAG, "status=" + status + ", serviceDiscovered");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    BluetoothGattService bluetoothGattService = bluetoothGatt.getService(FFF0);
                    BluetoothGattCharacteristic mBluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(FFF1);

                    Log.i(TAG, "读 characteristic");
                    bluetoothGatt.readCharacteristic(mBluetoothGattCharacteristic);
                }
            }

            public void onCharacteristicRead(final BluetoothGatt gatt, final BluetoothGattCharacteristic characteristic, int status) {
                Log.e(TAG, "status=" + status + ", onCharacteristicRead");

                if (status == BluetoothGatt.GATT_SUCCESS) {
                    if (FFF1.equals(characteristic.getUuid())) {
                        // 加密的 十进制
                        int characteristicIntInDecimalization = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 0);

                        // 真实的 十进制
                        characteristicIntInDecimalization = decode(characteristicIntInDecimalization);

                        double characteristicDoubleInDecimalization = characteristicIntInDecimalization / 100D;

                        String temperature = new DecimalFormat("#.00").format(characteristicDoubleInDecimalization);

                        Log.e(TAG, "真实温度: " + temperature + " ℃");

                        new Handler(getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Log.i(TAG, "继续 读取 characteristic");
                                gatt.readCharacteristic(characteristic);
                            }
                        }, 1000);
                    }
                }
            }

            private int decode(int characteristicIntInDecimalization) {
                // 十进制数字 转为 二进制数字字符串
                String binaryString = Integer.toBinaryString(characteristicIntInDecimalization);

                StringBuffer sb = new StringBuffer();

                for (int m = binaryString.length() - 1; m >= 0; --m) {
                    char charTemp = binaryString.charAt(m);

                    // 按位取反
                    int bitwiseInvert = (charTemp == '0') ? 1 : 0;

                    sb.append(bitwiseInvert);
                }

                // 二进制数字字符串 转为 十进制数字
                int decimalization = Integer.parseInt(sb.toString(), 2);

                return decimalization;
            }
        };

        @SuppressLint("NewApi")
        private BluetoothGattCallback mBle2BluetoothGattCallback = new BluetoothGattCallback() {

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                Log.i("onConnectionStateChange", "Status: " + status);

                switch (newState) {
                    case BluetoothProfile.STATE_CONNECTED:
                        Log.i("gattCallback", "STATE_CONNECTED");
                        gatt.discoverServices();
                        break;
                    case BluetoothProfile.STATE_DISCONNECTED:
                        Log.e("gattCallback", "STATE_DISCONNECTED");
                        break;
                    default:
                        Log.e("gattCallback", "STATE_OTHER");
                }
            }

            private String TAG = "onServicesDiscovered";

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                List<BluetoothGattService> services = gatt.getServices();

                for (BluetoothGattService bluetoothGattService : services) {
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();

                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        int instanceId = characteristic.getInstanceId();
                        int permissions = characteristic.getPermissions();
                        int properties = characteristic.getProperties();
                        BluetoothGattService service = characteristic.getService();
                        UUID uuid = characteristic.getUuid();
                        byte[] value = characteristic.getValue();
                        int writeType = characteristic.getWriteType();

                        Log.i(TAG, "instanceId = " + instanceId);
                        Log.i(TAG, "permissions = " + permissions);
                        Log.i(TAG, "properties = " + properties);

                        Log.i(TAG, "service = " + service);
                        Log.i(TAG, "uuid = " + uuid);
                        Log.i(TAG, "value = " + value);
                        Log.i(TAG, "writeType = " + writeType);
                    }
                }

                Log.i("onServicesDiscovered", services.toString());
                gatt.readCharacteristic(services.get(1).getCharacteristics().get(0));
            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                Log.i("onCharacteristicRead", characteristic.toString());
                gatt.disconnect();
            }
        };


        private void conn(AdapterView<?> parent, View view, int position, long id) {
            final MyBluetoothDevice myDevice = adapter.getItem(position);
            BluetoothDevice device = myDevice.getDevice();

            String name = device.getName();

            if (smartScanner.scanType == ScanType.BLE_ONLY) {
                smartScanner.stopLeScan();

                if (name.equals("BLE_1")) {
                    smartScanner.disconnectAndCloseGatt(mBle1BluetoothGatt);

                    mBle1BluetoothGatt = smartScanner.connectGatt(MainActivity.this, device, mBle1BluetoothGattCallback);
                } else if (name.equals("BLE_2")) {
                    smartScanner.disconnectAndCloseGatt(mBle2BluetoothGatt);

                    mBle2BluetoothGatt = smartScanner.connectGatt(MainActivity.this, device, mBle2BluetoothGattCallback);
                }
            } else if (smartScanner.scanType == ScanType.CLASSIC_ONLY) {
                int state = device.getBondState();

                if (state == BluetoothDevice.BOND_BONDED) {
                    if (name.startsWith("CLASSIC_1")) {
                        smartScanner.stopClassicScan();

                        SmartSocket smartSocket = new SmartSocket(
                                mainActivity,
                                new ISocketConnectStateListener() {
                                    @Override
                                    public void onConnected() {
                                    }

                                    @Override
                                    public void onError(Exception ex) {
                                    }

                                    @Override
                                    public void onDisconnected() {
                                        // startClassicScan();
                                    }
                                },
                                myDevice);

                        smartSocket.receiverAnalysisCallback = new Classic1ReceiverAnalysis(smartSocket) {
                            @Override
                            public void onSuccessResult(Object obj) {
                                System.out.println(Looper.myLooper() == Looper.getMainLooper());

                                // TODO calc
                            }

                            @Override
                            public void onTransferError(Exception ex) {
                                ex.printStackTrace();
                            }
                        };

                        smartSocket.connect();
                    }
                } else if (state == BluetoothDevice.BOND_BONDING) {

                } else if (state == BluetoothDevice.BOND_NONE) {
                    /* boolean result = */
                    smartScanner.pair(device);
                }
            }
        }

        private void startClassicScan() {
            if (smartScanner.isClassicScanning()) {
                return;
            }

            adapter.removeAllItem();

            classic_scan_time_past_chronometer.setBase(SystemClock.elapsedRealtime());
            classic_scan_time_past_chronometer.start();
            classic_scan_time_past_prgrsbr.setVisibility(View.VISIBLE);
            smartScanner.beginClassicScan();
        }

        private void stopClassicScan(Set<BluetoothDevice> bondedDeviceSet, @Nullable List<MyBluetoothDevice> foundDeviceList) {
            classic_scan_time_past_chronometer.stop();
            classic_scan_time_past_prgrsbr.setVisibility(View.INVISIBLE);

            if (!Util.<MyBluetoothDevice>isNullOrEmptyList(foundDeviceList)) {
                adapter.setItem(foundDeviceList);
            }
        }

        /**
         * BLE 扫描时间
         */
        private static final long SCAN_PERIOD = 11_000;

        private void startBleScan() {
            if (smartScanner.mBleScanning) {
                return;
            }

            // 保险起见, 关闭一次
            smartScanner.stopLeScan();

            ble_scan_time_past_chronometer.stop();
            adapter.removeAllItem();

            ble_scan_time_past_chronometer.setBase(SystemClock.elapsedRealtime());
            ble_scan_time_past_chronometer.start();
            ble_scan_time_past_prgrsbr.setVisibility(View.VISIBLE);

            // Stops scanning after a pre-defined scan period.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    smartScanner.stopLeScan();

                    ble_scan_time_past_chronometer.stop();
                    ble_scan_time_past_prgrsbr.setVisibility(View.INVISIBLE);
                }
            }, SCAN_PERIOD);

            smartScanner.beginLeScan();
        }
    }
}
