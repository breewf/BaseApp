package com.ghy.baseapp.base;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.company.myapp.R;
import com.ghy.baseapp.common.logger.Logger;
import com.ghy.baseapp.utils.AnimUtils;
import com.rey.material.widget.ProgressView;

import butterknife.ButterKnife;


/**
 * Created by GHY on 2016/4/29.
 * Activity基类
 */
public abstract class AbsBaseActivity extends AppCompatActivity {

    private final String TAG = "AbsBaseActivity";
    private static final int NO_LAYOUT = 0;

    private Toolbar mToolbar;
    /**
     * toolbar右侧样式
     */
    public static final int TOOLBAR_RIGHT_TYPE_DEFAULT = 0;//无样式
    public static final int TOOLBAR_RIGHT_TYPE_ITEM = 1;//item样式
    public static final int TOOLBAR_RIGHT_TYPE_IV = 2;//图片样式
    public static final int TOOLBAR_RIGHT_TYPE_TV = 3;//文字样式

    /**
     * progressBar样式
     */
    public static final int LOADING_PROGRESS_TYPE_CIRCLE = 0;//居中圆圈样式
    public static final int LOADING_PROGRESS_TYPE_LINE = 1;//底部线条样式

    /**
     * activity布局样式
     * 注意：ACTIVITY_STATUS_LOADING状态是把整个页面置为loading布局状态，其它元素不可见
     * loadingProgressShow()方法则只是在默认布局中显示loading，布局中其它元素可见
     */
    public static final int ACTIVITY_STATUS_SUCCESS = 0;//默认样式（加载默认布局）
    public static final int ACTIVITY_STATUS_LOADING = 1;//加载中样式（加载加载中布局）
    public static final int ACTIVITY_STATUS_NO_NET = 2;//无网络样式（加载无网络布局）
    public static final int ACTIVITY_STATUS_EMPTY = 3;//空页面样式（加载空页面布局）
    public static final int ACTIVITY_STATUS_ERROR = 4;//错误样式（加载错误布局）

    /**
     * 根布局View
     */
    private View mRootFrameView;
    /**
     * 根布局FrameLayout
     */
    private FrameLayout mRootFrameLayout;
    /**
     * 子类添加的View
     */
    private View mSonView;

    /**
     * 获取布局ID
     *
     * @return
     */
    protected abstract int getLayoutID();

    /**
     * 初始化
     */
    protected abstract void init();

    /**
     * 获取toolBar
     *
     * @return
     */
    protected Toolbar getToolBar() {
        return mToolbar;
    }

    /**
     * 是否打开ToolBar
     * 默认为true
     *
     * @return
     */
    protected boolean isOpenToolBar() {
        return true;
    }

    /**
     * 是否打开ToolBar左侧返回按钮
     * 默认打开
     *
     * @return
     */
    protected boolean isOpenToolBarLeftBack() {
        return true;
    }

    /**
     * 设置主标题
     *
     * @return
     */
    protected String getToolBarTitle() {
        return "";
    }

    /**
     * 设置副标题
     *
     * @return
     */
    protected String getToolBarSubTitle() {
        return "";
    }

    /**
     * ToolBar右边样式
     * 0：默认 无右边样式
     * 1：点击菜单栏样式 需自己定制
     * 2：文本样式
     * 3：图标样式
     * 可根据需求自定义样式
     */
    protected int setToolBarRightType() {
        return 0;
    }

    /**
     * 设置toolBar右侧图片
     *
     * @return
     */
    protected int setToolBarRightIv() {
        return 0;
    }

    /**
     * 设置toolBar右侧图片长按时的提示信息
     *
     * @return
     */
    protected String setToolBarRightIvTitle() {
        return "";
    }

    /**
     * 设置toolBar右侧文案
     *
     * @return
     */
    protected String setToolBarRightTv() {
        return "";
    }

    /**
     * 设置loadingProgress样式类型
     * 默认为居中转圈样式
     *
     * @return
     */
    protected int setLoadingProgressType() {
        return LOADING_PROGRESS_TYPE_CIRCLE;
    }

    /**
     * 无网络点击事件
     *
     * @param view
     */
    protected void onNoNetClick(View view) {
    }

    /**
     * 空页面点击事件
     *
     * @param view
     */
    protected void onEmptyClick(View view) {
    }

