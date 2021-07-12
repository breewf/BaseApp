package com.hy.myapp.bean;

import com.google.gson.annotations.SerializedName;
import com.hy.basic.BaseModel;

/**
 * 相关产品实体
 */
public class HXRelationProductData extends BaseModel {

    @SerializedName("review_product_id")
    public String reviewProductId;
    /*ip 名称*/
    public String name;
    @SerializedName("pic_path")
    public String picPath;
    /*星级*/
    @SerializedName("score_level")
    public String scoreLevel;
    /*评分*/
    @SerializedName(value = "_score", alternate = "score")
    public String score;
    /*ip对应的内容数量*/
    @SerializedName("number_of_contents")
    public int numberOfContents;
}
