package com.hy.myapp.activity;

import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.common.Toasts;
import com.hy.baseapp.base.net.ResponseSubscriber;
import com.hy.myapp.R;
import com.hy.myapp.api.ApiDataRepo;
import com.hy.myapp.bean.TestResultData;
import com.hy.myapp.databinding.ActivityRetrofitTestBinding;
import com.trello.rxlifecycle.android.ActivityEvent;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class RetrofitTestActivity extends AbsBaseActivity {

    private ActivityRetrofitTestBinding binding;

    @Override
    protected int getLayoutID() {
        return R.layout.activity_retrofit_test;
    }

    @Override
    protected String getToolBarTitle() {
        return "Retrofit网络请求";
    }

    @Override
    protected void init() {

        binding = ActivityRetrofitTestBinding.bind(getRootView());

        new ApiDataRepo().getApiDataTest("")
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new ResponseSubscriber<>() {
                    @Override
                    public void onNext(TestResultData resultData) {
                        if (resultData == null) {
                            Toasts.showShort("获取数据为空");
                            return;
                        }
                        binding.tv1.setText(resultData.toString());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        Toasts.showShort(throwable.getMessage());
                        binding.tv1.setText(throwable.getMessage());
                    }
                });

        new ApiDataRepo().getApiDataTest("")
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new ResponseSubscriber<>() {
                    @Override
                    public void onNext(TestResultData resultData) {
                        if (resultData == null) {
                            Toasts.showShort("获取数据为空");
                            return;
                        }
                        binding.tv2.setText(resultData.toString());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        Toasts.showShort(throwable.getMessage());
                        binding.tv2.setText(throwable.getMessage());
                    }
                });
    }

}
