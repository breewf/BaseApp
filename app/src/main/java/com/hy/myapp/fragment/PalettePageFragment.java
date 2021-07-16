package com.hy.myapp.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.hy.baseapp.base.AbsBaseFragment;
import com.hy.baseapp.helper.ImageHelper;
import com.hy.myapp.R;
import com.hy.myapp.activity.PaletteTestActivity;

import androidx.annotation.Nullable;
import butterknife.Bind;

/**
 * Created by GHY on 2016/5/12.
 */
public class PalettePageFragment extends AbsBaseFragment {

    public static final String PAGE_NUMBER = "PAGE_NUMBER";
    private int mPage;

    public static Bitmap bitmap;

    private String[] imageUrl = {"https://img1.baidu.com/it/u=3021819484,2455139200&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=1093577051,512997137&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=3189143085,2808291968&fm=26&fmt=auto&gp=0.jpg",
            "https://img0.baidu.com/it/u=3388914057,1541350825&fm=26&fmt=auto&gp=0.jpg"};

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
