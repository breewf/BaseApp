package com.hy.myapp.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.basic.utils.ImageLoader;
import com.hy.myapp.R;
import com.hy.myapp.databinding.ActivityPaletteListTestBinding;

import androidx.palette.graphics.Palette;

public class PaletteListTestActivity extends AbsBaseActivity {

    private String[] urls = {"https://www.gstatic.com/webp/gallery/1.jpg",
            "https://www.gstatic.com/webp/gallery/2.jpg",
            "https://www.gstatic.com/webp/gallery/4.jpg",
            "https://www.gstatic.com/webp/gallery/5.jpg",
            "https://www.gstatic.com/webp/gallery/1.jpg",
            "https://www.gstatic.com/webp/gallery/2.jpg",
            "https://www.gstatic.com/webp/gallery/4.jpg",
            "https://www.gstatic.com/webp/gallery/5.jpg"};

    private Bitmap[] bitmaps = new Bitmap[10];

    private ActivityPaletteListTestBinding binding;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_palette_list_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "PaletteListView页面";
    }

    @Override
    protected void init() {

        binding = ActivityPaletteListTestBinding.bind(getRootView());

        ListViewAdapter adapter = new ListViewAdapter();
        binding.lvPalette.setAdapter(adapter);

        binding.lvPalette.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                int position = view.getFirstVisiblePosition();
                getColor(bitmaps[position]);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    /**
     * 提取颜色
     *
     * @param bitmap
     */
    public void getColor(Bitmap bitmap) {
        if (bitmap == null) {
            return;
        }
        // 使用Palette来设置从Bitmap中提取出的颜色
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch == null) {
                    return;
                }
                setColor(vibrantSwatch.getRgb());
            }
        });
    }

    /**
     * 设置颜色变化
     *
     * @param color
     */
    private void setColor(int color) {
        if (getToolBar() != null) {
            getToolBar().setBackgroundColor(color);
        }
        if (android.os.Build.VERSION.SDK_INT >= 21) {
            Window window = getWindow();
            window.setStatusBarColor(getDarkerColor(color));
        }
    }

    /**
     * 转换为深色
     *
     * @param color
     * @return
     */
    public int getDarkerColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = hsv[1] + 0.1f;
        hsv[2] = hsv[2] - 0.1f;
        return Color.HSVToColor(hsv);
    }

    public class ListViewAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return urls.length;
        }

        @Override
        public Object getItem(int position) {
            return urls[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(PaletteListTestActivity.this).inflate(R.layout.item_palette_list, null);
                holder = new ViewHolder();
                holder.imageView = (ImageView) convertView.findViewById(R.id.iv_palette_item);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ImageLoader.loadImage(holder.imageView, urls[position],
                    bitmap -> {
                        bitmaps[position] = bitmap;
                        holder.imageView.setImageBitmap(bitmap);
                        return kotlin.Unit.INSTANCE;
                    });

            return convertView;
        }

        static class ViewHolder {
            ImageView imageView;
        }
    }

}
