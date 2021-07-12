package com.hy.baseapp.adapter;

import android.util.SparseArray;
import android.view.View;

/**
 * Created by GHY on 2016/5/4.
 * ViewHolder
 */
public class ViewHolder {

    /**
     * ImageView view = ViewHolder.get(mImageView, R.id.imageView);
     * @param view
     * @param id
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T extends View> T get(View view, int id) {
        SparseArray<View> viewHolder = (SparseArray<View>) view.getTag();
        if (viewHolder == null) {
            viewHolder = new SparseArray<>();
            view.setTag(viewHolder);
        }
        View childView = viewHolder.get(id);
        if (childView == null) {
            childView = view.findViewById(id);
            viewHolder.put(id, childView);
        }
        return (T) childView;
    }

}
