package com.hy.myapp.bean;

import androidx.annotation.NonNull;

import com.hy.basic.BaseModel;

/**
 * @author hy
 * @date 2021/7/16
 * desc: TestResultData
 **/
public class TestResultData extends BaseModel {

    public String from;
    public String name;

    @NonNull
    @Override
    public String toString() {
        return "TestResultData{" +
                "from='" + from + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
