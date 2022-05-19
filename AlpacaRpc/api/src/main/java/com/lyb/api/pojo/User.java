package com.lyb.api.pojo;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Mr.Alpaca
 * @version 1.0.0
 */
public class User implements Serializable {
    private Integer id;
    private String name;
    private Date date1;
    private Date date2;

    public User() {
    }

    public User(Integer id, String name, Date date1, Date date2) {
        this.id = id;
        this.name = name;
        this.date1 = date1;
        this.date2 = date2;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate1() {
        return date1;
    }

    public void setDate1(Date date1) {
        this.date1 = date1;
    }

    public Date getDate2() {
        return date2;
    }

    public void setDate2(Date date2) {
        this.date2 = date2;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", date1=" + date1 +
                ", date2=" + date2 +
                '}';
    }
}
