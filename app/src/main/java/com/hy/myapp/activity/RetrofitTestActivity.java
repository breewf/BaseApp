package com.hy.myapp.activity;

import android.widget.TextView;

import com.hy.baseapp.base.AbsBaseActivity;
import com.hy.baseapp.common.Toasts;
import com.hy.basic.network.ResponseSubscriber;
import com.hy.myapp.R;
import com.hy.myapp.api.ApiDataRepo;
import com.hy.myapp.bean.ReviewProductData;

import butterknife.Bind;

/**
 * @author hy
 * @date 2021/7/12
 * desc:
 **/
public class RetrofitTestActivity extends AbsBaseActivity {

    @Bind(R.id.tv) TextView mTextView;

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

        new ApiDataRepo().getReviewProductDetail("66",

                new ResponseSubscriber<ReviewProductData>() {
                    @Override
                    public void onNext(ReviewProductData reviewProductData) {

                        if (reviewProductData == null) {
                            Toasts.showShort("获取数据为空");
                            return;
                        }

                        mTextView.setText(reviewProductData.toString());
                    }

                    @Override
                    public void onError(Throwable throwable) {
                        super.onError(throwable);
                        Toasts.showShort(throwable.getMessage());
                        //mTextView.setText(throwable.getMessage());
                    }
                });
    }

}
