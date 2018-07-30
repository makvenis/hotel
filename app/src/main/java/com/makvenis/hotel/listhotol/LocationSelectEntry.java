package com.makvenis.hotel.listhotol;

/**
 * 餐桌实体类
 */

public class LocationSelectEntry {

    public int pathNumber;
    public String pathName;
    public int zhuangtai;
    public int peopleNumber;

    public LocationSelectEntry(int pathNumber, String pathName, int zhuangtai, int peopleNumber) {
        this.pathNumber = pathNumber;
        this.pathName = pathName;
        this.zhuangtai = zhuangtai;
        this.peopleNumber = peopleNumber;
    }

    public LocationSelectEntry(){}

    @Override
    public String toString() {
        return "LocationSelectEntry{" +
                "pathNumber=" + pathNumber +
                ", pathName='" + pathName + '\'' +
                ", zhuangtai=" + zhuangtai +
                ", peopleNumber=" + peopleNumber +
                '}';
    }

    public int getPathNumber() {
        return pathNumber;
    }

    public void setPathNumber(int pathNumber) {
        this.pathNumber = pathNumber;
    }

    public String getPathName() {
        return pathName;
    }

    public void setPathName(String pathName) {
        this.pathName = pathName;
    }

    public int getZhuangtai() {
        return zhuangtai;
    }

    public void setZhuangtai(int zhuangtai) {
        this.zhuangtai = zhuangtai;
    }

    public int getPeopleNumber() {
        return peopleNumber;
    }

    public void setPeopleNumber(int peopleNumber) {
        this.peopleNumber = peopleNumber;
    }
}
