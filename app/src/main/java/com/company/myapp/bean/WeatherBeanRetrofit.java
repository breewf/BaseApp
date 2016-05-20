package com.company.myapp.bean;

import java.util.List;

/**
 * Created by Administrator on 2016/3/10.
 */
public class WeatherBeanRetrofit {


    /**
     * desc : OK
     * status : 1000
     * data : {"wendu":"6","ganmao":"天凉，昼夜温差较大，较易发生感冒，请适当增减衣服，体质较弱的朋友请注意适当防护。","forecast":[{"fengxiang":"北风","fengli":"3-4级","high":"高温 7℃","type":"晴","low":"低温 -4℃","date":"10日星期四"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 8℃","type":"晴","low":"低温 -2℃","date":"11日星期五"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 9℃","type":"多云","low":"低温 1℃","date":"12日星期六"},{"fengxiang":"北风","fengli":"3-4级","high":"高温 12℃","type":"晴","low":"低温 1℃","date":"13日星期天"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 15℃","type":"晴","low":"低温 4℃","date":"14日星期一"}],"yesterday":{"fl":"3-4级","fx":"北风","high":"高温 6℃","type":"多云","low":"低温 -4℃","date":"9日星期三"},"aqi":"44","city":"北京"}
     */

    private String desc;
    private int status;
    /**
     * wendu : 6
     * ganmao : 天凉，昼夜温差较大，较易发生感冒，请适当增减衣服，体质较弱的朋友请注意适当防护。
     * forecast : [{"fengxiang":"北风","fengli":"3-4级","high":"高温 7℃","type":"晴","low":"低温 -4℃","date":"10日星期四"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 8℃","type":"晴","low":"低温 -2℃","date":"11日星期五"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 9℃","type":"多云","low":"低温 1℃","date":"12日星期六"},{"fengxiang":"北风","fengli":"3-4级","high":"高温 12℃","type":"晴","low":"低温 1℃","date":"13日星期天"},{"fengxiang":"无持续风向","fengli":"微风级","high":"高温 15℃","type":"晴","low":"低温 4℃","date":"14日星期一"}]
     * yesterday : {"fl":"3-4级","fx":"北风","high":"高温 6℃","type":"多云","low":"低温 -4℃","date":"9日星期三"}
     * aqi : 44
     * city : 北京
     */

    private DataEntity data;

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setData(DataEntity data) {
        this.data = data;
    }

    public String getDesc() {
        return desc;
    }

    public int getStatus() {
        return status;
    }

    public DataEntity getData() {
        return data;
    }

    public static class DataEntity {
        private String wendu;
        private String ganmao;
        /**
         * fl : 3-4级
         * fx : 北风
         * high : 高温 6℃
         * type : 多云
         * low : 低温 -4℃
         * date : 9日星期三
         */

        private YesterdayEntity yesterday;
        private String aqi;
        private String city;
        /**
         * fengxiang : 北风
         * fengli : 3-4级
         * high : 高温 7℃
         * type : 晴
         * low : 低温 -4℃
         * date : 10日星期四
         */

        private List<ForecastEntity> forecast;

        public void setWendu(String wendu) {
            this.wendu = wendu;
        }

        public void setGanmao(String ganmao) {
            this.ganmao = ganmao;
        }

        public void setYesterday(YesterdayEntity yesterday) {
            this.yesterday = yesterday;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public void setForecast(List<ForecastEntity> forecast) {
            this.forecast = forecast;
        }

        public String getWendu() {
            return wendu;
        }

        public String getGanmao() {
            return ganmao;
        }

        public YesterdayEntity getYesterday() {
            return yesterday;
        }

        public String getAqi() {
            return aqi;
        }

        public String getCity() {
            return city;
        }

        public List<ForecastEntity> getForecast() {
            return forecast;
        }

        public static class YesterdayEntity {
            private String fl;
            private String fx;
            private String high;
            private String type;
            private String low;
            private String date;

            public void setFl(String fl) {
                this.fl = fl;
            }

            public void setFx(String fx) {
                this.fx = fx;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getFl() {
                return fl;
            }

            public String getFx() {
                return fx;
            }

            public String getHigh() {
                return high;
            }

            public String getType() {
                return type;
            }

            public String getLow() {
                return low;
            }

            public String getDate() {
                return date;
            }
        }

        public static class ForecastEntity {
            private String fengxiang;
            private String fengli;
            private String high;
            private String type;
            private String low;
            private String date;

            public void setFengxiang(String fengxiang) {
                this.fengxiang = fengxiang;
            }

            public void setFengli(String fengli) {
                this.fengli = fengli;
            }

            public void setHigh(String high) {
                this.high = high;
            }

            public void setType(String type) {
                this.type = type;
            }

            public void setLow(String low) {
                this.low = low;
            }

            public void setDate(String date) {
                this.date = date;
            }

            public String getFengxiang() {
                return fengxiang;
            }

            public String getFengli() {
                return fengli;
            }

            public String getHigh() {
                return high;
            }

            public String getType() {
                return type;
            }

            public String getLow() {
                return low;
            }

            public String getDate() {
                return date;
            }
        }
    }
}
