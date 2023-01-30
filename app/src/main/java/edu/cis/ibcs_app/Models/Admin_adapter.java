package edu.cis.ibcs_app.Models;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import edu.cis.ibcs_app.Controllers.Actions;
import edu.cis.ibcs_app.Controllers.MainActivity;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class Admin_adapter extends RecyclerView.Adapter<Admin_viewHolder> {
    ArrayList<CISUser> mdata;
    MainActivity mainActivity;

    public boolean userInData(String id){
        for (CISUser value : mdata) {
            if (value.getUserId().equals(id)) return true;
        }
        return false;
    }

    public Admin_adapter(MainActivity ma) {
        mainActivity = ma;
        mdata = new ArrayList<CISUser>();
        update();
    }

    public void update(){ //puffs up the mdata
        Snackbar snackbar = Snackbar.make(mainActivity.findViewById(
            android.R.id.content), "Processing", BaseTransientBottomBar
            .LENGTH_INDEFINITE);
        snackbar.show();
        Admin_getRegisterRequests getRegisterRequests =
            new Admin_getRegisterRequests();
        getRegisterRequests.run();
        notifyDataSetChanged();
        snackbar.dismiss();
    }

    class Admin_getRegisterRequests implements Runnable{
        public Admin_getRegisterRequests() {}
        @Override
        public void run() {
            try{
                Request req = new Request("GET_ACC_REQUESTS");
                String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                Log.d("server", "GET_ACC_REQUESTS: " + result);
                if(!result.equals("")) {
                    String[] resultArr = result.split("'''");
                        // adds data into mdata
                        for (String value : resultArr) {
                            if (value != "") {
                                CISUser u = Actions.decodeUser(value);

                                if (!userInData(u.getUserId())){
                                    mdata.add(u);
                                }
                            }
                        }

                        // remove data from mdata that aren't in resultArr
                        ArrayList<String> userIDS = new ArrayList<>();
                        for (String value : resultArr){
                            userIDS.add(Actions.decodeUser(value)
                                .getUserId()); // gets userIDs in ACC REQ'S
                                               // from the server
                        }

                     // loop through CISusers in mdata and delete those that
                     // aren't in userIDS
                        for (int i = 0; i < mdata.size(); i++) {
                            CISUser value = mdata.get(i);
                            if (!userIDS.contains(value.getUserId())) {
                                mdata.remove(value);
                            }
                        }
                }
                else {
                    mdata.clear();
                }
            }
            catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    @NonNull
    @Override
    public Admin_viewHolder onCreateViewHolder(@NonNull ViewGroup parent
    , int viewType) {
        View inflatedView = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.acc_request_view, parent, false);
        Admin_viewHolder vh = new Admin_viewHolder(inflatedView);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull Admin_viewHolder holder
    , int position) {
        CISUser u = mdata.get(position);
        holder.TVName.setText("name: " + u.name);
        holder.TVId.setText("ID: " + u.userId);
        holder.TVYearLevel.setText("Y"+u.yearLevel);

        holder.bAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.overlay.setVisibility(View.VISIBLE);
                Register_handleReg register_handleReg = new Register_handleReg(
            true, u.userId);
                register_handleReg.run();
                update();
            }
        });

        holder.bDeny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.overlay.setVisibility(View.VISIBLE);
                Register_handleReg register_handleReg = new Register_handleReg(
            false, u.userId);
                register_handleReg.run();
                update();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mdata.size();
    }

    class Register_handleReg implements Runnable{
        String acceptS;
        boolean accept;
        String userID;

        public Register_handleReg(boolean accept_, String userID_) {
            accept = accept_;
            userID = userID_;
            if (accept){
                acceptS = "y";
            }
            else{
                acceptS =  "n";
            }
        }

        @Override
        public void run() {
            try{
                Request req = new Request("HANDLE_REGISTER_REQUEST");
                req.addParam(CISConstants.USER_ID_PARAM, userID);
                req.addParam("ACCEPT", acceptS);
                String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                Log.d("server", "handleRegister: " + result);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
