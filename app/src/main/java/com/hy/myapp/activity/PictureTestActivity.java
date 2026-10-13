package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.ImageHelper;

import com.hy.myapp.databinding.ActivityPictureTestBinding;

public class PictureTestActivity extends AbsBaseActivity {

    private String url1 = "http://p3.so.qhimg.com/sdr/1365_768_/t01a3fd2d6254bb2b73.jpg";
    private String url2 = "http://p2.so.qhimg.com/t016426c21639a6943c.jpg";
    private String url3 = "http://p0.so.qhimg.com/sdr/1365_768_/t010c80d149995c2acf.jpg";
    private String url4 = "http://p4.so.qhimg.com/sdr/1228_768_/t01185f568f2f064f39.jpg";
    private String urlCircle = "http://img4.duitang.com/uploads/item/201501/20/20150120180628_Uy5in.jpeg";

    String urlGif = "res:// /" + R.mipmap.xiaohuangren;

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

        ImageHelper.getInstance().loadImage(binding.ivTest1,url1);
        ImageHelper.getInstance().loadImage(binding.ivTest2,url2);
        ImageHelper.getInstance().loadImage(binding.ivTest3,url3);
        ImageHelper.getInstance().loadImage(binding.ivTest4,url4);

        ImageHelper.getInstance().loadImage(binding.ivCircle3,urlCircle);

        ImageHelper.getInstance().loadGifImage(binding.ivGif1,urlGif);
        ImageHelper.getInstance().loadGifImageClickListener(binding.ivGif2,urlGif);
    }

}
