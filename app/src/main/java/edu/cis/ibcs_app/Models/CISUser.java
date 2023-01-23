package edu.cis.ibcs_app.Models;

import java.util.ArrayList;

public class CISUser {
    public String userId = "";
    public String name;

    public String yearLevel;
    public ArrayList<Order> orders;
    public double money;

    public CISUser(String userId, String name, String yearLevel, ArrayList<Order> orders, double money) {
        this.userId = userId;
        this.name = name;
        this.yearLevel = yearLevel;
        this.orders = orders;
        this.money = money;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getYearLevel() {
        return yearLevel;
    }

    public void setYearLevel(String yearLevel) {
        this.yearLevel = yearLevel;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    @Override
    public String toString() {

        String result = "CISUser{userID='" + userId + "', ";
        result += "name='" + name + "', ";
        result += "yearLevel='" + yearLevel + "', ";
        result += "orders= ";
        // ADD ORDERS INTO RESULT
        for (Order value : orders){
            result += value + ", ";
        }

        result += "money=" + money + "}";
        return result;
    }

}