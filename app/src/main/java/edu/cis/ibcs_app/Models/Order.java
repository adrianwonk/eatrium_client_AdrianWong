package edu.cis.ibcs_app.Models;
import edu.cis.ibcs_app.Utils.*;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class Order {
    private String itemID = "";
    private String type = "";
    private String orderID = "";

    public Order(String itemID, String type, String orderID) {
        this.itemID = itemID;
        this.type = type;
        this.orderID = orderID;
    }

    public String getItemID() {
        return itemID;
    }

    public void setItemID(String itemID) {
        this.itemID = itemID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    @Override
    public String toString() {
        String result = "Order{";
        result += "itemID='" + this.getItemID() + "', ";
        result += "type='" + this.getType() + "', ";
        result += "orderID='" + this.getOrderID() + "'}";
        return result;
    }
}
