package io.github.imknown.healthbluetooth.demo;

import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.github.imknown.healthbluetooth.device.MyBluetoothDevice;

public class DeviceListAdapter extends BaseAdapter {

	private List<MyBluetoothDevice> list = new ArrayList<MyBluetoothDevice>();

	private LayoutInflater inflater;

	public DeviceListAdapter(Context context) {
		inflater = LayoutInflater.from(context);
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public MyBluetoothDevice getItem(int position) {
		return list.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public List<MyBluetoothDevice> getList() {
		return this.list;
	}

	public void setItem(List<MyBluetoothDevice> list) {
		this.list = list;
		this.notifyDataSetChanged();
	}

	public void addItem(MyBluetoothDevice MyBluetoothDevice) {
		if (this.list != null) {
			this.list.add(MyBluetoothDevice);
			this.notifyDataSetChanged();
		}
	}

	public void addItem(List<MyBluetoothDevice> list) {
		if (this.list != null) {
			this.list.addAll(list);
			this.notifyDataSetChanged();
		}
	}

	public void addItemToPosition(int position, MyBluetoothDevice MyBluetoothDevice) {
		if (this.list != null) {
			this.list.add(position, MyBluetoothDevice);
			this.notifyDataSetChanged();
		}
	}

	public void addItemToPosition(int position, List<MyBluetoothDevice> list) {
		if (this.list != null) {
			this.list.addAll(position, list);
			this.notifyDataSetChanged();
		}
	}

	public void removeItem(int position) {
		if (position < list.size()) {
			MyBluetoothDevice model = list.get(position);
			removeItem(model);
		}
	}

	public void removeItem(MyBluetoothDevice MyBluetoothDevice) {
		if (this.list.contains(MyBluetoothDevice)) {
			this.list.remove(MyBluetoothDevice);
			this.notifyDataSetChanged();
		}
	}

	public void removeAllItem() {
		if (this.list != null) {
			this.list.clear();
			this.notifyDataSetChanged();
		}
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewPlaceholder holder = null;

		if (convertView == null) {
			convertView = inflater.inflate(R.layout.listitem_deviceinfo, (ViewGroup) null);

			holder = new ViewPlaceholder();
			holder.friendNameAndBondStatus_tv = (TextView) convertView.findViewById(R.id.friendNameAndBondStatus_tv);
			holder.MACAndRSSI_tv = (TextView) convertView.findViewById(R.id.MACAndRSSI_tv);
			holder.class_tv = (TextView) convertView.findViewById(R.id.class_tv);

			convertView.setTag(holder);
		} else {
			holder = (ViewPlaceholder) convertView.getTag();
		}

		MyBluetoothDevice myBluetoothDevice = getItem(position);

		if (myBluetoothDevice != null) {
			BluetoothDevice bd = myBluetoothDevice.getDevice();
			BluetoothClass bluetoothClass = bd.getBluetoothClass();

			String bondState = MyBluetoothDevice.getFriendlyBondStateByDevice(myBluetoothDevice.getBondState());
			holder.friendNameAndBondStatus_tv.setText(bd.getName() + ", " + bondState);

			holder.MACAndRSSI_tv.setText("MAC: " + bd.getAddress() + ", RSSI: " + myBluetoothDevice.getRssi());

			String majorFriendName = MyBluetoothDevice.getMajorFriendNameByMajorDeviceClass(bluetoothClass.getMajorDeviceClass());
			String friendName = MyBluetoothDevice.getFriendNameByDeviceClass(bluetoothClass.getDeviceClass());
			String friendlyType = MyBluetoothDevice.getFriendlyTypeByDevice(bd);
			holder.class_tv.setText("类型: " + friendName + "(" + majorFriendName + ")" + ", 模式: " + friendlyType);
		}

		return convertView;
	}

	public class ViewPlaceholder {
		public TextView friendNameAndBondStatus_tv;

		public TextView MACAndRSSI_tv;

		public TextView class_tv;

		public TextView majorClassAndClassAndFriendlyType_tv;
	}
}