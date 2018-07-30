package com.makvenis.hotel.listhotol;

/* 左侧导航栏的实体类 */

public class LeftEntry {
    private String NameEng;/* 栏目的英文名称 */
    private String NameCn; /* 栏目的中文名称 */
    private int num;       /* 点餐的个数 */

    public LeftEntry(String nameEng, String nameCn, int num) {
        NameEng = nameEng;
        NameCn = nameCn;
        this.num = num;
    }

    public LeftEntry() {
    }

    @Override
    public String toString() {
        return "LeftEntry{" +
                "NameEng='" + NameEng + '\'' +
                ", NameCn='" + NameCn + '\'' +
                ", num=" + num +
                '}';
    }

    public String getNameEng() {
        return NameEng;
    }

    public void setNameEng(String nameEng) {
        NameEng = nameEng;
    }

    public String getNameCn() {
        return NameCn;
    }

    public void setNameCn(String nameCn) {
        NameCn = nameCn;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }
}
