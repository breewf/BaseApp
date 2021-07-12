package com.hy.baseapp.base;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import com.hy.baseapp.common.logger.Logger;
import com.hy.myapp.R;

import butterknife.ButterKnife;

/**
 * Created by HY on 2016/4/30.
 * fragment基类
 */
public abstract class AbsBaseFragment extends Fragment {

    private final String TAG = "AbsBaseFragment";
    private static final int NO_FRAGMENT_LAYOUT = 0;

    /**
     * 根布局view 和 子类添加内容布局view
     */
    private View mRootView, mContentView;

    /**
     * fragment根布局FrameLayout
     */
    private FrameLayout mRootFrameLayout;

    /**
     * progressBar样式
     */
    public static final int LOADING_PROGRESS_TYPE_CIRCLE = 0;//居中圆圈样式
    public static final int LOADING_PROGRESS_TYPE_LINE = 1;//底部线条样式

    /**
     * activity布局样式
     * 注意：FRAGMENT_STATUS_LOADING状态是把整个页面置为loading布局状态，其它元素不可见
     * loadingProgressShow()方法则只是在默认布局中显示loading，布局中其它元素可见
     */
    public static final int FRAGMENT_STATUS_SUCCESS = 0;//默认样式（加载默认布局）
    public static final int FRAGMENT_STATUS_LOADING = 1;//加载中样式（加载加载中布局）
    public static final int FRAGMENT_STATUS_NO_NET = 2;//无网络样式（加载无网络布局）
    public static final int FRAGMENT_STATUS_EMPTY = 3;//空页面样式（加载空页面布局）
    public static final int FRAGMENT_STATUS_ERROR = 4;//错误样式（加载错误布局）

    /**
     * 获取布局ID
     *
     * @return
     */
    protected abstract int getLayoutID();

    /**
     * 初始化
     *
     * @return
     */
    protected abstract void init(Bundle savedInstanceState, View contentView);

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

    /**
     * 设置loadingProgress样式类型
     * 默认为居中转圈样式
     *
     * @return
     */
    protected int setLoadingProgressType() {
        return LOADING_PROGRESS_TYPE_CIRCLE;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int contentLayoutId = getLayoutID();
        mRootView = inflater.inflate(R.layout.abs_base_fragment, container, false);
        mRootFrameLayout = (FrameLayout) mRootView.findViewById(R.id.abs_base_frame_layout_fg);
        if (contentLayoutId != NO_FRAGMENT_LAYOUT) {
            mContentView = inflater.inflate(contentLayoutId, null);
            mRootFrameLayout.addView(mContentView);
        } else {
            Logger.w("fragmentContentView == null");
        }
        //初始化基础控件
        initBase(mRootView);
        //初始化ButterKnife
        ButterKnife.bind(this, mRootView);
        return mRootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(savedInstanceState, mContentView);
    }

    /**
     * 注意：此方法和逻辑跟activity一致
     * <p>
     * 设置fragment布局界面状态
     * 备注：这几种布局状态和正常状态每次仅存在一种
     */
    View loadingLayoutView;//加载中布局
    View noNetLayoutView;//无网络布局
    View emptyLayoutView;//空页面布局
    View errorLayoutView;//错误页面布局

