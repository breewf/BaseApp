package com.ghy.baseapp.base;

import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.company.myapp.R;
import com.ghy.baseapp.common.log.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GHY on 2016/5/10.
 * AbsBaseRefreshListFragment基类
 * 谷歌下拉刷新ListView
 */
public abstract class AbsBaseRefreshListFragment extends AbsBaseFragment
        implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    private static final String TAG = "AbsBaseRefreshListFragment";
    private static final int NO_LAYOUT = 0;

    private ListView listView;

    /**
     * 谷歌下拉刷新控件
     */
    private SwipeRefreshLayout mRefreshLayout;

    /**
     * MyListView adapter
     */
    private MyListAdapter mListAdapter;

    protected ListView getListView() {
        return listView;
    }

    /**
     * 设置ListView header 布局
     *
     * @return
     */
    protected int getHeaderLayout() {
        return 0;
    }

    /**
     * 设置ListView footer 布局
     *
     * @return
     */
    protected int getFooterLayout() {
        return 0;
    }

    /**
     * 设置listView分割线高度
     *
     * @return
     */
    protected int getDividerHeight() {
        return -1;
    }

    /**
     * 设置 ListView type
     */
    protected int getAbsItemViewType(int position) {
        return 0;
    }

    /**
     * 设置ListView type count
     * 默认为1
     */
    protected int getAbsViewTypeCount() {
        return 1;
    }

    protected AbsAdapterItem getAbsAdapterItem(int type) {
        return null;
    }

    protected abstract AbsAdapterItem getAbsAdapterItem();

    protected abstract void init();

    /**
     * 下拉刷新完成
     */
    protected void setOnRefreshComplete(List list) {
        setData(list);
        mRefreshLayout.setRefreshing(false);
    }

    /**
     * 开始刷新
     */
    abstract protected void onRefreshStart();

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    protected int getLayoutID() {
        return R.layout.abs_base_refresh_list;
    }

    @Override
    protected void init(Bundle savedInstanceState, View contentView) {
        mRefreshLayout = (SwipeRefreshLayout) contentView;
        listView = (ListView) contentView.findViewById(R.id.abs_base_refresh_list);

        int dividerHeight = getDividerHeight();
        if (dividerHeight != -1) listView.setDividerHeight(dividerHeight);

        if (getHeaderLayout() != NO_LAYOUT) {
            View headerView = LayoutInflater.from(getContext()).inflate(getHeaderLayout(), null, false);
            listView.addHeaderView(headerView);
        }
        if (getFooterLayout() != NO_LAYOUT) {
            View footerView = LayoutInflater.from(getContext()).inflate(getFooterLayout(), null, false);
            listView.addFooterView(footerView);
        }

        //设置adapter
        mListAdapter = new MyListAdapter();
        listView.setAdapter(mListAdapter);
        //设置item点击监听
        listView.setOnItemClickListener(this);
        listView.setOnItemLongClickListener(this);
        //下拉刷新监听
        mRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                onRefreshStart();
            }
        });
        init();
    }

    /**
     * ListView adapter 非空检查
     *
     * @return
     */
    private boolean isAdapterNotNull() {
        if (mListAdapter == null) {
            Log.e("mListAdapter is null!");
            return false;
        }
        return true;
    }

    /**
     * 为 ListView 设置数据
     *
     * @param list
     */
    protected void setData(List list) {
        if (isAdapterNotNull()) {
            mListAdapter.setList(list);
            update();
        }
    }

    /**
     * 更新ListView
     */
    protected void update() {
        if (isAdapterNotNull()) {
            mListAdapter.notifyDataSetChanged();
            setFragmentStatus(FRAGMENT_STATUS_SUCCESS);
        }
    }

    /**
     * 适配器
     * ListView Adapter
     */
    private class MyListAdapter extends BaseAdapter {

        //数据源
        private List mList;

        private MyListAdapter() {
            mList = new ArrayList();
        }

        private void setList(List list) {
            mList = null;
            mList = list;
        }

        private void addList(List list) {
            mList.addAll(list);
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return getAbsItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return getAbsViewTypeCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbsAdapterItem item = null;
            if (convertView == null) {
                //获取item类型数量
                int count = getViewTypeCount();
                if (count != 1) {
                    int type = getItemViewType(position);
                    item = getAbsAdapterItem(type);
                } else {
                    item = getAbsAdapterItem();
                }
                convertView = LayoutInflater.from(getContext()).inflate(item.getItemLayout(), null, false);
                item.init(convertView);
                convertView.setTag(item);
            }
            if (item == null) item = (AbsAdapterItem) convertView.getTag();
            item.bindData(mList.get(position));
            return convertView;
        }
    }
}
