package com.hy.baseapp.common;

import rx.Subscriber;

/**
 * @author hy
 * @date 2021/7/12
 * desc: SubscriberExtension
 **/
public abstract class SubscriberExtension<T> extends Subscriber<T> {

    @Override
    public void onCompleted() {

    }

    @Override
    public void onError(Throwable e) {

    }

    @Override
    public void onNext(T t) {
        onCall(t);
    }

    /**
     * onNext 回调
     *
     * @param t t
     */
    public abstract void onCall(T t);

}
