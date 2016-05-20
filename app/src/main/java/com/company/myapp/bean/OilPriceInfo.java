package com.company.myapp.bean;

/**
 * 油价信息
 * Created by GHY on 2016/2/17.
 */
public class OilPriceInfo {

    /**
     * id : 0
     * ct : 2016-02-25 04:10:14.494
     * p0 : 5.17
     * p90 : 5.20
     * p93 : 5.56
     * p97 : 5.92
     * prov : 北京
     * insert_time : null
     */

    private int id;
    private String ct;
    private String p0;
    private String p90;
    private String p93;
    private String p97;
    private String prov;
    private Object insert_time;

    public void setId(int id) {
        this.id = id;
    }

    public void setCt(String ct) {
        this.ct = ct;
    }

    public void setP0(String p0) {
        this.p0 = p0;
    }

    public void setP90(String p90) {
        this.p90 = p90;
    }

    public void setP93(String p93) {
        this.p93 = p93;
    }

    public void setP97(String p97) {
        this.p97 = p97;
    }

    public void setProv(String prov) {
        this.prov = prov;
    }

    public void setInsert_time(Object insert_time) {
        this.insert_time = insert_time;
    }

    public int getId() {
        return id;
    }

    public String getCt() {
        return ct;
    }

    public String getP0() {
        return p0;
    }

    public String getP90() {
        return p90;
    }

    public String getP93() {
        return p93;
    }

    public String getP97() {
        return p97;
    }

    public String getProv() {
        return prov;
    }

    public Object getInsert_time() {
        return insert_time;
    }
}
