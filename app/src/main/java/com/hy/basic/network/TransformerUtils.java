package com.hy.basic.network;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class TransformerUtils {

    /**
     * 线程切换: IO -> Main
     */
    public static <T> Observable.Transformer<T, T> ioToMainThread() {
        return source -> source.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
