package com.hy.myapp;

import android.widget.Toast;

import com.hy.baseapp.activity.IndexHomeActivity;
import com.hy.baseapp.activity.TabLayoutActivity;
import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.myapp.activity.ActivityResultContractActivity;
import com.hy.myapp.activity.ActivityStatusTestActivity;
import com.hy.myapp.activity.BannerTestActivity;
import com.hy.myapp.activity.DialogTestActivity;
import com.hy.myapp.activity.FragmentStatusActivity;
import com.hy.myapp.activity.LoadingTestActivity;
import com.hy.myapp.activity.MyDialogTestActivity;
import com.hy.myapp.activity.PaletteTestActivity;
import com.hy.myapp.activity.PictureTestActivity;
import com.hy.myapp.activity.RetrofitTestActivity;
import com.hy.myapp.activity.SnackBarTestActivity;

import com.hy.myapp.databinding.ActivityMainBinding;

public class MainActivity extends AbsBaseActivity {

    private ActivityMainBinding binding;


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
        binding = ActivityMainBinding.bind(getRootView());
        //toolBar右侧点击事件
        setRightClickListener();
        //按钮点击事件
        binding.btnTest1.setOnClickListener(v -> btn1Click());
        binding.btnTest2.setOnClickListener(v -> btn2Click());
        binding.btnTest3.setOnClickListener(v -> btn3Click());
        binding.btnPicTest.setOnClickListener(v -> picTestClick());
        binding.btnDialogTest.setOnClickListener(v -> dialogTest());
        binding.btnDialogTest2.setOnClickListener(v -> dialogTest2());
        binding.btnSnackBar.setOnClickListener(v -> btnSnackBar());
        binding.btnBanner.setOnClickListener(v -> btnBanner());
        binding.btnTabChangeColor.setOnClickListener(v -> btnTabChange());
        binding.btnTabLayout1.setOnClickListener(v -> btnTabLayout1());
        binding.btnPalette.setOnClickListener(v -> btnPalette());
        binding.btnRetrofit.setOnClickListener(v -> btnRetrofit());
        binding.btnActivityResult.setOnClickListener(v -> btnActivityResult());
    }

    /**
     * toolBar右侧按钮点击事件
     */
    private void setRightClickListener() {
        setOnToolBarRightItemClickListener(itemId -> {
            if (itemId == R.id.abs_menu_main_item1) {
                Toast.makeText(MainActivity.this, "设置", Toast.LENGTH_SHORT).show();
            } else if (itemId == R.id.abs_menu_main_item2) {
                MainActivity.this.finish();
            }
        });
    }

    public void btn1Click() {
        startActivity(this, ActivityStatusTestActivity.class);
    }

    public void btn2Click() {
        startActivity(this, LoadingTestActivity.class);
    }

    public void btn3Click() {
        startActivity(this, FragmentStatusActivity.class);
    }

    public void picTestClick() {
        startActivity(this, PictureTestActivity.class);
    }

    public void dialogTest() {
        startActivity(this, DialogTestActivity.class);
    }

    public void dialogTest2() {
        startActivity(this, MyDialogTestActivity.class);
    }

    public void btnSnackBar() {
        startActivity(this, SnackBarTestActivity.class);
    }

    public void btnBanner() {
        startActivity(this, BannerTestActivity.class);
    }

    public void btnTabChange() {
        startActivity(this, IndexHomeActivity.class);
    }

    public void btnTabLayout1() {
        startActivity(this, TabLayoutActivity.class);
    }

    public void btnPalette() {
        startActivity(this, PaletteTestActivity.class);
    }

    public void btnRetrofit() {
        startActivity(this, RetrofitTestActivity.class);
    }

    public void btnActivityResult() {
        startActivity(this, ActivityResultContractActivity.class);
    }

}
