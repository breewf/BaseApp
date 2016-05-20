package com.company.myapp.adapter;


import com.ghy.baseapp.adapter.baserecycler.entity.MultiItemEntity;

/**
 * https://github.com/CymChad/BaseRecyclerViewAdapterHelper
 */
public class MultipleItem extends MultiItemEntity {
    public static final int LETTER = 1;
    public static final int CONTENT = 2;

    private String letter;
    private String content;

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
