package com.hy.myapp.adapter;

import android.content.Context;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.chad.library.adapter4.BaseMultiItemAdapter;
import com.chad.library.adapter4.viewholder.QuickViewHolder;
import com.hy.myapp.R;

import java.util.List;

/**
 * Created by GHY on 2016/5/17.
 * <p>
 * 已从本地 fork 的 BaseMultiItemQuickAdapter 迁移到 BRVAH 4.x 的 BaseMultiItemAdapter：
 * - 多布局不再用 addItmeType(type, layoutId)，而是 addItemType(type, listener)；
 * - 每种类型通过 OnMultiItemAdapterListener 分别负责创建与绑定各自的 ViewHolder；
 * - 类型判断由 onItemViewType 回调根据数据返回。
 */
public class MyRecyclerViewAdapterMultiple extends BaseMultiItemAdapter<MultipleItem> {

    public MyRecyclerViewAdapterMultiple(Context context, List<MultipleItem> data) {
        super(data);
        onItemViewType((position, list) -> list.get(position).getItemType());

        addItemType(MultipleItem.LETTER, new BaseMultiItemAdapter.OnMultiItemAdapterListener<MultipleItem, QuickViewHolder>() {
            @NonNull
            @Override
            public QuickViewHolder onCreate(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
                return new QuickViewHolder(R.layout.item_list_view_item_type_letter, parent);
            }

            @Override
            public void onBind(@NonNull QuickViewHolder holder, int position, @Nullable MultipleItem item) {
                holder.setText(R.id.item_tv_letter, item == null ? null : item.getLetter());
            }
        });

        addItemType(MultipleItem.CONTENT, new BaseMultiItemAdapter.OnMultiItemAdapterListener<MultipleItem, QuickViewHolder>() {
            @NonNull
            @Override
            public QuickViewHolder onCreate(@NonNull Context context, @NonNull ViewGroup parent, int viewType) {
                return new QuickViewHolder(R.layout.item_list_view_item_type_content, parent);
            }

            @Override
            public void onBind(@NonNull QuickViewHolder holder, int position, @Nullable MultipleItem item) {
                holder.setText(R.id.item_tv_content, item == null ? null : item.getContent());
            }
        });
    }
}
