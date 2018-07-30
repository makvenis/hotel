package com.makvenis.hotel.xutils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 创建用户购买商品的情况表，用于存储购买基本信息
 * 字段：cid className type num sale pathNum zhuangtai
 * 解释 主键 商品名称 商品类型（大、小份） 数量 总价 当前座位编号
 */

@Table(name = "listCoffee")
public class JavaXutilsCoffee {

    @Column(name = "id",isId = true)
    private int id; //主键

    @Column(name = "cid")
    private int cid; //当前页面来自于cid的编号

    @Column(name = "fid")
    private int fid; //当前页面来自于cid的编号

    @Column(name = "className")
    private String className; //类名称

    @Column(name = "type")
    private String type; //类型 ---（大份max,小份min）

    @Column(name = "num")
    private int num; //数量

    @Column(name = "sale")
    private float sale; //总价

    @Column(name = "pathNum")
    private int pathNum; //座位编号

    @Column(name = "zhuangtai")
    private int zhuangtai; //(假如未结账则为1 结账0)

    public JavaXutilsCoffee() {
    }

    public JavaXutilsCoffee(int id, int cid, int fid, String className, String type, int num, float sale, int pathNum, int zhuangtai) {
        this.id = id;
        this.cid = cid;
        this.fid = fid;
        this.className = className;
        this.type = type;
        this.num = num;
        this.sale = sale;
        this.pathNum = pathNum;
        this.zhuangtai = zhuangtai;
    }

    @Override
    public String toString() {
        return "JavaXutilsCoffee{" +
                "id=" + id +
                ", cid=" + cid +
                ", fid=" + fid +
                ", className='" + className + '\'' +
                ", type='" + type + '\'' +
                ", num=" + num +
                ", sale=" + sale +
                ", pathNum=" + pathNum +
                ", zhuangtai=" + zhuangtai +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getCid() {
        return cid;
    }

    public void setCid(int cid) {
        this.cid = cid;
    }

    public int getFid() {
        return fid;
    }

    public void setFid(int fid) {
        this.fid = fid;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public float getSale() {
        return sale;
    }

    public void setSale(float sale) {
        this.sale = sale;
    }

    public int getPathNum() {
        return pathNum;
    }

    public void setPathNum(int pathNum) {
        this.pathNum = pathNum;
    }

    public int getZhuangtai() {
        return zhuangtai;
    }

    public void setZhuangtai(int zhuangtai) {
        this.zhuangtai = zhuangtai;
    }
}
