package com.hy.myapp.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.hy.basic.BaseModel;

/**
 * @author hy
 * @date 2021/3/17
 * desc: 评测 预告图片/视频 数据实体
 **/
public class ProductResourceData extends BaseModel implements MultiItemEntity {

    public static final int TYPE_PIC = 1;
    public static final int TYPE_VIDEO = 2;

    public String pic_path;
    /**
     * 下载链接
     */
    public String download_pic_path;

    public int width;
    public int height;

    private String thumbnailUrl;
    private String originalUrl;

    private int thumbnailWidth;
    private int thumbnailHeight;

    private int originalWidth;
    private int originalHeight;

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

    public int getThumbnailWidth() {
        return thumbnailWidth;
    }

    public void setThumbnailWidth(int thumbnailWidth) {
        this.thumbnailWidth = thumbnailWidth;
    }

    public int getThumbnailHeight() {
        return thumbnailHeight;
    }

    public void setThumbnailHeight(int thumbnailHeight) {
        this.thumbnailHeight = thumbnailHeight;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public void setOriginalWidth(int originalWidth) {
        this.originalWidth = originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public void setOriginalHeight(int originalHeight) {
        this.originalHeight = originalHeight;
    }

    public void setDownloadPicPath(String downloadPicPath) {
        this.download_pic_path = downloadPicPath;
    }

    public String getDownloadPicPath() {
        return download_pic_path;
    }

    public ProductResourceData() {

    }

    @Override
    public int getItemType() {
        if (isVideo()) {
            return TYPE_VIDEO;
        } else {
            return TYPE_PIC;
        }
    }

    private boolean isVideo() {
        return false;
    }

}
