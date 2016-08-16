package io.github.imknown.healthbluetooth.demo.analysis;

import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;

import io.github.imknown.healthbluetooth.communication.ReceiverAnalysisCallback;
import io.github.imknown.healthbluetooth.communication.SmartSocket;

public abstract class Classic1ReceiverAnalysis extends ReceiverAnalysisCallback {

    public Classic1ReceiverAnalysis(SmartSocket smartSocket) {
        super(smartSocket);

        handlerOnMainThread = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {

                switch (msg.what) {
                    case ON_SUCCESS_RESULT:
                        onSuccessResult(msg.obj);
                        break;
                    case ON_TRANSFER_ERROR:
                        onTransferError((Exception) msg.obj);
                        break;
                }

                return true;
            }
        });
    }

    // abstract method below ==============================

    @Override
    public void onParseInputStream(InputStream inputStream) throws IOException {
        // TODO result dealed with inputStream
        String result = "xxx";

        super.smartSocket.sendResult(result);
    }

    @Override
    public void onProcessing(String message) {
        if (TextUtils.isEmpty(message)) {
            return;
        }

        if (message.equals("stable")) {
            inResulted(message);
        }
    }

    private void inResulted(String result) {
        // TODO calc

        if (result.equals("success")) {
            sendSuccessResult(new ResultValue(123456));
        } else if (result.equals("fail")) {
            String detailMessage = "fail reason";

            Log.e(TAG, detailMessage);

            onTransferError(new Exception(detailMessage));
        }
    }

    private void sendSuccessResult(ResultValue bp) {
        if (sendToUIThread) {
            Message msg = handlerOnMainThread.obtainMessage();
            msg.what = ON_SUCCESS_RESULT;
            msg.obj = bp;
            msg.sendToTarget();
        } else {
            onSuccessResult(bp);
        }
    }

    public class ResultValue {
        public static final int NO_DATA = -1;

        public int oneValue = NO_DATA;

        public ResultValue(int oneValue) {
            this.oneValue = oneValue;
        }
    }
}