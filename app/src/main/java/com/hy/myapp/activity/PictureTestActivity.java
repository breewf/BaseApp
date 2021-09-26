package com.hy.myapp.activity;

import com.hy.myapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.ImageHelper;

import butterknife.Bind;

public class PictureTestActivity extends AbsBaseActivity {

    private String url1 = "http://p3.so.qhimg.com/sdr/1365_768_/t01a3fd2d6254bb2b73.jpg";
    private String url2 = "http://p2.so.qhimg.com/t016426c21639a6943c.jpg";
    private String url3 = "http://p0.so.qhimg.com/sdr/1365_768_/t010c80d149995c2acf.jpg";
    private String url4 = "http://p4.so.qhimg.com/sdr/1228_768_/t01185f568f2f064f39.jpg";
    private String urlCircle = "http://img4.duitang.com/uploads/item/201501/20/20150120180628_Uy5in.jpeg";

    String urlGif = "res:// /" + R.mipmap.xiaohuangren;

    @Bind(R.id.iv_test1)
    SimpleDraweeView ivTest1;
    @Bind(R.id.iv_test2)
    SimpleDraweeView ivTest2;
    @Bind(R.id.iv_test3)
    SimpleDraweeView ivTest3;
    @Bind(R.id.iv_test4)
    SimpleDraweeView ivTest4;
    @Bind(R.id.iv_circle3)
    SimpleDraweeView ivCircle;
    @Bind(R.id.iv_gif1)
    SimpleDraweeView ivGif1;
    @Bind(R.id.iv_gif2)
    SimpleDraweeView ivGif2;

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

        ImageHelper.getInstance().loadImage(ivTest1,url1);
        ImageHelper.getInstance().loadImage(ivTest2,url2);
        ImageHelper.getInstance().loadImage(ivTest3,url3);
        ImageHelper.getInstance().loadImage(ivTest4,url4);

        ImageHelper.getInstance().loadImage(ivCircle,urlCircle);

        ImageHelper.getInstance().loadGifImage(ivGif1,urlGif);
        ImageHelper.getInstance().loadGifImageClickListener(ivGif2,urlGif);
    }

}
