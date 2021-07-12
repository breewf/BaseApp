package com.hy.myapp.activity;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.hy.myapp.R;
import com.facebook.drawee.view.SimpleDraweeView;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.component.banner.ConvenientBanner;
import com.hy.baseapp.component.banner.holder.CBViewHolderCreator;
import com.hy.baseapp.component.banner.holder.Holder;
import com.hy.baseapp.component.banner.listener.OnItemClickListener;
import com.hy.baseapp.component.banner.transforms.*;
import com.hy.baseapp.helper.ImageHelper;
import com.hy.baseapp.helper.ToastHelper;

import java.util.Arrays;
import java.util.List;

import butterknife.Bind;
import butterknife.OnClick;

public class BannerTestActivity extends AbsBaseActivity {

    private String[] images = {"http://www.pp3.cn/uploads/allimg/111112/110323M57-5.jpg",
            "http://p4.so.qhimg.com/sdr/1228_768_/t013e442f43954f6ef4.jpg",
            "http://img2.3lian.com/2014/f2/37/d/39.jpg",
            "http://p1.so.qhimg.com/sdr/1228_768_/t01fc41b1c114cc1b46.jpg"
    };

    private List<String> networkImages;

    @Bind(R.id.convenientBanner)
    ConvenientBanner banner;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_banner_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "Banner控件测试页面";
    }

    @Override
    protected void init() {

        networkImages = Arrays.asList(images);

        banner.setPages(new CBViewHolderCreator() {
            @Override
            public Object createHolder() {
                return new NetworkImageHolderView();
            }
        }, networkImages)
                //设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.mipmap.ic_page_indicator, R.mipmap.ic_page_indicator_focused})
                //设置指示器的方向
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                .setOnItemClickListener(new OnBannerClickListener());

    }

    /**
     * 网络图片加载例子
     */
    private class NetworkImageHolderView implements Holder<String> {
        private SimpleDraweeView imageView;

        @Override
        public View createView(Context context) {
            //你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
            imageView = new SimpleDraweeView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, String data) {
            ImageHelper.getInstance().loadImage(imageView, images[position]);
        }
    }

    private class OnBannerClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(int position) {
            ToastHelper.getInstance().showToast("点击了第：" + position + "张");
        }
    }

    @OnClick(R.id.tv_banner_page1)
    public void bannerPage1() {
        changeTransforemer(AccordionTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page2)
    public void bannerPage2() {
        changeTransforemer(CubeOutTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page3)
    public void bannerPage3() {
        changeTransforemer(DepthPageTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page4)
    public void bannerPage4() {
        changeTransforemer(ForegroundToBackgroundTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page5)
    public void bannerPage5() {
        changeTransforemer(RotateDownTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page6)
    public void bannerPage6() {
        changeTransforemer(RotateUpTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page7)
    public void bannerPage7() {
        changeTransforemer(StackTransformer.class.getSimpleName());
    }

    @OnClick(R.id.tv_banner_page8)
    public void bannerPage8() {
        changeTransforemer(ZoomOutTranformer.class.getSimpleName());
    }

    /**
     * 改变翻页效果
     *
     * @param transforemerName
     */
    private void changeTransforemer(String transforemerName) {
        try {
            Class cls = Class.forName("com.hy.baseapp.component.banner.transforms." + transforemerName);
            ABaseTransformer transforemer = (ABaseTransformer) cls.newInstance();
            banner.getViewPager().setPageTransformer(true, transforemer);
            //部分3D特效需要调整滑动速度
            if (transforemerName.equals("StackTransformer")) {
                banner.setScrollDuration(1200);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        banner.startTurning(4000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        banner.stopTurning();
    }
}
