package com.hy.myapp.adapter;

/**
 * <a href="https://github.com/CymChad/BaseRecyclerViewAdapterHelper">...</a>
 * <p>
 * 原继承自本地 fork 的 MultiItemEntity，现 BRVAH 4.x 已移除该接口，
 * 多类型改由 BaseMultiItemAdapter 的 onItemViewType 回调决定，
 * 因此这里自带 itemType 字段供适配器判断布局类型。
 */
public class MultipleItem {
    public static final int LETTER = 1;
    public static final int CONTENT = 2;

    private int itemType;
    private String letter;
    private String content;

    public MultipleItem() {
    }

    public MultipleItem(int itemType) {
        this.itemType = itemType;
    }

    public int getItemType() {
        return itemType;
    }

    public void setItemType(int itemType) {
        this.itemType = itemType;
    }

    public String getLetter() {
        return letter;
    }

    public void setLetter(String letter) {
        this.letter = letter;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
