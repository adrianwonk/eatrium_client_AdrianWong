package edu.cis.ibcs_app.Models;
import edu.cis.ibcs_app.Utils.*;

import java.util.ArrayList;

public class MenuItem {
    public String name;
    public String description;
    public double price;
    public String id;
    public int amountAvailable;
    public String type;


    public MenuItem(String name, String description, double price, String id, int amountAvailable, String type) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.id = id;
        this.amountAvailable = amountAvailable;
        this.type = type;
    }
}
