package com.hy.myapp.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseQuickAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.hy.myapp.R;

import java.util.List;

/**
 * Created by GHY on 2016/5/16.
 * <p>
 * 已从本地 fork（com.hy.baseapp.adapter）迁移到 BRVAH 4.x：
 * - 泛型需同时声明数据类型与 ViewHolder 类型；
 * - 不再有 convert()/BaseViewHolder，改为自行实现 onCreateViewHolder + onBindViewHolder；
 * - QuickViewHolder 提供了与旧 helper 等价的 setText(viewId, ...) 便捷方法。
 */
public class MyRecyclerAdapterTest extends BaseQuickAdapter<String, QuickViewHolder> {

    public MyRecyclerAdapterTest(Context context, List<String> data) {
        super(data);
    }

    @NonNull
    @Override
    protected QuickViewHolder onCreateViewHolder(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
        return new QuickViewHolder(R.layout.item_recycler_view_test1, parent);
    }

    @Override
    protected void onBindViewHolder(@NonNull QuickViewHolder holder, int position, @Nullable String item) {
        holder.setText(R.id.recycler_view_text, item);
    }
}