    public void setFragmentStatus(int fragmentStatus) {
        //取消加载中loadingProgress
        //无论加载哪一种布局，都相当于成功加载了界面，取消loadingProgress
        loadingProgressDismiss();
        switch (fragmentStatus) {
            case FRAGMENT_STATUS_SUCCESS:
                if (loadingLayoutView != null) {
                    mRootFrameLayout.removeView(loadingLayoutView);
                }
                if (noNetLayoutView != null) {
                    mRootFrameLayout.removeView(noNetLayoutView);
                }
                if (emptyLayoutView != null) {
                    mRootFrameLayout.removeView(emptyLayoutView);
                }
                if (errorLayoutView != null) {
                    mRootFrameLayout.removeView(errorLayoutView);
                }
                mContentView.setVisibility(View.VISIBLE);
                break;
            case FRAGMENT_STATUS_LOADING:
                if (noNetLayoutView != null) {
                    mRootFrameLayout.removeView(noNetLayoutView);
                }
                if (emptyLayoutView != null) {
                    mRootFrameLayout.removeView(emptyLayoutView);
                }
                if (errorLayoutView != null) {
                    mRootFrameLayout.removeView(errorLayoutView);
                }
                loadingLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_loading_layout, null);
                ProgressBar loadingProgress = (ProgressBar) loadingLayoutView.findViewById(R.id.abs_loading_progress);
                mRootFrameLayout.addView(loadingLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                loadingProgressStartDelay(loadingProgress);
                break;
            case FRAGMENT_STATUS_NO_NET:
                if (loadingLayoutView != null) {
                    mRootFrameLayout.removeView(loadingLayoutView);
                }
                if (emptyLayoutView != null) {
                    mRootFrameLayout.removeView(emptyLayoutView);
                }
                if (errorLayoutView != null) {
                    mRootFrameLayout.removeView(errorLayoutView);
                }
                noNetLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_no_net_layout, null);
                mRootFrameLayout.addView(noNetLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                //无网络布局点击事件
                setNoNetClickListener(noNetLayoutView);
                break;
            case FRAGMENT_STATUS_EMPTY:
                if (loadingLayoutView != null) {
                    mRootFrameLayout.removeView(loadingLayoutView);
                }
                if (noNetLayoutView != null) {
                    mRootFrameLayout.removeView(noNetLayoutView);
                }
                if (errorLayoutView != null) {
                    mRootFrameLayout.removeView(errorLayoutView);
                }
                emptyLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_empty_layout, null);
                mRootFrameLayout.addView(emptyLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                //空页面布局点击事件
                setEmptyClickListener(emptyLayoutView);
                break;
            case FRAGMENT_STATUS_ERROR:
                if (loadingLayoutView != null) {
                    mRootFrameLayout.removeView(loadingLayoutView);
                }
                if (noNetLayoutView != null) {
                    mRootFrameLayout.removeView(noNetLayoutView);
                }
                if (emptyLayoutView != null) {
                    mRootFrameLayout.removeView(emptyLayoutView);
                }
                errorLayoutView = getActivity().getLayoutInflater().inflate(R.layout.abs_base_error_layout, null);
                mRootFrameLayout.addView(errorLayoutView);
                mContentView.setVisibility(View.INVISIBLE);
                //错误页面布局点击事件
                setErrorClickListener(errorLayoutView);
                break;
            default:
                break;
        }
    }

    /**
     * loadingProgress延迟转动
     *
     * @param loadingProgress
     */
    private void loadingProgressStartDelay(final ProgressBar loadingProgress) {
        //new Handler().postDelayed(new Runnable() {
        //    @Override
        //    public void run() {
        //
        //    }
        //}, 200);
    }

    /**
     * 无网络布局点击事件
     *
     * @param noNetLayoutView
     */
    private void setNoNetClickListener(View noNetLayoutView) {
        if (noNetLayoutView != null) {
            noNetLayoutView.setOnClickListener(new NoNetClickListener());
        }
    }

    private class NoNetClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (noNetLayoutView != null) {
                mRootFrameLayout.removeView(noNetLayoutView);
            }
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
        if (emptyLayoutView != null) {
            emptyLayoutView.setOnClickListener(new EmptyClickListener());
        }
    }

    private class EmptyClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (emptyLayoutView != null) {
                mRootFrameLayout.removeView(emptyLayoutView);
            }
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
        if (errorLayoutView != null) {
            errorLayoutView.setOnClickListener(new ErrorClickListener());
        }
    }

    private class ErrorClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            //点击后清除该布局并显示loadingProgress
            if (errorLayoutView != null) {
                mRootFrameLayout.removeView(errorLayoutView);
            }
            loadingProgressShow();
            onErrorClick(v);
        }
    }

    /**
     * 初始化基础控件
     */
    private ProgressBar mProgressCircleFg;
    private ProgressBar mProgressLineFg;

    private void initBase(View rootView) {
        mProgressCircleFg = (ProgressBar) rootView.findViewById(R.id.abs_base_progressbar_circle_fg);
        mProgressLineFg = (ProgressBar) rootView.findViewById(R.id.abs_base_progressbar_line_fg);
    }

    /**
     * 显示LoadingProgressBar
     */
    public void loadingProgressShow() {
        switch (setLoadingProgressType()) {
            case LOADING_PROGRESS_TYPE_CIRCLE:
                mProgressCircleFg.setVisibility(View.VISIBLE);
                break;
            case LOADING_PROGRESS_TYPE_LINE:
                mProgressLineFg.setVisibility(View.VISIBLE);
                break;
            default:
                mProgressCircleFg.setVisibility(View.VISIBLE);
                break;
        }
    }

    /**
     * 取消LoadingProgressBar
     */
    public void loadingProgressDismiss() {
        mProgressCircleFg.setVisibility(View.GONE);
        mProgressLineFg.setVisibility(View.GONE);
    }

}
