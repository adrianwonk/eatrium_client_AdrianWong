package edu.cis.ibcs_app.Models;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.LayoutInflaterCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;

import edu.cis.ibcs_app.Controllers.Actions;
import edu.cis.ibcs_app.Controllers.MainActivity;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Admin_menuItemAdapter extends RecyclerView.Adapter<Admin_menuItemViewHolder> {

    ArrayList<MenuItem> mdata;
    MainActivity mainActivity;

    public Admin_menuItemAdapter(MainActivity ma) {
        mainActivity = ma;
        mdata = new ArrayList<MenuItem>();
        update();
    }

    public void update(){ //puffs up the mdata

        Snackbar snackbar = Snackbar.make(mainActivity.findViewById(android.R.id.content), "Processing", BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.show();
        Admin_getMenuitems getMenuitems = new Admin_getMenuitems();
        getMenuitems.run();
        notifyDataSetChanged();
        snackbar.dismiss();
    }

    public class Admin_getMenuitems implements Runnable{

        @Override
        public void run() {
            mdata.clear();
            Request req = new Request("GET_MENU_ITEMS");
            String result = null;

            try {
                result = SimpleClient.makeRequest(CISConstants.HOST, req);
            } catch (IOException e) {
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
    public Admin_menuItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext()).inflate(R.layout.menu_item_row, parent, false);
        Admin_menuItemViewHolder vh = new Admin_menuItemViewHolder(inflatedView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Admin_menuItemViewHolder holder, int position) {

        MenuItem item = mdata.get(position);
        holder.amountAvail.setText("" + item.amountAvailable + " left");
        holder.desc.setText(item.description);
        holder.name.setText(item.name);
        holder.price.setText("Price: $" + item.price);
        holder.button.setText("EDIT");

        String id = item.id;

//        TODO
        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//              POPUP --
                AlertDialog.Builder builder;
                LayoutInflater inflater = (LayoutInflater) mainActivity.getSystemService(mainActivity.LAYOUT_INFLATER_SERVICE);
                builder = new AlertDialog.Builder(mainActivity);
                View popupView = LayoutInflater.from(mainActivity).inflate(R.layout.menu_item_modify, null);

                EditText name = popupView.findViewById(R.id.modify_name);
                EditText desc = popupView.findViewById(R.id.modify_desc);
                EditText price = popupView.findViewById(R.id.modify_price);
                EditText amount = popupView.findViewById(R.id.modify_amountAvail);
                EditText type = popupView.findViewById(R.id.modify_type);
                TextView id = popupView.findViewById(R.id.modify_idview);
                Button b = popupView.findViewById(R.id.modify_button);

//              OPTIONAL --
                name.setText(item.name);
                desc.setText(item.description);
                price.setText("" + item.price);
                amount.setText("" + item.amountAvailable);
                type.setText(item.type);
                id.setText("ID: " + item.id);
//              -- OPTIONAL

                builder.setView(popupView);
                final AlertDialog dialog = builder.create();
                setupMenuItemPopUp(b, dialog, name, desc, price, amount, type, item.id);
                dialog.show();
//              -- POPUP
            }
        });
    }

//  POPUP --
    public void setupMenuItemPopUp(Button b, AlertDialog dialog, EditText name, EditText desc, EditText price, EditText amount, EditText type, String id ){
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
                    SimpleClient.makeRequest(CISConstants.HOST, req);
                    update();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                dialog.dismiss();
            }
        });
    }
//  -- POPUP
    @Override
    public int getItemCount() {
        return mdata.size();
    }
}
