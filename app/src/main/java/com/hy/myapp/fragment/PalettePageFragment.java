package com.hy.myapp.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.hy.myapp.R;
import com.hy.myapp.activity.PaletteTestActivity;
import com.hy.baseapp.base.AbsBaseFragment;
import com.hy.baseapp.helper.ImageHelper;

import butterknife.Bind;

/**
 * Created by GHY on 2016/5/12.
 */
public class PalettePageFragment extends AbsBaseFragment {

    public static final String PAGE_NUMBER = "PAGE_NUMBER";
    private int mPage;

    /**
     * fragment是否可见
     */
    private boolean isVisible = false;

    public static Bitmap bitmap;

    private String[] imageUrl = {"http://img5q.duitang.com/uploads/item/201503/22/20150322225932_LAAid.jpeg",
            "http://img.zcool.cn/community/0139c6571f9b726ac7253812a99bd2.png",
            "http://image.tianjimedia.com/uploadImages/2012/326/39959B1N25P8.jpg",
            "http://5.26923.com/download/pic/000/003/8a56b4055c28869391447c668f88043b.jpg"};


    @Bind(R.id.iv_palette_fg1)
    ImageView imageView;

    public static Bitmap[] bitmaps = new Bitmap[4];

    @Override
    protected int getLayoutID() {
        return R.layout.fragment_palette_pager;
    }

    public static PalettePageFragment newInstance(int pageNumber) {
        Bundle args = new Bundle();
        args.putInt(PAGE_NUMBER, pageNumber);
        PalettePageFragment palettePageFragment = new PalettePageFragment();
        palettePageFragment.setArguments(args);
        return palettePageFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(PAGE_NUMBER);
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {

        ImageHelper.getInstance().loadImage(imageView, imageUrl[mPage],
                new ImageHelper.OnLoadCompleteListener() {
                    @Override
                    public void onLoadComplete() {
                        bitmap = ((GlideBitmapDrawable) imageView.getDrawable()).getBitmap();
                        bitmaps[mPage] = bitmap;
                        //首次进入设置第一页的图片的颜色
                        PaletteTestActivity.mInstance.getColor(bitmaps[0]);
                    }
                });
    }

}
