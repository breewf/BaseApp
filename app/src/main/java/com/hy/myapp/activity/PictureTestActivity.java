package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.basic.utils.ImageLoader;

import com.hy.myapp.databinding.ActivityPictureTestBinding;

public class PictureTestActivity extends AbsBaseActivity {

    private String url1 = "https://p3-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/3253a3c3bf7843d283447e0fc001d689~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAg5bCP6JmOQUnnlJ_mtLs=:q75.awebp?rk3s=f64ab15b&x-expires=1792393528&x-signature=D9do7NDYNnehXfRU3%2BmvEYlWNNc%3D";
    private String url2 = "https://p3-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/1ab2b9794a8e4d66958cc0d7eadde778~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAg5bCP6JmOQUnnlJ_mtLs=:q75.awebp?rk3s=f64ab15b&x-expires=1792393528&x-signature=Fx5Fzhh3F0HnHA%2BFL7UoMtchj98%3D";
    private String url3 = "https://p3-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/7f92ed573e8d44a9bc2a50552f65f888~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAg5bCP6JmOQUnnlJ_mtLs=:q75.awebp?rk3s=f64ab15b&x-expires=1792393528&x-signature=0mBz3IH46aswQeebr%2BV%2FyvtoAJw%3D";
    private String url4 = "https://p3-xtjj-sign.byteimg.com/tos-cn-i-73owjymdk6/8eaedd01d9cf406c879fcc6156044aaf~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAg5bCP6JmOQUnnlJ_mtLs=:q75.awebp?rk3s=f64ab15b&x-expires=1792393528&x-signature=WrDW1NDIVSHBWyCI9h1ObgO6csQ%3D";
    private String urlCircle = "https://p26-passport.byteacctimg.com/img/user-avatar/d3bdbf5ab5ab329a6fbf2f88b0cd2adf~90x90.awebp";
    
    private int gifRes = R.mipmap.xiaohuangren;

    private ActivityPictureTestBinding binding;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_picture_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "图片加载示例";
    }

    @Override
    protected void init() {
        binding = ActivityPictureTestBinding.bind(getRootView());

        ImageLoader.loadImage(binding.ivTest1, url1);
        ImageLoader.loadImage(binding.ivTest2, url2);
        ImageLoader.loadImage(binding.ivTest3, url3);
        ImageLoader.loadImage(binding.ivTest4, url4);

        ImageLoader.loadCircleImage(binding.ivCircle3, urlCircle);

        ImageLoader.loadGifImage(binding.ivGif1, gifRes);
    }

}
