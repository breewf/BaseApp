package com.hy.myapp.bean;

import com.hy.basic.BaseModel;

import java.util.List;

/**
 * @author hy
 * @date 2021/3/17
 * desc: 评测ip数据实体
 **/
public class ReviewProductData extends BaseModel {

    /**
     * id
     */
    public String review_product_id;
    public String name;

    public String pic_path;
    public String content;

    /**
     * 用户的评分
     */
    public String user_score_level;

    /**
     * 得分0-10
     */
    public String score;

    /**
     * 图片数量, 包括视频
     */
    public String album_counts;

    public ScoreRule score_rule;
    public List<AlbumTabs> album_tabs;
    public List<Star> star_list;
    public List<ProductResourceData> review_resource;
    public BestViews best_views;
    public ReviewArticles articles;
    public List<HXRelationProductData> relation_product_list;

    public ReviewProductData() {

    }

    public static class ScoreRule extends BaseModel {
        public String title;
        public String content;
    }

    public static class AlbumTabs extends BaseModel {
        public int album_category_id;
        public String name;
    }

    public static class Star extends BaseModel {
        public int star_level;
        public int vote_num;
    }

    public static class BestViews extends BaseModel {
        public int total;
        public int hot_total;
        public List<ReviewProductCommentData> datalist;
    }

    public static class ReviewArticles extends BaseModel {
        public int total;
        public int last_id;
        public List<ReviewProductArticleFeedData> datalist;
    }
}
