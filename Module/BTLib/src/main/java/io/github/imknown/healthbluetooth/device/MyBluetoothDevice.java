package io.github.imknown.healthbluetooth.device;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothClass.Device;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanSettings;
import android.os.Build;

import java.io.Serializable;

public class MyBluetoothDevice implements Serializable {

    private static final long serialVersionUID = 1174144096753455483L;

    public MyBluetoothDevice(BluetoothDevice device) {
        this.device = device;
    }

    /**
     * 信号强度
     */
    private int rssi;

    /**
     * 备注
     */
    private String remark;

    /**
     * 绑定状态
     */
    private int bondState;

    public int getBondState() {
        return bondState;
    }

    public void setBondState(int bondState) {
        this.bondState = bondState;
    }

    private BluetoothDevice device;

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    // ==============

    @SuppressLint("NewApi")
    public static String getFriendlyTypeByDevice(BluetoothDevice device) {
        String friendlyType = "未知模式";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            int type = device.getType();
            if (type == BluetoothDevice.DEVICE_TYPE_CLASSIC) {
                friendlyType = "BR/EDR传统模式";
            } else if (type == BluetoothDevice.DEVICE_TYPE_DUAL) {
                friendlyType = "BR/EDR/LE 多模式";
            } else if (type == BluetoothDevice.DEVICE_TYPE_LE) {
                friendlyType = "LE低耗模式 ";
            } else if (type == BluetoothDevice.DEVICE_TYPE_UNKNOWN) {
                friendlyType = "未知模式";
            }
        } else {
            friendlyType = "BR/EDR传统模式";
        }

