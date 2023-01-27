package edu.cis.ibcs_app.Controllers;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

import edu.cis.ibcs_app.Models.Admin_adapter;
import edu.cis.ibcs_app.Models.Admin_menuItemAdapter;
import edu.cis.ibcs_app.Models.Admin_menuItemViewHolder;
import edu.cis.ibcs_app.Models.CISUser;
import edu.cis.ibcs_app.Models.Cart_menuItemAdapter;
import edu.cis.ibcs_app.Models.MenuItem;
import edu.cis.ibcs_app.Models.Order;
import edu.cis.ibcs_app.Models.Orders_itemsAdapter;
import edu.cis.ibcs_app.Models.Request;
import edu.cis.ibcs_app.Models.SimpleClient;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Actions {

    public static void menuItemPopup(MainActivity mainActivity){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(mainActivity);
        View popupView = LayoutInflater.from(mainActivity).inflate(R.layout.menu_item_modify, null);

        EditText name = popupView.findViewById(R.id.modify_name);
        EditText desc = popupView.findViewById(R.id.modify_desc);
        EditText price = popupView.findViewById(R.id.modify_price);
        EditText amount = popupView.findViewById(R.id.modify_amountAvail);
        EditText type = popupView.findViewById(R.id.modify_type);
        Button b = popupView.findViewById(R.id.modify_button);
        TextView error = popupView.findViewById(R.id.modify_errorMsg);

        builder.setView(popupView);
        final AlertDialog dialog = builder.create();
        setupMenuItemPopUp(b, dialog, name, desc, price, amount, type, "", error, mainActivity);
        dialog.show();
    }

    public static void setupMenuItemPopUp(Button b, AlertDialog dialog, EditText name, EditText desc, EditText price, EditText amount, EditText type, String id, TextView error, MainActivity mainActivity){
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Request req = new Request(CISConstants.ADD_MENU_ITEM);
                req.addParam(CISConstants.ITEM_NAME_PARAM, String.valueOf(name.getText()));
                req.addParam(CISConstants.DESC_PARAM, String.valueOf(desc.getText()));
                req.addParam(CISConstants.PRICE_PARAM, String.valueOf(price.getText()));
                req.addParam(CISConstants.AMOUNT_AVAIL_PARAM, String.valueOf(amount.getText()));
                req.addParam(CISConstants.ITEM_TYPE_PARAM, String.valueOf(type.getText()));
                req.addParam(CISConstants.ITEM_ID_PARAM, id);
                try {
                    String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                    Log.d("server", "Add menu item: " + result);
                    mainActivity.menuItemAdapter.update();
                    dialog.dismiss();

                } catch (IOException e) {
                    Log.d("error", e.getMessage());
                    if (e.getMessage().equals(CISConstants.PARAM_MISSING_ERR)) {
                        error.setText("please provide valid inputs");
                        error.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
    }
//  -- POPUP

    public static void cartPopup(MainActivity mainActivity , Orders_itemsAdapter ordersAdapter){

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(mainActivity);
        View popupView = LayoutInflater.from(mainActivity).inflate(R.layout.cart, null);

        TextView myBalance = popupView.findViewById(R.id.cart_money);
        TextView cartTotal = popupView.findViewById(R.id.cart_total);

        Button checkout = popupView.findViewById(R.id.cart_checkout);
        Button exit = popupView.findViewById(R.id.cart_exit);

        Cart_menuItemAdapter adapter = new Cart_menuItemAdapter(mainActivity, cartTotal, checkout);
        RecyclerView recyclerView = popupView.findViewById(R.id.cart_items);
        LinearLayoutManager llm = new LinearLayoutManager(popupView.getContext());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(adapter);

        myBalance.setText("My Balance: " + mainActivity.thisUser.getMoney());

        Request req = new Request("GET_CART_TOTAL");
        req.addParam(CISConstants.USER_ID_PARAM, mainActivity.thisUser.userId);

        Double total;

        try {
            String result = SimpleClient.makeRequest(CISConstants.HOST, req);
            total = Double.parseDouble(result);
            cartTotal.setText("Cart Total: " + result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (total > mainActivity.thisUser.getMoney() || total == 0){
            checkout.setVisibility(View.INVISIBLE);
        }

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request req = new Request("CHECKOUT_CART");
                req.addParam(CISConstants.USER_ID_PARAM, mainActivity.thisUser.getUserId());
                try{
                    String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                    Log.d("server", "CHECKOUT_CART: " + result);
                    adapter.update();
                    myBalance.setText("My Balance: " + mainActivity.thisUser.getMoney());
                    cartTotal.setText("Cart Total: 0.0");
                    checkout.setVisibility(View.INVISIBLE);
                } catch (IOException e) {
                    Log.d("server", e.getMessage());
                }
            }
        });



        builder.setView(popupView);
        final AlertDialog dialog = builder.create();

        exit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ordersAdapter.update();
                dialog.dismiss();
            }
        });

        dialog.show();

    }


    public static void loadAdmin(String userID, MainActivity mainActivity){
        if (getUserType(userID) != 'A'){
            Snackbar.make(mainActivity.findViewById(R.id.container), "Please provide valid admin ID", BaseTransientBottomBar.LENGTH_SHORT).show();
        }
        else {
            mainActivity.setContentView(R.layout.admin_page);
            Button b = mainActivity.findViewById(R.id.admin_logout);
            Button addMenuItem = mainActivity.findViewById(R.id.admin_addItem);

            b.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    logout(mainActivity);
                }
            });

            addMenuItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    menuItemPopup(mainActivity);
                }
            });

            Admin_adapter adapter = new Admin_adapter(mainActivity);
            RecyclerView recyclerView = mainActivity.findViewById(R.id.admin_creationRequests);
            recyclerView.setAdapter(adapter);
            recyclerView.setLayoutManager(new LinearLayoutManager(mainActivity));

            mainActivity.menuItemAdapter = new Admin_menuItemAdapter(mainActivity);
            RecyclerView recyclerView1 = mainActivity.findViewById(R.id.admin_menuItemsManager);
            recyclerView1.setAdapter(mainActivity.menuItemAdapter);
            recyclerView1.setLayoutManager(new LinearLayoutManager(mainActivity));


        }
    }

    public static void loadUser(String userID, MainActivity mainActivity){
        mainActivity.thisUser = getUser(userID);

        mainActivity.setContentView(R.layout.order_page);
        Button cart = mainActivity.findViewById(R.id.order_cart);
        TextView helloMsg = mainActivity.findViewById(R.id.order_helloMsg);
        Button logoutB = mainActivity.findViewById(R.id.order_logout);

        helloMsg.setText("Hello,\n" + mainActivity.thisUser.getName());

        Orders_itemsAdapter adapter = new Orders_itemsAdapter(mainActivity);
        RecyclerView recView = mainActivity.findViewById(R.id.order_items);
        recView.setAdapter(adapter);
        recView.setLayoutManager(new LinearLayoutManager(mainActivity));

        logoutB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout(mainActivity);
            }
        });
        cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cartPopup(mainActivity, adapter);
            }
        });

    }

    public static void logout(MainActivity mainActivity){
        mainActivity.setContentView(R.layout.activity_main);
        mainActivity.recreate();
    }
    public static char getUserType(String uID){
        try{
            Request req = new Request("GET_USER_TYPE");
            req.addParam(CISConstants.USER_ID_PARAM, uID);
            String result = SimpleClient.makeRequest(CISConstants.HOST, req);
            Log.d("server", "GET_USER_TYPE: " + result);
            return result.charAt(0);
        } catch (IOException e) {
            e.printStackTrace();
            return 'N';
        }
    }

    public static CISUser getUser(String uID){
        try{
            Request req = new Request(CISConstants.GET_USER);
            req.addParam(CISConstants.USER_ID_PARAM, uID);
            String result = SimpleClient.makeRequest(CISConstants.HOST, req);
            Log.d("server", "GET_USER: " + result);

            return decodeUser(result);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static CISUser decodeUser(String result){
        String userId = "";
        String name;
        String yearLevel;
        ArrayList<Order> orders = new ArrayList<Order>();
        double money;

        String[] res = result.split("orders= " );

        String[] userIdNameYearLevel = res[0].split("'");
        userId = userIdNameYearLevel[1];
        name = userIdNameYearLevel[3];
        yearLevel = userIdNameYearLevel[5];

        if (res[1].charAt(0) == ','){
            String s = res[1].split("=")[1];
            money = Double.parseDouble(s.substring(0, s.length()-1));
        }
        else {
            String[] ordersAndMoney = res[1].split("Order"); // <--- money is last in the array

            String[] lastOrderAndMoney = ordersAndMoney[ordersAndMoney.length-1].split(", money="); // last order on 0, money} on 1

            String[] ordersOnly = new String[ordersAndMoney.length-2]; // rest of the orders (can use decodeorder)
            System.arraycopy(ordersAndMoney, 1, ordersOnly, 0, ordersOnly.length);

            for (String value : ordersOnly){
                orders.add(decodeOrder(value));
            }

            orders.add(decodeOrder(lastOrderAndMoney[0]));
            money = Double.parseDouble(lastOrderAndMoney[1].substring(0, lastOrderAndMoney[1].length()-1));
        }


        return new CISUser(userId, name, yearLevel, orders, money);
    }

    public static Order decodeOrder(String orderString){
        String[] result = orderString.split("'");
        String itemID = result[1];
        String type = result[3];
        String orderID = result[5];

        try {
            return new Order(itemID, type, orderID);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static MenuItem decodeMenuItem(String s){
        String[] elements = s.split(", ");
        for (int i = 0; i < elements.length; i++){
            String element = elements[i].split("=")[1];
            String elementCleaned = "";
            for (var value : element.toCharArray()){
                if (!(value == '\'' || value == '}' || value == '{')){
                    elementCleaned += value;
                }
            }
            elements[i] = elementCleaned;
        }

        String name = elements[0];
        String desc = elements[1];
        double price = Double.parseDouble(elements[2]);
        String id = elements[3];
        int amountAvail = Integer.parseInt(elements[4]);
        String type = elements[5];

        return new MenuItem(name, desc, price, id, amountAvail, type);
    }



}