    /**
     * 错误面点击事件
     *
     * @param view
     */
    protected void onErrorClick(View view) {
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //保持屏幕竖屏
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        int id = getLayoutID();
        //加载根布局
        mRootFrameView = getLayoutInflater().inflate(R.layout.abs_base_activity, null);
        mRootFrameLayout = (FrameLayout) mRootFrameView.findViewById(R.id.abs_base_frame_layout);
        //设置布局
        setContentView(mRootFrameView);
        //加载子类布局
        if (id != NO_LAYOUT) {
            mSonView = getLayoutInflater().inflate(id, null);
            mRootFrameLayout.addView(mSonView);
        } else {
            Logger.e("mSonView == null");
        }
        //初始化ToolBar
        initToolBar();
        //初始化基础控件
        initBase(mRootFrameView);
        //初始化ButterKnife
        ButterKnife.bind(this);
        //初始化
        init();
    }

    /**
     * 设置activity布局界面状态
     * 备注：这几种布局状态和正常状态每次仅存在一种
     */
    View loadingLayoutView;//加载中布局
    View noNetLayoutView;//无网络布局
    View emptyLayoutView;//空页面布局
    View errorLayoutView;//错误页面布局

    public void setActivityStatus(int activityStatus) {
        //取消加载中loadingProgress
        //无论加载哪一种布局，都相当于成功加载了界面，取消loadingProgress
        loadingProgressDismiss();
        switch (activityStatus) {
            case ACTIVITY_STATUS_SUCCESS:
                if (loadingLayoutView != null) mRootFrameLayout.removeView(loadingLayoutView);
                if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
                if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
                if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
                mSonView.setVisibility(View.VISIBLE);
                //添加淡入动画
                AnimUtils.fadeIn(mSonView);
                break;
            case ACTIVITY_STATUS_LOADING:
                if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
                if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
                if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
                loadingLayoutView = getLayoutInflater().inflate(R.layout.abs_base_loading_layout, null);
                ProgressView loadingProgress = (ProgressView) loadingLayoutView.findViewById(R.id.abs_loading_progress);
                mRootFrameLayout.addView(loadingLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                loadingProgressStartDelay(loadingProgress);
                break;
            case ACTIVITY_STATUS_NO_NET:
                if (loadingLayoutView != null) mRootFrameLayout.removeView(loadingLayoutView);
                if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
                if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
                noNetLayoutView = getLayoutInflater().inflate(R.layout.abs_base_no_net_layout, null);
                mRootFrameLayout.addView(noNetLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                //无网络布局点击事件
                setNoNetClickListener(noNetLayoutView);
                break;
            case ACTIVITY_STATUS_EMPTY:
                if (loadingLayoutView != null) mRootFrameLayout.removeView(loadingLayoutView);
                if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
                if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
                emptyLayoutView = getLayoutInflater().inflate(R.layout.abs_base_empty_layout, null);
                mRootFrameLayout.addView(emptyLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                //空页面布局点击事件
                setEmptyClickListener(emptyLayoutView);
                break;
            case ACTIVITY_STATUS_ERROR:
                if (loadingLayoutView != null) mRootFrameLayout.removeView(loadingLayoutView);
                if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
                if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
                errorLayoutView = getLayoutInflater().inflate(R.layout.abs_base_error_layout, null);
                mRootFrameLayout.addView(errorLayoutView);
                mSonView.setVisibility(View.INVISIBLE);
                //错误页面布局点击事件
                setErrorClickListener(errorLayoutView);
                break;
        }
    }

    /**
     * loadingProgress延迟转动
     *
     * @param loadingProgress
     */
    private void loadingProgressStartDelay(final ProgressView loadingProgress) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingProgress.start();
            }
        }, 200);
    }

    /**
     * 无网络布局点击事件
     *
     * @param noNetLayoutView
     */
    private void setNoNetClickListener(View noNetLayoutView) {
        if (noNetLayoutView != null) noNetLayoutView.setOnClickListener(new NoNetClickListener());
    }

    private class NoNetClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (noNetLayoutView != null) mRootFrameLayout.removeView(noNetLayoutView);
            loadingProgressShow();
            onNoNetClick(v);
        }
    }

    /**
     * 空页面布局点击事件
     *
     * @param emptyLayoutView
     */
    private void setEmptyClickListener(View emptyLayoutView) {
        if (emptyLayoutView != null) emptyLayoutView.setOnClickListener(new EmptyClickListener());
    }

    private class EmptyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (emptyLayoutView != null) mRootFrameLayout.removeView(emptyLayoutView);
            loadingProgressShow();
            onEmptyClick(v);
        }
    }

    /**
     * 错误页面布局点击事件
     *
     * @param errorLayoutView
     */
    private void setErrorClickListener(View errorLayoutView) {
        if (errorLayoutView != null) errorLayoutView.setOnClickListener(new ErrorClickListener());
    }

    private class ErrorClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (errorLayoutView != null) mRootFrameLayout.removeView(errorLayoutView);
            loadingProgressShow();
            onErrorClick(v);
        }
    }

    /**
     * 初始化基础控件
     */
    private ProgressView mProgressCircle;
    private ProgressView mProgressLine;

    private void initBase(View rootView) {
        mProgressCircle = (ProgressView) rootView.findViewById(R.id.abs_base_progressbar_circle);
        mProgressLine = (ProgressView) rootView.findViewById(R.id.abs_base_progressbar_line);
    }

    /**
     * 显示LoadingProgressBar
     */
    public void loadingProgressShow() {
        switch (setLoadingProgressType()) {
            case LOADING_PROGRESS_TYPE_CIRCLE:
                mProgressCircle.setVisibility(View.VISIBLE);
                mProgressCircle.start();
                break;
            case LOADING_PROGRESS_TYPE_LINE:
                mProgressLine.setVisibility(View.VISIBLE);
                mProgressLine.start();
                break;
            default:
                mProgressCircle.setVisibility(View.VISIBLE);
                mProgressCircle.start();
                break;
        }
    }

    /**
     * 取消LoadingProgressBar
     */
    public void loadingProgressDismiss() {
        mProgressCircle.setVisibility(View.GONE);
        mProgressCircle.stop();
        mProgressLine.setVisibility(View.GONE);
        mProgressLine.stop();
    }

    /**
     * 初始化ToolBar
     */
    private void initToolBar() {
        mToolbar = (Toolbar) findViewById(R.id.abs_base_toolbar);
        mToolbar.setTitle(getToolBarTitle() != null ? getToolBarTitle() : "");
        mToolbar.setSubtitle(getToolBarSubTitle() != null ? getToolBarSubTitle() : "");
        mToolbar.setVisibility(isOpenToolBar() ? View.VISIBLE : View.GONE);
        setSupportActionBar(mToolbar);
        //是否开启返回箭头按钮
        getSupportActionBar().setDisplayHomeAsUpEnabled(isOpenToolBarLeftBack());
    }

    /**
     * 开启activity
     *
     * @param context
     * @param activity
     */
    public void startActivity(Context context, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        startActivity(intent);
    }


    /**
     * toolBar右侧菜单栏
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        switch (setToolBarRightType()) {
            case TOOLBAR_RIGHT_TYPE_DEFAULT:
                return super.onCreateOptionsMenu(menu);
            case TOOLBAR_RIGHT_TYPE_ITEM:
                getMenuInflater().inflate(R.menu.abs_menu_main_item, menu);
                return true;
            case TOOLBAR_RIGHT_TYPE_IV:
                getMenuInflater().inflate(R.menu.abs_menu_main_iv, menu);
                if (setToolBarRightIv() != 0)
                    menu.getItem(0).setIcon(getResources().getDrawable(setToolBarRightIv()));
                if (!TextUtils.isEmpty(setToolBarRightIvTitle()))
                    menu.getItem(0).setTitle(setToolBarRightIvTitle());
                return true;
            case TOOLBAR_RIGHT_TYPE_TV:
                getMenuInflater().inflate(R.menu.abs_menu_main_tv, menu);
                if (!TextUtils.isEmpty(setToolBarRightTv()))
                    menu.getItem(0).setTitle(setToolBarRightTv());
                return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            //左上角返回按钮
            case android.R.id.home:
                this.finish();
                break;
            case R.id.abs_menu_main_iv:
                if (rightIvClickListener != null) rightIvClickListener.onToolBarRightIvClick();
                break;
            case R.id.abs_menu_main_tv:
                if (rightTvClickListener != null) rightTvClickListener.onToolBarRightTvClick();
                break;
            case R.id.abs_menu_main_item1:
                if (rightItemClickListener != null)
                    rightItemClickListener.onToolBarRightItemClick(R.id.abs_menu_main_item1);
                break;
            case R.id.abs_menu_main_item2:
                if (rightItemClickListener != null)
                    rightItemClickListener.onToolBarRightItemClick(R.id.abs_menu_main_item2);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * toolBar右侧菜单点击监听
     */
    private ToolBarRightIvClickListener rightIvClickListener;
    private ToolBarRightTvClickListener rightTvClickListener;
    private ToolBarRightItemClickListener rightItemClickListener;

    public void setOnToolBarRightIvClickListener(ToolBarRightIvClickListener listener) {
        rightIvClickListener = listener;
    }

    public void setOnToolBarRightTvClickListener(ToolBarRightTvClickListener listener) {
        rightTvClickListener = listener;
    }

    public void setOnToolBarRightItemClickListener(ToolBarRightItemClickListener listener) {
        rightItemClickListener = listener;
    }

    public interface ToolBarRightIvClickListener {
        void onToolBarRightIvClick();
    }

    public interface ToolBarRightTvClickListener {
        void onToolBarRightTvClick();
    }

    public interface ToolBarRightItemClickListener {
        void onToolBarRightItemClick(int itemId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.unbind(this);
    }
}
