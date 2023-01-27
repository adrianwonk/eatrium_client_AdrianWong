package edu.cis.ibcs_app.Models;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import edu.cis.ibcs_app.R;

public class Admin_menuItemViewHolder extends RecyclerView.ViewHolder {

    public TextView name;
    public TextView desc;
    public TextView price;
    public TextView amountAvail;
    public Button button;
    public View view;
    public Admin_menuItemViewHolder(@NonNull View itemView) {
        super(itemView);
        name = itemView.findViewById(R.id.itemrow_name);
        desc = itemView.findViewById(R.id.itemrow_desc);
        price = itemView.findViewById(R.id.itemrow_price);
        amountAvail = itemView.findViewById(R.id.itemrow_amountAvail);
        button = itemView.findViewById(R.id.itemrow_edit);
        view = itemView;
    }
}
