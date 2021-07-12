package com.hy.baseapp.helper;

import android.content.Context;
import android.graphics.drawable.Animatable;
import android.net.Uri;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.BaseControllerListener;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;
import com.hy.baseapp.BaseApplication;

/**
 * Created by GHY on 2016/4/29.
 * 图片加载助手类
 */
public class ImageHelper {

    // String url = "res:// /" + R.mipmap.ic_launcher;
    // String url_gif = "http://img3.3lian.com/2006/013/08/20051103121420947.gif";

    public interface OnLoadCompleteListener{
        void onLoadComplete();
    }

    private static ImageHelper mInstance;

    private Context mContext;

    /**
     * gif图是否在播放
     */
    private static boolean isPlay = true;

    public ImageHelper(Context mContext) {
        this.mContext = mContext;
    }

    public static ImageHelper getInstance() {
        if (mInstance == null) {
            synchronized (ImageHelper.class) {
                if (mInstance == null) {
                    mInstance = new ImageHelper(BaseApplication.getInstance().getApplicationContext());
                }
            }
        }
        return mInstance;
    }

    /**
     * 加载图片
     *
     * @param simpleDraweeView
     * @param url              图片地址
     */
    public void loadImage(SimpleDraweeView simpleDraweeView, String url) {
        Uri uri = Uri.parse(url);
        simpleDraweeView.setImageURI(uri);
    }

    /**
     * 加载图片
     *
     * @param simpleDraweeView
     * @param url 图片地址
     * @param listener
     */
    public void loadImage(SimpleDraweeView simpleDraweeView, String url, final OnLoadCompleteListener listener) {
        ControllerListener controllerListener = new BaseControllerListener(){
            @Override
            public void onFinalImageSet(String id, Object imageInfo, Animatable animatable) {
                super.onFinalImageSet(id, imageInfo, animatable);
                if (listener != null) {
                    listener.onLoadComplete();
                }
            }
        };
        Uri uri = Uri.parse(url);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setControllerListener(controllerListener)
                .setUri(uri).build();
        simpleDraweeView.setController(controller);
    }

    /**
     * 使用Glide加载imageView
     * @param imageView
     * @param url
     */
    public void loadImage(ImageView imageView,String url){
        Glide.with(mContext).load(url).into(imageView);
    }

    /**
     * 使用Glide加载imageView
     * @param imageView
     * @param url
     * @param listener
     */
    public void loadImage(ImageView imageView, String url, final OnLoadCompleteListener listener){
        Glide.with(mContext).load(url).into(new GlideDrawableImageViewTarget(imageView){
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super GlideDrawable> animation) {
                super.onResourceReady(resource, animation);
                if (listener != null) {
                    listener.onLoadComplete();
                }
            }
        });
    }

    /**
     * 加载Gif图 自动播放
     *
     * @param simpleDraweeView
     * @param url              图片地址
     */
    public void loadGifImage(SimpleDraweeView simpleDraweeView, String url) {
        loadGifImage(simpleDraweeView, url, true);
    }

    /**
     * 加载Gif图
     *
     * @param simpleDraweeView
     * @param url              图片地址
     * @param autoPlay         是否自动播放
     */
    public void loadGifImage(SimpleDraweeView simpleDraweeView, String url, boolean autoPlay) {
        Uri uri = Uri.parse(url);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(autoPlay)
                .build();
        simpleDraweeView.setController(controller);
    }

    /**
     * 加载Gif图 自动播放
     * 添加点击监听 点击播放、暂停
     *
     * @param simpleDraweeView
     * @param url              图片地址
     */
    public void loadGifImageClickListener(final SimpleDraweeView simpleDraweeView, String url) {
        Uri uri = Uri.parse(url);
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setUri(uri)
                .setAutoPlayAnimations(true)
                .setControllerListener(controllerListener)
                .build();
        simpleDraweeView.setController(controller);
        simpleDraweeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animatable animation = simpleDraweeView.getController().getAnimatable();
                if (animation != null && !isPlay) {
                    // 开始播放
                    animation.start();
                    isPlay = true;
                } else {
                    // 暂停播放
                    animation.stop();
                    isPlay = false;
                }
            }
        });
    }

    /**
     * 也许你希望在代码中直接控制动画的播放。
     * 这种情况下，你需要监听图片是否加载完毕，然后才能控制动画的播放
     */
    private ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
        @Override
        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable anim) {
            if (anim != null) {
                anim.start();
            }
        }
    };
}
