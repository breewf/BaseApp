package com.ghy.baseapp.introduce;

/**
 * Created by GHY on 2016/4/29.
 * Fresco使用
 */
public class Introduce_Fresco {

    /**
     * xml简单使用
     *
     * <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv1"
     android:layout_width="160dp"
     android:layout_height="240dp"
     android:scaleType="fitCenter" />
     */


    /**
     * 使用占位符图片
     *
     * <!-- placeholderImage：占位符图片
     placeholderImageScaleType：占位符图片缩放类型-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv4"
     android:layout_width="160dp"
     android:layout_height="240dp"
     android:layout_marginTop="10dp"
     fresco:placeholderImage="@mipmap/loading_default_160x240"
     fresco:placeholderImageScaleType="fitCenter" />
     */


    /**
     *加载中及加载失败
     *
     *
     * <!--
     actualImageScaleType：实际图片的缩放类型
     progressBarImage：加载中图片
     progressBarImageScaleType：加载中图片缩放类型
     progressBarAutoRotateInterval：加载中图片自动旋转间隔时间，直到图片加载完成为止
     fadeDuration：淡入淡出动画，直到图片被完全加载完毕这一过程都会受到该动画的影响-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_loading"
     android:layout_width="160dp"
     android:layout_height="240dp"
     android:layout_centerInParent="true"
     android:layout_weight="1"
     fresco:actualImageScaleType="focusCrop"
     fresco:placeholderImage="@mipmap/loading_default_160x240"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarAutoRotateInterval="2000"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside"
     fresco:fadeDuration="2000"/>

     <!--
     failureImage：加载失败的图片
     failureImageScaleType：加载失败图片的缩放类型-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_loading_error"
     android:layout_width="160dp"
     android:layout_height="240dp"
     android:layout_marginLeft="10dp"
     android:layout_weight="1"
     fresco:actualImageScaleType="focusCrop"
     fresco:failureImage="@mipmap/loading_error_160x240"
     fresco:failureImageScaleType="fitCenter"
     fresco:placeholderImage="@mipmap/loading_default_160x240"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarAutoRotateInterval="5000"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside"
     fresco:fadeDuration="2000"/>
     */


    /**
     * 加载失败点击重试
     * 重复加载4次还是没有加载出来的时候才会显示 failureImage(失败图) 的图片
     *
     *
     * <!--
     retryImage：重试图片
     retryImageScaleType：重试图片缩放类型-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_retry"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_marginBottom="20dp"
     android:layout_gravity="center_horizontal"
     fresco:actualImageScaleType="focusCrop"
     fresco:failureImage="@mipmap/icon_failure"
     fresco:failureImageScaleType="centerInside"
     fresco:placeholderImage="@mipmap/icon_placeholder"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarAutoRotateInterval="5000"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside"
     fresco:retryImage="@mipmap/icon_retry"
     fresco:retryImageScaleType="centerCrop" />
     */


    /**
     * 使用背景图
     *
     * <!--
     backgroundImage：背景图
     fadeDuration：淡入淡出动画，直到图片被完全加载完毕这一过程都会受到该动画的影响-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_background1"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:actualImageScaleType="focusCrop"
     fresco:backgroundImage="@android:color/holo_orange_light"
     fresco:fadeDuration="4000" />
     */


    /**
     * 使用叠加图
     *
     * <!--
     backgroundImage：背景图
     overlayImage：叠加图
     pressedStateOverlayImage：按压状态显示叠加图-->

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_overlay1"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:actualImageScaleType="focusCrop"
     fresco:placeholderImage="@mipmap/img_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:backgroundImage="@android:color/holo_orange_light"
     fresco:pressedStateOverlayImage="@android:color/holo_green_dark"
     fresco:overlayImage="@android:color/black" />
     */



    /**
     *  圆形图
     *
     *
     <!--roundAsCircle：true为圆形图-->
     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_circle1"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundAsCircle="true"/>

     <!--roundAsCircle：true为圆形图-->
     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_circle2"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_marginLeft="20dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/icon_placeholder"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside"
     fresco:progressBarAutoRotateInterval="2000"
     fresco:failureImage="@mipmap/icon_failure"
     fresco:failureImageScaleType="centerInside"
     fresco:fadeDuration="2000"
     fresco:roundAsCircle="true"/>
     */



    /**
     * 圆角图
     *
     *
     * <!--roundedCornerRadius：圆角图
     roundTopLeft：左上，true为圆角，false：非圆角
     其它同理，当四个值都为true时，可省略不写-->


     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_round1"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundedCornerRadius="30dp"
     fresco:roundTopLeft="true"
     fresco:roundTopRight="true"
     fresco:roundBottomLeft="true"
     fresco:roundBottomRight="true"/>

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_round2"
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_marginLeft="20dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/icon_placeholder"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside"
     fresco:progressBarAutoRotateInterval="2000"
     fresco:failureImage="@mipmap/icon_failure"
     fresco:failureImageScaleType="centerInside"
     fresco:fadeDuration="2000"
     fresco:roundedCornerRadius="30dp"/>


     <!-- 同时设置圆形和圆角，会以圆形的方式显示-->
     <com.facebook.drawee.view.SimpleDraweeView
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundAsCircle="true"
     fresco:roundedCornerRadius="30dp"
     fresco:roundTopLeft="false"
     fresco:roundTopRight="true"
     fresco:roundBottomLeft="true"
     fresco:roundBottomRight="true"/>

     */


