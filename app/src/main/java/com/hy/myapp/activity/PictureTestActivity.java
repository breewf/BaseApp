package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.basic.utils.ImageLoader;

import com.hy.myapp.databinding.ActivityPictureTestBinding;

public class PictureTestActivity extends AbsBaseActivity {

    private String url1 = "https://www.gstatic.com/webp/gallery/1.jpg";
    private String url2 = "https://www.gstatic.com/webp/gallery/2.jpg";
    private String url3 = "https://www.gstatic.com/webp/gallery/3.jpg";
    private String url4 = "https://www.gstatic.com/webp/gallery/4.jpg";
    private String urlCircle = "https://www.gstatic.com/webp/gallery/5.jpg";
    
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
