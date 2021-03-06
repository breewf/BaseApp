package com.hy.myapp.bean;


import com.hy.basic.BaseModel;

/**
 * @author hy
 * @date 2021/3/17
 * desc: 评测ip详情页-单品评测数据实体
 **/
public class ReviewProductArticleFeedData extends BaseModel {

    public String object_id;
    public String object_type;

    public String title;
    public String pic_path;

    public boolean is_favorite;
    public int favorite_num;
    public long publish_time;

    public boolean onlyOne;
    public boolean isFirst;
    public boolean isLast;

    public ReviewProductArticleFeedData() {

    }

}