    /**
     * 圆形圆角边框及颜色
     *
     *
     * <!-- roundingBorderWidth：边框宽度
     roundingBorderColor：边框颜色-->
     <com.facebook.drawee.view.SimpleDraweeView
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundAsCircle="true"
     fresco:roundingBorderWidth="6dp"
     fresco:roundingBorderColor="@color/black"/>

     <com.facebook.drawee.view.SimpleDraweeView
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_marginLeft="20dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundedCornerRadius="30dp"
     fresco:roundingBorderWidth="6dp"
     fresco:roundingBorderColor="@color/black"/>

     <!-- roundingBorderWidth：边框宽度
     roundingBorderColor：边框颜色-->
     <com.facebook.drawee.view.SimpleDraweeView
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundAsCircle="true"
     fresco:roundingBorderWidth="4dp"
     fresco:roundingBorderColor="@color/white"/>

     <com.facebook.drawee.view.SimpleDraweeView
     android:layout_width="100dp"
     android:layout_height="100dp"
     android:layout_marginLeft="20dp"
     android:layout_centerInParent="true"
     fresco:placeholderImage="@mipmap/cat_touxiang"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:roundedCornerRadius="30dp"
     fresco:roundingBorderWidth="4dp"
     fresco:roundingBorderColor="@color/white"/>

     */


    /**
     * Gif动图
     *
     *
     * <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_gif1"
     android:layout_width="360dp"
     android:layout_height="240dp"
     android:layout_centerInParent="true"
     android:layout_weight="1"
     fresco:actualImageScaleType="centerCrop"
     fresco:placeholderImage="@mipmap/loading_default_360x240"
     fresco:placeholderImageScaleType="fitCenter"
     fresco:progressBarAutoRotateInterval="2000"
     fresco:progressBarImage="@mipmap/icon_progress_bar"
     fresco:progressBarImageScaleType="centerInside" />

     <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_gif2"
     android:layout_width="360dp"
     android:layout_height="240dp"
     android:layout_marginTop="-20dp"
     android:layout_centerInParent="true"
     android:layout_weight="1"
     fresco:actualImageScaleType="fitCenter"
     fresco:placeholderImage="@mipmap/xiaohuangren"
     fresco:placeholderImageScaleType="fitCenter" />


     java代码中：
     Uri uri_gif = Uri.parse(url_gif);
     Uri uri_gif2 = Uri.parse("res://mipmap-xxhdpi/" + R.mipmap.xiaohuangren);

     //设置动画图自动播放
     DraweeController controller = Fresco.newDraweeControllerBuilder()
     .setUri(uri_gif)
     .setAutoPlayAnimations(true)
     .build();
     ivGif1.setController(controller);

     DraweeController controller2 = Fresco.newDraweeControllerBuilder()
     .setUri(uri_gif2)
     .setAutoPlayAnimations(true)
     .setControllerListener(controllerListener)
     .build();
     ivGif2.setController(controller2);

     ivGif2.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
    Animatable animation = ivGif2.getController().getAnimatable();
    if (animation != null && !isPlay) {
    // 开始播放
    animation.start();
    isPlay = true;
    } else {
    // 停止播放
    animation.stop();
    isPlay = false;
    }
    }
    });

     */

//    /**
//     * 也许你希望在代码中直接控制动画的播放。
//     * 这种情况下，你需要监听图片是否加载完毕，然后才能控制动画的播放
//     */
//    ControllerListener controllerListener = new BaseControllerListener<ImageInfo>() {
//        @Override
//        public void onFinalImageSet(
//                String id,
//                @Nullable ImageInfo imageInfo,
//                @Nullable Animatable anim) {
//            if (anim != null) {
//                anim.start();
//            }
//        }
//    };


    /**
     * 改变图片的大小
     *
     *
     * <com.facebook.drawee.view.SimpleDraweeView
     android:id="@+id/iv_scan1"
     android:layout_width="360dp"
     android:layout_height="240dp"
     android:layout_centerInParent="true"
     android:layout_weight="1"
     fresco:actualImageScaleType="centerInside"
     fresco:placeholderImage="@mipmap/image02"
     fresco:placeholderImageScaleType="fitCenter" />

     Java代码：
     Uri uri = Uri.parse("res://mipmap-xxhdpi/" + R.mipmap.image02);

     //显示默认图片尺寸大小
     private void showImageSizeDefault(Uri uri){
     ivScan1.setImageURI(uri);
     }

     //修改图片尺寸大小
     private void changeImageSize(Uri uri) {
     int width = 400, height = 340;
     ImageRequest request = ImageRequestBuilder.newBuilderWithSource(uri)
     //设置尺寸
     .setResizeOptions(new ResizeOptions(width, height))
     .build();
     final PipelineDraweeController controller = (PipelineDraweeController) Fresco.newDraweeControllerBuilder()
     .setOldController(ivScan1.getController())
     .setImageRequest(request)
     .build();
     ivScan1.setController(controller);
     }

     */

}
