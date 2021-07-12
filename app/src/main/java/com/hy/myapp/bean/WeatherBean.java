package com.hy.myapp.bean;

import java.util.List;

/**
 * bean示例
 * Created by GHY on 2016/5/3.
 */
public class WeatherBean {


    /**
     * wendu : 26
     * ganmao : 昼夜温差大，风力较强，易发生感冒，请注意适当增减衣服，加强自我防护避免感冒。
     * forecast : [{"fengxiang":"北风","fengli":"4-5级","high":"高温 27℃","type":"晴","low":"低温 10℃","date":"3日星期二"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 28℃","type":"阴","low":"低温 15℃","date":"4日星期三"},{"fengxiang":"北风","fengli":"3-4级","high":"高温 27℃","type":"多云","low":"低温 16℃","date":"5日星期四"},{"fengxiang":"北风","fengli":"4-5级","high":"高温 25℃","type":"晴","low":"低温 12℃","date":"6日星期五"},{"fengxiang":"南风","fengli":"3-4级","high":"高温 25℃","type":"晴","low":"低温 14℃","date":"7日星期六"}]
     * yesterday : {"fl":"4-5级","fx":"北风","high":"高温 22℃","type":"阵雨","low":"低温 12℃","date":"2日星期一"}
     * aqi : 72
     * city : 北京
     */

    private String wendu;
    private String ganmao;
    /**
     * fl : 4-5级
     * fx : 北风
     * high : 高温 22℃
     * type : 阵雨
     * low : 低温 12℃
     * date : 2日星期一
     */

    private YesterdayEntity yesterday;
    private String aqi;
    private String city;
    /**
     * fengxiang : 北风
     * fengli : 4-5级
     * high : 高温 27℃
     * type : 晴
     * low : 低温 10℃
     * date : 3日星期二
     */

    private List<ForecastEntity> forecast;

    public String getWendu() {
        return wendu;
    }

    public void setWendu(String wendu) {
        this.wendu = wendu;
    }

    public String getGanmao() {
        return ganmao;
    }

    public void setGanmao(String ganmao) {
        this.ganmao = ganmao;
    }

    public YesterdayEntity getYesterday() {
        return yesterday;
    }

    public void setYesterday(YesterdayEntity yesterday) {
        this.yesterday = yesterday;
    }

    public String getAqi() {
        return aqi;
    }

    public void setAqi(String aqi) {
        this.aqi = aqi;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public List<ForecastEntity> getForecast() {
        return forecast;
    }

    public void setForecast(List<ForecastEntity> forecast) {
        this.forecast = forecast;
    }

    public static class YesterdayEntity {
        private String fl;
        private String fx;
        private String high;
        private String type;
        private String low;
        private String date;

        public String getFl() {
            return fl;
        }

        public void setFl(String fl) {
            this.fl = fl;
        }

        public String getFx() {
            return fx;
        }

        public void setFx(String fx) {
            this.fx = fx;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    public static class ForecastEntity {
        private String fengxiang;
        private String fengli;
        private String high;
        private String type;
        private String low;
        private String date;

        public String getFengxiang() {
            return fengxiang;
        }

        public void setFengxiang(String fengxiang) {
            this.fengxiang = fengxiang;
        }

        public String getFengli() {
            return fengli;
        }

        public void setFengli(String fengli) {
            this.fengli = fengli;
        }

        public String getHigh() {
            return high;
        }

        public void setHigh(String high) {
            this.high = high;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLow() {
            return low;
        }

        public void setLow(String low) {
            this.low = low;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }
}
