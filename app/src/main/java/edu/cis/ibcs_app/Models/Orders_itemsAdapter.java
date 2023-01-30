package edu.cis.ibcs_app.Models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

import edu.cis.ibcs_app.Controllers.Actions;
import edu.cis.ibcs_app.Controllers.MainActivity;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Orders_itemsAdapter extends
RecyclerView.Adapter<Admin_menuItemViewHolder> {

    ArrayList<MenuItem> mdata;
    MainActivity mainActivity;
    CISUser currentUser;

    public Orders_itemsAdapter(MainActivity ma) {
        mainActivity = ma;
        currentUser = ma.thisUser;
        mdata = new ArrayList<MenuItem>();
        update();
    }

    public void update() { //puffs up the mdata
        Orders_itemsAdapter.Orders_getMenuItems getMenuitems =
            new Orders_getMenuItems();

        getMenuitems.run();
        notifyDataSetChanged();
    }

    public class Orders_getMenuItems implements Runnable {
        @Override
        public void run() {
            mdata.clear();
            Request req = new Request("GET_MENU_ITEMS");
            String result = null;

            try {
                result = SimpleClient.makeRequest(CISConstants.HOST, req);
            }
            catch (IOException e) {
                throw new RuntimeException(e);
            }

            Log.d("server", "GET_ACC_REQUESTS: " + result);

            if(!result.equals("")) {
                String[] resultArr = result.split("'''");
                // adds data into mdata
                for (String value : resultArr) {
                    if (value != "") {
                        MenuItem item = Actions.decodeMenuItem(value);
                        mdata.add(item);
                    }
                }
            }
        }
    }

    @NonNull
    @Override
    public Admin_menuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent
    , int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate
            (R.layout.menu_item_row, parent, false);

        Admin_menuItemViewHolder vh =
            new Admin_menuItemViewHolder(inflatedView);

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Admin_menuItemViewHolder holder,
    int position) {
        MenuItem item = mdata.get(position);
        holder.amountAvail.setText("" + item.amountAvailable + " left");
        holder.desc.setText(item.description);
        holder.name.setText(item.name);
        holder.price.setText("Price: $" + item.price);
        holder.button.setText("ADD TO CART");

//        TODO
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Request req = new Request("ADD_TO_CART");
                req.addParam(CISConstants.ITEM_ID_PARAM, item.id);
                req.addParam(CISConstants.USER_ID_PARAM, currentUser.userId);
                String result = null;
                try {
                    result = SimpleClient.makeRequest(CISConstants.HOST, req);
                    update();
                }
                catch (IOException e) {
                    Snackbar snackbar = Snackbar.make(mainActivity.findViewById
                        (android.R.id.content), "out of stock!",
                        BaseTransientBottomBar.LENGTH_SHORT);
                    snackbar.show();
                }
                Log.d("server", "ADD TO CART: " + result);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }
}
