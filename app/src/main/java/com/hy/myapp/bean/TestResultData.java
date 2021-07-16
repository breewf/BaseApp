package com.hy.myapp.bean;

import com.hy.basic.BaseModel;

/**
 * @author hy
 * @date 2021/7/16
 * desc: TestResultData
 **/
public class TestResultData extends BaseModel {

    public String sid;
    public String text;
    public String video;
    public String header;
    public String uid;
    public String name;

    @Override
    public String toString() {
        return "TestResultData{" +
                "sid='" + sid + '\'' +
                ", text='" + text + '\'' +
                ", video='" + video + '\'' +
                ", header='" + header + '\'' +
                ", uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
