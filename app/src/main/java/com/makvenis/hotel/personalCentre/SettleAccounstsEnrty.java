package com.makvenis.hotel.personalCentre;

/* 订单实体类 */
public class SettleAccounstsEnrty {

    public String photoUrl; //表b中的图片地址
    public String asale; //价格（a）
    public String sName; //名称（a）
    public String anum;  //数量（a）
    public String aid;   //id（a）

    public SettleAccounstsEnrty(String photoUrl, String asale, String sName, String anum, String aid) {
        this.photoUrl = photoUrl;
        this.asale = asale;
        this.sName = sName;
        this.anum = anum;
        this.aid = aid;
    }

    public SettleAccounstsEnrty() {
    }

    @Override
    public String toString() {
        return "SettleAccounstsEnrty{" +
                "photoUrl='" + photoUrl + '\'' +
                ", asale='" + asale + '\'' +
                ", sName='" + sName + '\'' +
                ", anum='" + anum + '\'' +
                ", aid='" + aid + '\'' +
                '}';
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAsale() {
        return asale;
    }

    public void setAsale(String asale) {
        this.asale = asale;
    }

    public String getsName() {
        return sName;
    }

    public void setsName(String sName) {
        this.sName = sName;
    }

    public String getAnum() {
        return anum;
    }

    public void setAnum(String anum) {
        this.anum = anum;
    }

    public String getAid() {
        return aid;
    }

    public void setAid(String aid) {
        this.aid = aid;
    }
}
