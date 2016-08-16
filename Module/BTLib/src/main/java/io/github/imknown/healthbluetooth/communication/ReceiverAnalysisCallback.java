package io.github.imknown.healthbluetooth.communication;

import android.os.Handler;

import java.io.IOException;
import java.io.InputStream;

public abstract class ReceiverAnalysisCallback {
	protected final static String TAG = ReceiverAnalysisCallback.class.getSimpleName();

	protected SmartSocket smartSocket;

	/** 用来 通知刷新 UI */
	protected Handler handlerOnMainThread;

	/** 是否将 数据 发送到 主线程, 如果是 false, 需要 自己 创建 Handler 处理 */
	protected boolean sendToUIThread = true;

	protected ReceiverAnalysisCallback(SmartSocket smartSocket) {
		this.smartSocket = smartSocket;
	}

	protected final static int ON_SUCCESS_RESULT = 1;
	protected final static int ON_TRANSFER_ERROR = 2;

	/** 解析 InputStream */
	protected abstract void onParseInputStream(InputStream inputStream) throws IOException;

	/** 测量过程中 */
	protected abstract void onProcessing(String message);

	/** 用来把 成功的数据 发给 页面 */
	protected abstract void onSuccessResult(Object obj);

	/** 数据传输错误 */
	protected abstract void onTransferError(Exception ex);
}
