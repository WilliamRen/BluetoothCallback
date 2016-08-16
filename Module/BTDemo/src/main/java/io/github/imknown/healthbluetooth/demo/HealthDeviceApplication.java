package io.github.imknown.healthbluetooth.demo;

import android.app.Application;
import io.github.imknown.healthbluetooth.bluetooth.SmartScanner;

public class HealthDeviceApplication extends Application {

	public SmartScanner smartScanner;

	@Override
	public void onCreate() {
		super.onCreate();

		smartScanner = new SmartScanner(this);
		smartScanner.registerBroadcast();
		smartScanner.enableBluetooth();
	}

	@Override
	public void onTerminate() {
		super.onTerminate();

		smartScanner.disableBluetooth();
		smartScanner.unregisterBroadcast();
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
	}
}
