package com.company.myapp.fragment;

import android.os.Handler;

import com.company.myapp.adapter.MultipleItem;
import com.company.myapp.adapter.MyRecyclerViewAdapterMultiple;
import com.ghy.baseapp.adapter.baserecycler.BaseQuickAdapter;
import com.ghy.baseapp.base.AbsBaseRefreshRecyclerFragment;
import com.ghy.baseapp.helper.ToastHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by HY on 2016/5/1.
 */
public class RecyclerViewTestFragment2 extends AbsBaseRefreshRecyclerFragment {


    private List<MultipleItem> mList;
    private MultipleItem multipleItem;

    private String[] mLetter = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    @Override
    protected BaseQuickAdapter getAbsQuickAdapter() {
        return new MyRecyclerViewAdapterMultiple(getActivity(),mList);
    }

    @Override
    protected void init() {
        mList = new ArrayList<>();
        for (int i = 0; i < mLetter.length; i++) {
            multipleItem = new MultipleItem();
            //setItemType一定要设置
            multipleItem.setItemType(MultipleItem.LETTER);
            multipleItem.setLetter(mLetter[i]);
            mList.add(multipleItem);
            for (int j = 0; j < 5; j++) {
                multipleItem = new MultipleItem();
                //setItemType一定要设置
                multipleItem.setItemType(MultipleItem.CONTENT);
                multipleItem.setContent(mLetter[i] + j + "");
                mList.add(multipleItem);
            }
        }

        setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (mList.get(position).getLetter() == null){
                    ToastHelper.getInstance().showToast("点击了"+mList.get(position).getContent());
                }else {
                    ToastHelper.getInstance().showToast("点击了"+mList.get(position).getLetter());
                }
            }
        });
    }

    @Override
    protected boolean isOpenItemDecoration() {
        return false;
    }

    @Override
    protected void onRefreshStart() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ToastHelper.getInstance().showToast("刷新完成");
                setOnRefreshComplete(mList);
            }
        },3000);
    }

    @Override
    protected boolean isOpenLoadMore() {
        return false;
    }

    @Override
    protected void onLoadMoreStart() {

    }

}
