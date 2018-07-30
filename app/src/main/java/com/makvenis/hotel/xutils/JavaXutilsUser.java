package com.makvenis.hotel.xutils;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

/**
 * 用户数据类
 */

@Table(name = "user")
public class JavaXutilsUser {

    @Column(name = "id",isId = true)
    private int id;

    @Column(name = "key")
    private String key;

    @Column(name = "data")
    private String data;

    public JavaXutilsUser() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JavaXutilsUser{" +
                "id=" + id +
                ", key='" + key + '\'' +
                ", data='" + data + '\'' +
                '}';
    }
}
