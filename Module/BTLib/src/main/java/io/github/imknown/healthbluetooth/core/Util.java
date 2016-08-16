package io.github.imknown.healthbluetooth.core;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.util.Log;

import java.math.BigInteger;
import java.util.List;

public class Util {

    private final static int HEX_RADIX = 16;

    /**
     * 获取 十进制 数字
     *
     * @param asciiHexInt_L_fromDevice 低位 16进制 数字, 例如 0x32
     * @param asciiHexInt_H_fromDevice 高位 16进制 数字, 例如 0x38
     * @return 十进制 数字, 例如 130
     */
    public static int asciiHexIntToDecFigure(int asciiHexInt_L_fromDevice, int asciiHexInt_H_fromDevice) {
        char asciiHexChar_L_fromDevice = (char) asciiHexInt_L_fromDevice;
        char asciiHexChar_H_fromDevice = (char) asciiHexInt_H_fromDevice;

        String asciiHexString_fromDevice = String.valueOf(asciiHexChar_H_fromDevice) + String.valueOf(asciiHexChar_L_fromDevice);

        int asciiDecInt_fromDevice = Integer.parseInt(asciiHexString_fromDevice, HEX_RADIX);

        return asciiDecInt_fromDevice;
    }

    /**
     * 获取 十进制 数字
     *
     * @param asciiHexString_L_fromDevice 低位 16进制 字符串, 例如 "31"
     * @param asciiHexString_H_fromDevice 高位 16进制 字符串, 例如 "35"
     * @return 十进制 数字, 例如 81
     */
    public static int asciiHexStringToDecFigure(String asciiHexString_L_fromDevice, String asciiHexString_H_fromDevice) {
        int asciiDecInt_L_fromDevice = new BigInteger(asciiHexString_L_fromDevice, HEX_RADIX).intValue();
        int asciiDecInt_H_fromDevice = new BigInteger(asciiHexString_H_fromDevice, HEX_RADIX).intValue();

        return asciiHexIntToDecFigure(asciiDecInt_L_fromDevice, asciiDecInt_H_fromDevice);
    }

    /**
     * 16位进制所包含的字母
     */
    private static final byte[] HEX = "0123456789ABCDEF".getBytes();

    /**
     * 将字节数组转换成16位进制的字符串
     *
     * @param b 要转换的字节数组
     * @return 转换后的字符串
     */
    public static String bytes2HexString(byte[] b) {
        byte[] buff = new byte[2 * b.length];

        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = HEX[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = HEX[b[i] & 0x0f];
        }

        return new String(buff);
    }

    public static void playPairSound(Context context, final int resId) {
        // AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
        // attrBuilder.setLegacyStreamType(AudioManager.STREAM_NOTIFICATION);
        // attrBuilder.setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT);
        // attrBuilder.setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION);
        //
        // SoundPool.Builder spBuilder = new SoundPool.Builder();
        // spBuilder.setMaxStreams(2);
        // spBuilder.setAudioAttributes(attrBuilder.build());
        //
        // SoundPool sp21 = spBuilder.build();

        int maxStreams = 5, streamType = AudioManager.STREAM_NOTIFICATION, srcQuality = 0;
        @SuppressWarnings("deprecation")
        final SoundPool sp20 = new SoundPool(maxStreams, streamType, srcQuality);

        int priority = 1;
        final int soundId = sp20.load(context, resId, priority);

        // soundID : a soundID returned by the load() function
        // leftVolume : left volume value (range = 0.0 to 1.0), 左声道
        // rightVolume : right volume value (range = 0.0 to 1.0), 右声道
        // priority : stream priority (0 = lowest priority), 优先级
        // loop : loop mode (0 = no loop, -1 = loop forever), 循环与否
        // rate : playback rate (1.0 = normal playback, range 0.5 to 2.0), 播放返回的速度
        final float leftVolume = 1, rightVolume = 1;
        final int playPriority = 0, loop = 0;
        final float rate = 1.0F;

        sp20.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                int streamID = sp20.play(soundId, leftVolume, rightVolume, playPriority, loop, rate);

                if (streamID == 0 || status != 0) {
                    Log.e("SmartScannerPairing", "Play sound " + resId + " fail.");
                }
            }
        });
    }

    public static <T> boolean isNullOrEmptyList(List<T> list) {
        boolean result = false;

        if (list == null || list.isEmpty()) {
            result = true;
        }

        return result;
    }
}
