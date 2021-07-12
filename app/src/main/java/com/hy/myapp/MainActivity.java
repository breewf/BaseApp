package com.hy.myapp;

import android.widget.Toast;

import com.hy.baseapp.activity.IndexHomeActivity;
import com.hy.baseapp.activity.IndexTabActivity;
import com.hy.baseapp.activity.TabLayoutActivity;
import com.hy.baseapp.activity.TabLayoutMeActivity;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.common.logger.Logger;
import com.hy.myapp.activity.ActivityStatusTestActivity;
import com.hy.myapp.activity.BannerTestActivity;
import com.hy.myapp.activity.DialogTestActivity;
import com.hy.myapp.activity.FileDownloadTestActivity;
import com.hy.myapp.activity.FragmentStatusActivity;
import com.hy.myapp.activity.LabelViewTestActivity;
import com.hy.myapp.activity.LoadingTestActivity;
import com.hy.myapp.activity.MyDialogTestActivity;
import com.hy.myapp.activity.PaletteTestActivity;
import com.hy.myapp.activity.PictureTestActivity;
import com.hy.myapp.activity.RetrofitTestActivity;
import com.hy.myapp.activity.SlidingMenuTestActivity;
import com.hy.myapp.activity.SnackBarTestActivity;
import com.hy.myapp.activity.WheelTestActivity;

import butterknife.OnClick;

public class MainActivity extends AbsBaseActivity {


    @Override
    protected int getLayoutID() {
        return R.layout.activity_main;
    }

    @Override
    protected String getToolBarTitle() {
        return "BaseApp";
    }

    @Override
    protected boolean isOpenToolBar() {
        return true;
    }

    @Override
    protected boolean isOpenToolBarLeftBack() {
        return false;
    }

    @Override
    protected int setToolBarRightType() {
        return TOOLBAR_RIGHT_TYPE_ITEM;
    }

    @Override
    protected String setToolBarRightIvTitle() {
        return "设置";
    }

    @Override
    protected String setToolBarRightTv() {
        return "哈哈";
    }

    @Override
    protected int setLoadingProgressType() {
        return LOADING_PROGRESS_TYPE_CIRCLE;
    }

    @Override
    protected void init() {
        Logger.i("Hello");

        //toolBar右侧点击事件
        setRightClickListener();

    }

    @OnClick(R.id.btn_test1)
    public void btn1Click() {
        startActivity(this, ActivityStatusTestActivity.class);
    }

    @OnClick(R.id.btn_test2)
    public void btn2Click() {
        startActivity(this, LoadingTestActivity.class);
    }

    @OnClick(R.id.btn_test3)
    public void btn3Click() {
        startActivity(this, FragmentStatusActivity.class);
    }

    @OnClick(R.id.btn_pic_test)
    public void picTestClick() {
        startActivity(this, PictureTestActivity.class);
    }

    @OnClick(R.id.btn_dialog_test)
    public void dialogTest() {
        startActivity(this, DialogTestActivity.class);
    }

    @OnClick(R.id.btn_dialog_test2)
    public void dialogTest2() {
        startActivity(this, MyDialogTestActivity.class);
    }

    @OnClick(R.id.btn_test_5)
    public void btn5Click() {
        startActivity(this, WheelTestActivity.class);
    }

    @OnClick(R.id.btn_label)
    public void btnlabelView() {
        startActivity(this, LabelViewTestActivity.class);
    }

    @OnClick(R.id.btn_snack_bar)
    public void btnSnackBar() {
        startActivity(this, SnackBarTestActivity.class);
    }

    @OnClick(R.id.btn_banner)
    public void btnBanner() {
        startActivity(this, BannerTestActivity.class);
    }

    @OnClick(R.id.btn_tab_change_color)
    public void btnTabChange() {
        startActivity(this, IndexHomeActivity.class);
    }

    @OnClick(R.id.btn_tab_index)
    public void btnTabIndex() {
        startActivity(this, IndexTabActivity.class);
    }

    @OnClick(R.id.btn_tab_layout1)
    public void btnTabLayout1() {
        startActivity(this, TabLayoutActivity.class);
    }

    @OnClick(R.id.btn_tab_layout2)
    public void btnTabLayout2() {
        startActivity(this, TabLayoutMeActivity.class);
    }

    @OnClick(R.id.btn_palette)
    public void btnPalette() {
        startActivity(this, PaletteTestActivity.class);
    }

    @OnClick(R.id.btn_file_download)
    public void btnFileDownLoad() {
        startActivity(this, FileDownloadTestActivity.class);
    }

    @OnClick(R.id.btn_sliding_menu)
    public void btnSlidingMenu() {
        startActivity(this, SlidingMenuTestActivity.class);
    }

    @OnClick(R.id.btn_retrofit)
    public void btnRetrofit() {
        startActivity(this, RetrofitTestActivity.class);
    }


    /**
     * toolBar右侧按钮点击事件
     */
    private void setRightClickListener() {

        setOnToolBarRightItemClickListener(new ToolBarRightItemClickListener() {
            @Override
            public void onToolBarRightItemClick(int itemId) {
                switch (itemId) {
                    case R.id.abs_menu_main_item1:
                        Toast.makeText(MainActivity.this, "设置", Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.abs_menu_main_item2:
                        MainActivity.this.finish();
                        break;
                    default:
                        break;
                }
            }
        });
    }

}
