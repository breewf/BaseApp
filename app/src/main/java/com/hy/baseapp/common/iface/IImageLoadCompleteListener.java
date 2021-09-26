package com.hy.baseapp.common.iface;

import android.graphics.Bitmap;

/**
 * @author hy
 * @date 2021/9/26
 * desc: IImageLoadCompleteListener
 **/
public interface IImageLoadCompleteListener {

    /**
     * loadComplete
     *
     * @param bitmap bitmap
     */
    void loadComplete(Bitmap bitmap);
}
