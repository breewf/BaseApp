package com.company.myapp;

import android.widget.Toast;

import com.company.myapp.activity.ActivityStatusTestActivity;
import com.company.myapp.activity.ApiRequestTestActivity;
import com.company.myapp.activity.BannerTestActivity;
import com.company.myapp.activity.DateTimeTestActivity;
import com.company.myapp.activity.DialogTestActivity;
import com.company.myapp.activity.FileDownloadTestActivity;
import com.company.myapp.activity.FragmentStatusActivity;
import com.company.myapp.activity.LabelViewTestActivity;
import com.company.myapp.activity.ListViewMoreItemTypeTestActivity;
import com.company.myapp.activity.ListViewTest1Activity;
import com.company.myapp.activity.ListViewTest2Activity;
import com.company.myapp.activity.LoadingTestActivity;
import com.company.myapp.activity.MyDialogTestActivity;
import com.company.myapp.activity.PaletteTestActivity;
import com.company.myapp.activity.PictureTestActivity;
import com.company.myapp.activity.PullZoomTest1Activity;
import com.company.myapp.activity.RecyclerViewTest1Activity;
import com.company.myapp.activity.RecyclerViewTest2Activity;
import com.company.myapp.activity.RecyclerViewTest3Activity;
import com.company.myapp.activity.RecyclerViewTest4Activity;
import com.company.myapp.activity.RefreshTest1Activity;
import com.company.myapp.activity.RefreshTest2Activity;
import com.company.myapp.activity.RefreshTest3Activity;
import com.company.myapp.activity.RefreshTest4Activity;
import com.company.myapp.activity.SheetTestActivity;
import com.company.myapp.activity.SlidMenuTabTestActivity;
import com.company.myapp.activity.SlidMenuTestActivity;
import com.company.myapp.activity.SnackBarTestActivity;
import com.company.myapp.activity.WheelTestActivity;
import com.ghy.baseapp.activity.IndexHomeActivity;
import com.ghy.baseapp.activity.IndexTabActivity;
import com.ghy.baseapp.activity.TabLayoutActivity;
import com.ghy.baseapp.activity.TabLayoutMeActivity;
import com.ghy.baseapp.base.AbsBaseActivity;
import com.ghy.baseapp.common.logger.Logger;

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

    @OnClick(R.id.btn_test4)
    public void btn4Click() {
        startActivity(this, ApiRequestTestActivity.class);
    }

    @OnClick(R.id.btn_pic_test)
    public void picTestClick() {
        startActivity(this, PictureTestActivity.class);
    }

    @OnClick(R.id.btn_dialog_test)
    public void dialogTest(){
        startActivity(this, DialogTestActivity.class);
    }

    @OnClick(R.id.btn_dialog_test2)
    public void dialogTest2(){
        startActivity(this, MyDialogTestActivity.class);
    }

    @OnClick(R.id.btn_test_5)
    public void btn5Click(){
        startActivity(this, WheelTestActivity.class);
    }

    @OnClick(R.id.btn_label)
    public void btnlabelView(){
        startActivity(this, LabelViewTestActivity.class);
    }

    @OnClick(R.id.btn_snack_bar)
    public void btnSnackBar(){
        startActivity(this, SnackBarTestActivity.class);
    }

    @OnClick(R.id.btn_banner)
    public void btnBanner(){
        startActivity(this, BannerTestActivity.class);
    }


    @OnClick(R.id.btn_data)
    public void btnData(){
        startActivity(this, DateTimeTestActivity.class);
    }

    @OnClick(R.id.btn_sheet)
    public void btnSheet(){
        startActivity(this, SheetTestActivity.class);
    }

    @OnClick(R.id.btn_listView1)
    public void btnListView1(){
        startActivity(this, ListViewTest1Activity.class);
    }

    @OnClick(R.id.btn_listView2)
    public void btnListView2(){
        startActivity(this, ListViewTest2Activity.class);
    }

    @OnClick(R.id.btn_listView_item_type)
    public void btnListViewItemType(){
        startActivity(this, ListViewMoreItemTypeTestActivity.class);
    }

    @OnClick(R.id.btn_pull_to_refresh1)
    public void btnPullToRefresh1(){
        startActivity(this, RefreshTest1Activity.class);
    }

    @OnClick(R.id.btn_pull_to_refresh2)
    public void btnPullToRefresh2(){
        startActivity(this, RefreshTest2Activity.class);
    }

    @OnClick(R.id.btn_pull_to_refresh3)
    public void btnPullToRefresh3(){
        startActivity(this, RefreshTest3Activity.class);
    }

    @OnClick(R.id.btn_pull_to_refresh4)
    public void btnPullToRefresh4(){
        startActivity(this, RefreshTest4Activity.class);
    }

    @OnClick(R.id.btn_tab_change_color)
    public void btnTabChange(){
        startActivity(this, IndexHomeActivity.class);
    }

    @OnClick(R.id.btn_tab_index)
    public void btnTabIndex(){
        startActivity(this, IndexTabActivity.class);
    }

    @OnClick(R.id.btn_tab_layout1)
    public void btnTabLayout1(){
        startActivity(this, TabLayoutActivity.class);
    }

    @OnClick(R.id.btn_tab_layout2)
    public void btnTabLayout2(){
        startActivity(this, TabLayoutMeActivity.class);
    }

    @OnClick(R.id.btn_palette)
    public void btnPalette(){
        startActivity(this, PaletteTestActivity.class);
    }

    @OnClick(R.id.btn_recycler1)
    public void btnRecycleView1(){
        startActivity(this, RecyclerViewTest1Activity.class);
    }

    @OnClick(R.id.btn_recycler2)
    public void btnRecycleView2(){
        startActivity(this, RecyclerViewTest2Activity.class);
    }

    @OnClick(R.id.btn_recycler3)
    public void btnRecycleView3(){
        startActivity(this, RecyclerViewTest3Activity.class);
    }

    @OnClick(R.id.btn_recycler4)
    public void btnRecycleView4(){
        startActivity(this, RecyclerViewTest4Activity.class);
    }

    @OnClick(R.id.btn_zoom1)
    public void btnPullZoom1(){
        startActivity(this, PullZoomTest1Activity.class);
    }

    @OnClick(R.id.btn_slid_menu)
    public void btnSlidMenu(){
        startActivity(this, SlidMenuTestActivity.class);
    }

    @OnClick(R.id.btn_slid_menu2)
    public void btnSlidMenu2(){
        startActivity(this, SlidMenuTabTestActivity.class);
    }

    @OnClick(R.id.btn_file_download)
    public void btnFileDownLoad(){
        startActivity(this, FileDownloadTestActivity.class);
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
                }
            }
        });
    }

}
