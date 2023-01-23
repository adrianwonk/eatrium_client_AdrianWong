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
import edu.cis.ibcs_app.Models.CISUser;
import edu.cis.ibcs_app.Models.MenuItem;
import edu.cis.ibcs_app.Models.Order;
import edu.cis.ibcs_app.Models.Request;
import edu.cis.ibcs_app.Models.SimpleClient;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Actions {

    public static void menuItemPopup(MainActivity mainActivity){
        AlertDialog.Builder builder;
        LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(mainActivity.LAYOUT_INFLATER_SERVICE);
        builder = new AlertDialog.Builder(mainActivity);
        View popupView = LayoutInflater.from(mainActivity).inflate(R.layout.menu_item_modify, null);

        EditText name = popupView.findViewById(R.id.modify_name);
        EditText desc = popupView.findViewById(R.id.modify_desc);
        EditText price = popupView.findViewById(R.id.modify_price);
        EditText amount = popupView.findViewById(R.id.modify_amountAvail);
        EditText type = popupView.findViewById(R.id.modify_type);
        Button b = popupView.findViewById(R.id.modify_button);

        builder.setView(popupView);
        final AlertDialog dialog = builder.create();
        setupMenuItemPopUp(b, dialog, name, desc, price, amount, type, mainActivity);
        dialog.show();
    }

    public static void setupMenuItemPopUp(Button b, AlertDialog dialog, EditText name, EditText desc, EditText price, EditText amount, EditText type, MainActivity mainActivity){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request req = new Request(CISConstants.ADD_MENU_ITEM);
                req.addParam(CISConstants.ITEM_NAME_PARAM, String.valueOf(name.getText()));
                req.addParam(CISConstants.DESC_PARAM, String.valueOf(desc.getText()));
                req.addParam(CISConstants.PRICE_PARAM, String.valueOf(price.getText()));
                req.addParam(CISConstants.AMOUNT_AVAIL_PARAM, String.valueOf(amount.getText()));
                req.addParam(CISConstants.ITEM_TYPE_PARAM, String.valueOf(type.getText()));
                try {
                    SimpleClient.makeRequest(CISConstants.HOST, req);
                    mainActivity.menuItemAdapter.update();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                dialog.dismiss();
            }
        });
    }
//  -- POPUP




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
        CISUser currentUser = getUser(userID);


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
        ArrayList<Order> orders = new ArrayList<>();
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
            String[] ordersAndMoney = res[1].split("}"); // <--- money is last in the array

            String[] ordersOnly = new String[ordersAndMoney.length-1];
            System.arraycopy(ordersAndMoney, 0, ordersOnly, 0, ordersOnly.length);

            for (String value : ordersOnly){
                orders.add(decodeOrder(value));
            }

            String moneyStr = ordersAndMoney[ordersAndMoney.length-1];
            moneyStr = moneyStr.split(", money=")[1];

            money = Double.parseDouble(moneyStr);
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
