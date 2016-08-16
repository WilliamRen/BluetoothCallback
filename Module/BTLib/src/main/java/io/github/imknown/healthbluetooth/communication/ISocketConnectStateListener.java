package io.github.imknown.healthbluetooth.communication;

public interface ISocketConnectStateListener {
    void onConnected();

    void onError(Exception ex);

    void onDisconnected();
}
