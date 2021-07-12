package com.hy.basic.network;

import com.hy.baseapp.common.Toasts;
import com.hy.basic.AppNetworkUtils;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

import rx.Subscriber;

/**
 * @author hy
 * @date 2021/7/12
 * desc: ResponseSubscriber
 **/
public abstract class ResponseSubscriber<T> extends Subscriber<T> {

    /**
     * 不显示toast
     */
    private boolean mNotDisplayErrorToast;

    public ResponseSubscriber() {

    }

    public ResponseSubscriber(boolean notDisplayErrorToast) {
        this.mNotDisplayErrorToast = notDisplayErrorToast;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!AppNetworkUtils.isConnected()) {
            if (!mNotDisplayErrorToast) {
                Toasts.showShort("网络不给力");
            }
        }
    }

    @Override
    public void onError(Throwable throwable) {
        if (throwable == null) {
            return;
        }

        if (throwable instanceof UnknownHostException) {
            if (!mNotDisplayErrorToast) {
                Toasts.showShort("网络不给力");
            }
        }

        if (throwable instanceof SocketTimeoutException) {
            if (!mNotDisplayErrorToast) {
                Toasts.showShort("网络不给力");
            }
        }

        if (throwable instanceof IOException) {
            if (!mNotDisplayErrorToast) {
                Toasts.showShort("网络不给力");
            }
        }
    }

    @Override
    public void onCompleted() {

    }
}
