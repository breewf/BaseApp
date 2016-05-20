package com.ghy.baseapp.api;

/**
 * Created by GHY on 2016/5/3.
 * 注意：暂使用下面的数据格式封装，具体的格式以服务端数据结构为准
 * http://wthrcdn.etouch.cn/weather_mini?citykey=101010100
 *
 * {"desc":"OK","status":1000,
 * "data":{"wendu":"19","ganmao":"昼夜温差大，风力较强，易发生感冒，请注意适当增减衣服，加强自我防护避免感冒。","forecast":[{"fengxiang":"北风","fengli":"4-5级","high":"高温 27℃","type":"晴","low":"低温 10℃","date":"3日星期二"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 28℃","type":"多云","low":"低温 15℃","date":"4日星期三"},{"fengxiang":"北风","fengli":"3-4级","high":"高温 29℃","type":"多云","low":"低温 15℃","date":"5日星期四"},{"fengxiang":"北风","fengli":"4-5级","high":"高温 25℃","type":"晴","low":"低温 12℃","date":"6日星期五"},{"fengxiang":"南风","fengli":"3-4级","high":"高温 25℃","type":"晴","low":"低温 14℃","date":"7日星期六"}],"yesterday":{"fl":"4-5级","fx":"北风","high":"高温 22℃","type":"阵雨","low":"低温 12℃","date":"2日星期一"},"aqi":"47","city":"北京"}
 * }
 * 把外层的desc 和 status 提出来，服务端也可使用此格式来返回
 * desc：请求成功或失败，status：状态码，还可以加字段如msg表示提示信息等
 */
public class ResponseData {

    public String desc;
    public int status;
    public String msg;

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "desc='" + desc + '\'' +
                ", status=" + status +
                ", msg='" + msg + '\'' +
                '}';
    }
}
