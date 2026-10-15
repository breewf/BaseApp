package com.hy.myapp.activity;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.hy.myapp.R;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.component.banner.ConvenientBanner;
import com.hy.baseapp.component.banner.holder.CBViewHolderCreator;
import com.hy.baseapp.component.banner.holder.Holder;
import com.hy.baseapp.component.banner.listener.OnItemClickListener;
import com.hy.baseapp.component.banner.transforms.*;
import com.hy.basic.utils.ImageLoader;
import com.hy.baseapp.helper.ToastHelper;

import java.util.Arrays;
import java.util.List;

import com.hy.myapp.databinding.ActivityBannerTestBinding;

public class BannerTestActivity extends AbsBaseActivity {

    private String[] images = {"https://www.gstatic.com/webp/gallery/1.jpg",
            "https://www.gstatic.com/webp/gallery/2.jpg",
            "https://www.gstatic.com/webp/gallery/3.jpg",
            "https://www.gstatic.com/webp/gallery/4.jpg"};

    private ActivityBannerTestBinding binding;

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
        binding = ActivityBannerTestBinding.bind(getRootView());
        List<String> networkImages = Arrays.asList(images);
        binding.convenientBanner.setPages(new CBViewHolderCreator() {
                    @Override
                    public Object createHolder() {
                        return new NetworkImageHolderView();
                    }
                }, networkImages)
                // 设置两个点图片作为翻页指示器，不设置则没有指示器，可以根据自己需求自行配合自己的指示器,不需要圆点指示器可用不设
                .setPageIndicator(new int[]{R.mipmap.ic_page_indicator, R.mipmap.ic_page_indicator_focused})
                // 设置指示器的方向
                .setPageIndicatorAlign(ConvenientBanner.PageIndicatorAlign.ALIGN_PARENT_RIGHT)
                .setOnItemClickListener(new OnBannerClickListener());

        binding.tvBannerPage1.setOnClickListener(v -> bannerPage1());
        binding.tvBannerPage2.setOnClickListener(v -> bannerPage2());
        binding.tvBannerPage3.setOnClickListener(v -> bannerPage3());
        binding.tvBannerPage4.setOnClickListener(v -> bannerPage4());
        binding.tvBannerPage5.setOnClickListener(v -> bannerPage5());
        binding.tvBannerPage6.setOnClickListener(v -> bannerPage6());
        binding.tvBannerPage7.setOnClickListener(v -> bannerPage7());
        binding.tvBannerPage8.setOnClickListener(v -> bannerPage8());
    }

    /**
     * 网络图片加载例子
     */
    private class NetworkImageHolderView implements Holder<String> {
        private ImageView imageView;

        @Override
        public View createView(Context context) {
            // 你可以通过layout文件来创建，也可以像我一样用代码创建，不一定是Image，任何控件都可以进行翻页
            imageView = new ImageView(context);
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            return imageView;
        }

        @Override
        public void UpdateUI(Context context, int position, String data) {
            ImageLoader.loadImage(imageView, images[position]);
        }
    }

    private static class OnBannerClickListener implements OnItemClickListener {

        @Override
        public void onItemClick(int position) {
            ToastHelper.getInstance().showToast("点击了第：" + position + "张");
        }
    }

    public void bannerPage1() {
        changeTransforemer(AccordionTransformer.class.getSimpleName());
    }

    public void bannerPage2() {
        changeTransforemer(CubeOutTransformer.class.getSimpleName());
    }

    public void bannerPage3() {
        changeTransforemer(DepthPageTransformer.class.getSimpleName());
    }

    public void bannerPage4() {
        changeTransforemer(ForegroundToBackgroundTransformer.class.getSimpleName());
    }

    public void bannerPage5() {
        changeTransforemer(RotateDownTransformer.class.getSimpleName());
    }

    public void bannerPage6() {
        changeTransforemer(RotateUpTransformer.class.getSimpleName());
    }

    public void bannerPage7() {
        changeTransforemer(StackTransformer.class.getSimpleName());
    }

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
            binding.convenientBanner.getViewPager().setPageTransformer(true, transforemer);
            // 部分3D特效需要调整滑动速度
            if (transforemerName.equals("StackTransformer")) {
                binding.convenientBanner.setScrollDuration(1200);
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
        binding.convenientBanner.startTurning(4000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        binding.convenientBanner.stopTurning();
    }
}
