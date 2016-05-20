package com.ghy.baseapp.api;

import java.util.HashMap;

/**
 * Created by GHY on 2016/5/3.
 */
public class Header {

    //http header
    private static HashMap<String, String> header = new HashMap<>();
    public static HashMap<String, String> getHeader() {
//        header.put("Accept-Encoding","gzip, deflate");
        return header;
    }

}
