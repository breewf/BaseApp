package com.hy.myapp.demo.network

import com.hy.basic.BaseModel


/**
 * @author hy
 * @date 2021/9/27
 * desc: famous-sentence 接口的业务数据体,对应信封 data 字段 {"from":..,"name":..}。
 * 注意:这里只描述 data 里的内容,code/message 属于外层信封(BaseHttpResponse),不要重复声明,
 * 否则 Gson 会把 data 当信封解析导致字段全为 null。
 **/
class TestResultDataKt : BaseModel() {
    var from: String? = null
    var name: String? = null
}

/** random-string 接口的业务数据体,对应信封 data 字段 {"string":..}(data 是对象而非裸字符串) */
class RandomStringData : BaseModel() {
    var string: String? = null
}

