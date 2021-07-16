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
import android.widget.ListView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.helper.ImageHelper;
import com.hy.myapp.R;

import androidx.palette.graphics.Palette;
import butterknife.Bind;

public class PaletteListTestActivity extends AbsBaseActivity {


    private String[] urls = {"https://img1.baidu.com/it/u=3021819484,2455139200&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=1093577051,512997137&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=3189143085,2808291968&fm=26&fmt=auto&gp=0.jpg",
            "https://img0.baidu.com/it/u=3388914057,1541350825&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=2673390958,3377558716&fm=26&fmt=auto&gp=0.jpg",
            "https://img0.baidu.com/it/u=372055851,3186859110&fm=26&fmt=auto&gp=0.jpg",
            "https://img2.baidu.com/it/u=3561514531,2170659185&fm=26&fmt=auto&gp=0.jpg",
            "https://img2.baidu.com/it/u=3307484942,1189254751&fm=26&fmt=auto&gp=0.jpg",
            "https://img1.baidu.com/it/u=4004991081,1096065104&fm=26&fmt=auto&gp=0.jpg",
            "https://img2.baidu.com/it/u=1033754621,2908807803&fm=26&fmt=auto&gp=0.jpg"};

    private Bitmap bitmap;
    private Bitmap[] bitmaps = new Bitmap[10];

    @Bind(R.id.lv_palette)
    ListView listView;

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

        ListViewAdapter adapter = new ListViewAdapter();
        listView.setAdapter(adapter);

        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
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
            //顶部状态栏颜色加深
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
        Color.colorToHSV(color, hsv); // convert to hsv
        hsv[1] = hsv[1] + 0.1f; // more saturation
        hsv[2] = hsv[2] - 0.1f; // less brightness
        int darkerColor = Color.HSVToColor(hsv);
        return darkerColor;
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

            //ImageHelper.getInstance().loadImage(holder.imageView,urls[position]);

            ImageHelper.getInstance().loadImage(holder.imageView, urls[position],
                    new ImageHelper.OnLoadCompleteListener() {
                        @Override
                        public void onLoadComplete() {
                            bitmap = ((GlideBitmapDrawable) holder.imageView.getDrawable()).getBitmap();
                            bitmaps[position] = bitmap;
                        }
                    });

            return convertView;
        }

        class ViewHolder {
            ImageView imageView;
        }
    }

}
