package com.hy.myapp.fragment;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;

import com.hy.baseapp.base.AbsBaseFragment;
import com.hy.basic.utils.ImageLoader;
import com.hy.myapp.R;
import com.hy.myapp.activity.PaletteTestActivity;
import com.hy.myapp.databinding.FragmentPalettePagerBinding;

import androidx.annotation.Nullable;

/**
 * Created by GHY on 2016/5/12.
 */
public class PalettePageFragment extends AbsBaseFragment {

    public static final String PAGE_NUMBER = "PAGE_NUMBER";
    private int mPage;

    private String[] imageUrl = {"https://www.gstatic.com/webp/gallery/1.jpg",
            "https://www.gstatic.com/webp/gallery/2.jpg",
            "https://www.gstatic.com/webp/gallery/4.jpg",
            "https://www.gstatic.com/webp/gallery/5.jpg"};

    private FragmentPalettePagerBinding binding;

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

        binding = FragmentPalettePagerBinding.bind(contentView);

        ImageLoader.loadImage(binding.ivPaletteFg1, imageUrl[mPage],
                b -> {
                    bitmaps[mPage] = b;
                    //首次进入设置第一页的图片的颜色
                    PaletteTestActivity.mInstance.getColor(bitmaps[0]);

                    binding.ivPaletteFg1.setImageBitmap(b);
                    return kotlin.Unit.INSTANCE;
                });
    }

}