        return friendlyType;
    }

    // region [ Broadcast ============================= ]

    // endregion

    public static String getFriendlyBondStateByDevice(int bondState) {
        String friendlyBondState = "未知绑定状态";
        if (bondState == BluetoothDevice.BOND_NONE) {
            friendlyBondState = "未绑定";
        } else if (bondState == BluetoothDevice.BOND_BONDING) {
            friendlyBondState = "绑定中";
        } else if (bondState == BluetoothDevice.BOND_BONDED) {
            friendlyBondState = "已绑定";
        }

        return friendlyBondState;
    }

    public static String getMajorFriendNameByMajorDeviceClass(int majorDeviceClass) {
        String majorFriendName = "未知类型";

        switch (majorDeviceClass) {
            case Device.Major.AUDIO_VIDEO:
                majorFriendName = "音频 视频";
                break;
            case Device.Major.COMPUTER:
                majorFriendName = "计算机";
                break;
            case Device.Major.HEALTH:
                majorFriendName = "健康";
                break;
            case Device.Major.IMAGING:
                majorFriendName = "成像";
                break;
            case Device.Major.MISC:
                majorFriendName = "杂项";
                break;
            case Device.Major.NETWORKING:
                majorFriendName = "网络";
                break;
            case Device.Major.PERIPHERAL:
                majorFriendName = "外围设备";
                break;
            case Device.Major.PHONE:
                majorFriendName = "电话";
                break;
            case Device.Major.TOY:
                majorFriendName = "玩具";
                break;
            case Device.Major.UNCATEGORIZED:
                majorFriendName = "未分类";
                break;
            case Device.Major.WEARABLE:
                majorFriendName = "可穿戴";
                break;
        }

        return majorFriendName;
    }

    public static String getFriendNameByDeviceClass(int deviceClass) {
        String friendName = "未知类型";

        // AUDIO_VIDEO
        switch (deviceClass) {
            case Device.AUDIO_VIDEO_CAMCORDER:
                friendName = "便携式摄像机";
                break;
            case Device.AUDIO_VIDEO_CAR_AUDIO:
                friendName = "轿车音响";
                break;
            case Device.AUDIO_VIDEO_HANDSFREE:
                friendName = "免提设备";
                break;
            case Device.AUDIO_VIDEO_HEADPHONES:
                friendName = "双耳式耳机";
                break;
            case Device.AUDIO_VIDEO_HIFI_AUDIO:
                friendName = "HIFI 音响";
                break;
            case Device.AUDIO_VIDEO_LOUDSPEAKER:
                friendName = "扬声器";
                break;
            case Device.AUDIO_VIDEO_MICROPHONE:
                friendName = "麦克风";
                break;
            case Device.AUDIO_VIDEO_PORTABLE_AUDIO:
                friendName = "便携式音响";
                break;
            case Device.AUDIO_VIDEO_SET_TOP_BOX:
                friendName = "机顶盒";
                break;
            case Device.AUDIO_VIDEO_UNCATEGORIZED:
                friendName = "未分类";
                break;
            case Device.AUDIO_VIDEO_VCR:
                friendName = " 盒式磁带录像机";
                break;
            case Device.AUDIO_VIDEO_VIDEO_CAMERA:
                friendName = "视频摄像机";
                break;
            case Device.AUDIO_VIDEO_VIDEO_CONFERENCING:
                friendName = "视频会议技术";
                break;
            case Device.AUDIO_VIDEO_VIDEO_DISPLAY_AND_LOUDSPEAKER:
                friendName = "视频显示与扬声器";
                break;
            case Device.AUDIO_VIDEO_VIDEO_GAMING_TOY:
                friendName = "视频游戏玩具";
                break;
            case Device.AUDIO_VIDEO_VIDEO_MONITOR:
                friendName = "视频监视器";
                break;
            case Device.AUDIO_VIDEO_WEARABLE_HEADSET:
                friendName = "可穿戴耳机";
                break;

            // COMPUTER

            case Device.COMPUTER_DESKTOP:
                friendName = "台式电脑";
                break;
            case Device.COMPUTER_HANDHELD_PC_PDA:
                friendName = "掌上电脑(PDA)";
                break;
            case Device.COMPUTER_LAPTOP:
                friendName = "笔记本电脑";
                break;
            case Device.COMPUTER_PALM_SIZE_PC_PDA:
                friendName = "Palm-Size PC(PDA)";
                break;
            case Device.COMPUTER_SERVER:
                friendName = "服务器";
                break;
            case Device.COMPUTER_UNCATEGORIZED:
                friendName = "未分类计";
                break;
            case Device.COMPUTER_WEARABLE:
                friendName = "可穿戴计算机";
                break;

            // HEALTH

            case Device.HEALTH_BLOOD_PRESSURE:
                friendName = "血压仪";
                break;
            case Device.HEALTH_DATA_DISPLAY:
                friendName = "数据显示";
                break;
            case Device.HEALTH_GLUCOSE:
                friendName = "血糖仪";
                break;
            case Device.HEALTH_PULSE_OXIMETER:
                friendName = "脉搏血氧仪";
                break;
            case Device.HEALTH_PULSE_RATE:
                friendName = "脉率仪";
                break;
            case Device.HEALTH_THERMOMETER:
                friendName = "温度计";
                break;
            case Device.HEALTH_UNCATEGORIZED:
                friendName = "未分类";
                break;
            case Device.HEALTH_WEIGHING:
                friendName = "秤";
                break;

            // PHONE

            case Device.PHONE_CELLULAR:
                friendName = "移动电话";
                break;
            case Device.PHONE_CORDLESS:
                friendName = "无绳电话";
                break;
            case Device.PHONE_ISDN:
                friendName = "综合服务数字网(ISDN)";
                break;
            case Device.PHONE_MODEM_OR_GATEWAY:
                friendName = "调制解调器或者网关";
                break;
            case Device.PHONE_SMART:
                friendName = "智能手机";
                break;
            case Device.PHONE_UNCATEGORIZED:
                friendName = "未分类";
                break;

            // TOY

            case Device.TOY_CONTROLLER:
                friendName = "控制器";
                break;
            case Device.TOY_DOLL_ACTION_FIGURE:
                friendName = "玩偶手办";
                break;
            case Device.TOY_GAME:
                friendName = "游戏";
                break;
            case Device.TOY_ROBOT:
                friendName = "机器人";
                break;
            case Device.TOY_UNCATEGORIZED:
                friendName = "未分类";
                break;
            case Device.TOY_VEHICLE:
                friendName = "汽车";
                break;

            // WEARABLE

            case Device.WEARABLE_GLASSES:
                friendName = "眼镜";
                break;
            case Device.WEARABLE_HELMET:
                friendName = "头盔";
                break;
            case Device.WEARABLE_JACKET:
                friendName = "茄克衫";
                break;
            case Device.WEARABLE_PAGER:
                friendName = "寻呼机";
                break;
            case Device.WEARABLE_UNCATEGORIZED:
                friendName = "未分类";
                break;
            case Device.WEARABLE_WRIST_WATCH:
                friendName = "腕表";
                break;
        }

        return friendName;
    }

    public static String getFriendCallbackTypeNameByCallbackType(int callbackType) {
        String friendName = "未知类型";

        switch (callbackType) {
            case ScanSettings.CALLBACK_TYPE_ALL_MATCHES:
                friendName = "ALL_MATCHES";
                break;
            case ScanSettings.CALLBACK_TYPE_FIRST_MATCH:
                friendName = "FIRST_MATCH";
                break;
            case ScanSettings.CALLBACK_TYPE_MATCH_LOST:
                friendName = "MATCH_LOST";
                break;
        }

        return friendName;
    }
}
