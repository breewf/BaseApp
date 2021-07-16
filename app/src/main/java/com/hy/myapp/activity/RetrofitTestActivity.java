package com.hy.myapp.activity;

import android.widget.TextView;

import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.common.Toasts;
import com.hy.basic.network.ResponseSubscriber;
import com.hy.myapp.R;
import com.hy.myapp.api.ApiDataRepo;
import com.hy.myapp.bean.TestResultData;
import com.trello.rxlifecycle.android.ActivityEvent;

import butterknife.Bind;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class RetrofitTestActivity extends AbsBaseActivity {

    @Bind(R.id.tv_2) TextView mTextView2;
    @Bind(R.id.tv_1) TextView mTextView1;

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

        new ApiDataRepo().getApiDataTest2()
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new ResponseSubscriber<String>() {
                    @Override
                    public void onNext(String string) {
                        mTextView1.setText(string);
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        Toasts.showShort(throwable.getMessage());
                        mTextView1.setText(throwable.getMessage());
                    }
                });

        new ApiDataRepo().getApiDataTest("28654780")
                .compose(bindUntilEvent(ActivityEvent.DESTROY))
                .subscribe(new ResponseSubscriber<TestResultData>() {
                    @Override
                    public void onNext(TestResultData resultData) {
                        if (resultData == null) {
                            Toasts.showShort("获取数据为空");
                            return;
                        }
                        mTextView2.setText(resultData.toString());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        Toasts.showShort(throwable.getMessage());
                        mTextView2.setText(throwable.getMessage());
                    }
                });

    }

}
