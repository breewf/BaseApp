package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.ImageLoader;

import com.hy.myapp.databinding.ActivityPictureTestBinding;

public class PictureTestActivity extends AbsBaseActivity {

    private String url1 = "http://p3.so.qhimg.com/sdr/1365_768_/t01a3fd2d6254bb2b73.jpg";
    private String url2 = "http://p2.so.qhimg.com/t016426c21639a6943c.jpg";
    private String url3 = "http://p0.so.qhimg.com/sdr/1365_768_/t010c80d149995c2acf.jpg";
    private String url4 = "http://p4.so.qhimg.com/sdr/1228_768_/t01185f568f2f064f39.jpg";
    private String urlCircle = "http://img4.duitang.com/uploads/item/201501-20-20150120180628_Uy5in.jpeg";
    
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
