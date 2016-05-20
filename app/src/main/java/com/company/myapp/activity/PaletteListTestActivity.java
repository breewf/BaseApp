package com.company.myapp.activity;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.graphics.Palette;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;

import com.bumptech.glide.load.resource.bitmap.GlideBitmapDrawable;
import com.company.myapp.R;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.helper.ImageHelper;

import butterknife.Bind;

public class PaletteListTestActivity extends AbsBaseActivity {


    private String[] urls = {"http://g.hiphotos.baidu.com/image/pic/item/1f178a82b9014a90b04cc438ae773912b21beec1.jpg",
            "http://img3.imgtn.bdimg.com/it/u=826333428,4229099179&fm=21&gp=0.jpg",
            "http://img1.imgtn.bdimg.com/it/u=2669799184,4062348277&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=1219545299,4180981016&fm=21&gp=0.jpg",
            "http://img2.imgtn.bdimg.com/it/u=3910940699,3916213189&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=3758056319,2732037056&fm=21&gp=0.jpg",
            "http://img3.imgtn.bdimg.com/it/u=2730013132,2691125751&fm=21&gp=0.jpg",
            "http://e.hiphotos.baidu.com/zhidao/pic/item/5366d0160924ab18eb02b75e35fae6cd7b890b46.jpg",
            "http://img4.imgtn.bdimg.com/it/u=2989430555,1416378759&fm=21&gp=0.jpg",
            "http://image.tianjimedia.com/uploadImages/2012/010/XC4Y39BYZT9A.jpg"};

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
        if (bitmap == null) return;
        // 使用Palette来设置从Bitmap中提取出的颜色
        Palette.generateAsync(bitmap, new Palette.PaletteAsyncListener() {
            @Override
            public void onGenerated(Palette palette) {
                Palette.Swatch vibrantSwatch = palette.getVibrantSwatch();
                if (vibrantSwatch == null) return;
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
        if (getToolBar() != null) getToolBar().setBackgroundColor(color);
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

//            ImageHelper.getInstance().loadImage(holder.imageView,urls[position]);

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
