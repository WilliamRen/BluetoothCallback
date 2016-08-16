package io.github.imknown.healthbluetooth.bluetooth;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.util.List;
import java.util.Set;

import io.github.imknown.healthbluetooth.device.MyBluetoothDevice;

public abstract class ScannerListener {
    public void onDiscoveryStarted() {
    }

    public void onDiscoveryFinished(Set<BluetoothDevice> bondedDeviceSet, List<MyBluetoothDevice> foundMyBluetoothDeviceList) {
    }

    public void onConnectionStateChanged(int state) {
    }

    /**
     * 蓝牙硬件 状态改变
     */
    public void onStateChanged(int state, int previousState) {
    }

    public void onScanModeChanged() {
    }

    public void onLocalNameChanged() {
    }

    @Deprecated
    public void onBleStateChanged() {
    }

    @Deprecated
    public void onBleAclConnected() {
    }

    @Deprecated
    public void onBleAclDisconnected() {
    }

    // ↓↓↓
    // ↓↓↓
    // ↓↓↓
    // ↓↓↓

    public void onFoundNew(MyBluetoothDevice myBluetoothDevice, int boudState) {
    }

    public void onFound(MyBluetoothDevice myBluetoothDevice, int boudState) {
    }

    @Deprecated
    public void onDisappeared(MyBluetoothDevice myBluetoothDevice) {
    }

    @Deprecated
    public void onPairingRequest(MyBluetoothDevice myBluetoothDevice) {
    }

    public void onAclConnected(MyBluetoothDevice myBluetoothDevice) {
    }

    public void onBondStateChanged(MyBluetoothDevice myBluetoothDevice) {
    }

    public void onUuid(MyBluetoothDevice mbd, ParcelUuid[] parcelUuids) {
    }

    public void onAclDisconnectRequested(MyBluetoothDevice myBluetoothDevice) {
    }

    public void onAclDisconnected(MyBluetoothDevice myBluetoothDevice) {
    }

    public void onClassChanged(MyBluetoothDevice myBluetoothDevice, BluetoothClass clazz) {
    }

    public void onNameChanged() {
    }

    @Deprecated
    public void onAliasChanged() {
    }

    @Deprecated
    public void onSdpRecord() {
    }

    @Deprecated
    public void onMasInstance() {
    }

    @Deprecated
    public void onNameFailed() {
    }

    @Deprecated
    public void onPairingCancel() {
    }

    @Deprecated
    public void onConnectionAccessRequest() {
    }

    @Deprecated
    public void onConnectionAccessReply() {
    }

    @Deprecated
    public void onConnectionAccessCancel() {
    }

    public void onError(Exception ex) {
    }
}
