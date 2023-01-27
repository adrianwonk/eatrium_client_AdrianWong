package edu.cis.ibcs_app.Models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;

import edu.cis.ibcs_app.Controllers.Actions;
import edu.cis.ibcs_app.Controllers.MainActivity;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Cart_menuItemAdapter extends RecyclerView.Adapter<Admin_menuItemViewHolder> {

    ArrayList<Order> mdata;
    MainActivity mainActivity;

    TextView cartTotal;

    Button checkoutB;

    public Cart_menuItemAdapter(MainActivity ma, TextView cartTotal, Button checkoutB) {
        this.cartTotal = cartTotal;
        this.checkoutB = checkoutB;
        mainActivity = ma;
        mdata = new ArrayList<>();
        update();
    }

    public void update(){ //puffs up the mdata
        mdata.clear();
        Request req = new Request(CISConstants.GET_USER);
        req.addParam(CISConstants.USER_ID_PARAM, mainActivity.thisUser.getUserId());
        String result = null;

        try {
            result = SimpleClient.makeRequest(CISConstants.HOST, req);
            mainActivity.thisUser = Actions.decodeUser(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.d("server", "THIS_USER: " + result);

        for (Order o : mainActivity.thisUser.getOrders()){
            mdata.add(o);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public Admin_menuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(mainActivity).inflate(R.layout.menu_item_row, parent, false);
        Admin_menuItemViewHolder vh = new Admin_menuItemViewHolder(inflatedView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Admin_menuItemViewHolder holder, int position) {
        Order o = mdata.get(position);
        final int pos = position;
        Request req = new Request(CISConstants.GET_ITEM);
        req.addParam(CISConstants.ITEM_ID_PARAM, o.getItemID());

        MenuItem item = null;

        try{
            String result = SimpleClient.makeRequest(CISConstants.HOST, req);
            Log.d("server", "GET_ITEM: " + result);
            if (result.charAt(0) == 'M') {
                item = Actions.decodeMenuItem(result);
            }
            else {
                Log.d("server", "ERROR: " + result);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (item != null){
            holder.name.setText(item.name);
            holder.desc.setText(item.description);
            holder.amountAvail.setText("" + item.amountAvailable + " left");
            holder.price.setText("Price: $" + item.price);
            holder.button.setText("REMOVE");

            holder.button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Request req = new Request("REMOVE_FROM_CART");
                    req.addParam(CISConstants.USER_ID_PARAM, mainActivity.thisUser.userId);
                    req.addParam(CISConstants.ORDER_ID_PARAM, o.getOrderID());

                    try{
                        String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                        Log.d("server", "REMOVE_FROM_CART: " + result);
                        if (result.equals(CISConstants.SUCCESS)){
                            mdata.remove(pos);
                            notifyDataSetChanged();

                            // to update cart UI variables --
                            Request req2 = new Request("GET_CART_TOTAL");
                            req2.addParam(CISConstants.USER_ID_PARAM, mainActivity.thisUser.userId);

                            Double total;

                            try {
                                String result2 = SimpleClient.makeRequest(CISConstants.HOST, req2);
                                total = Double.parseDouble(result2);
                                cartTotal.setText("Cart Total: " + result2);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            if (total <= mainActivity.thisUser.getMoney() && total != 0){
                                checkoutB.setVisibility(View.VISIBLE);
                            }
                            else {
                                checkoutB.setVisibility(View.INVISIBLE);
                            }
                            // --
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }
}
