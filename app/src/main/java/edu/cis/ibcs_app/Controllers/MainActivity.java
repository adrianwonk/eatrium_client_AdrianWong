package edu.cis.ibcs_app.Controllers;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import edu.cis.ibcs_app.Models.Admin_menuItemAdapter;
import edu.cis.ibcs_app.Models.CISUser;
import edu.cis.ibcs_app.Models.Request;
import edu.cis.ibcs_app.Models.SimpleClient;
import edu.cis.ibcs_app.R;
import edu.cis.ibcs_app.Utils.CISConstants;

public class MainActivity extends AppCompatActivity {

    //important elements
    EditText userIdInput;

    CISUser thisUser;

    public Admin_menuItemAdapter menuItemAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        //This is not great, for extra credit you can fix this so that network calls happen on a different thread
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        userIdInput = (EditText) findViewById(R.id.user_id_input);

    }

    public void ping(View v) {
        try{
            Request req = new Request(CISConstants.PING);
            String message = SimpleClient.makeRequest(CISConstants.HOST, req);
            Log.d("server", "ping: "  + message);
        }

        catch (Exception e){
            e.printStackTrace();
            System.out.println("Can't ping!");
        }
    }

    public void logIn(View view) {
        try {
            String userID = String.valueOf(userIdInput.getText());
            Log.d("server", "logIn: "+ userID);

            if (userID.isEmpty() || userID.isBlank()) Snackbar.make(findViewById(R.id.container), "Please provide a valid input", BaseTransientBottomBar.LENGTH_SHORT).show();

            else {
                Log.d("server", ""+Actions.getUserType(userID));
                switch (Actions.getUserType(userID)){
                    case 'A':
                        Actions.loadAdmin(userID, this);
                        break;
                    case 'U':
                        Actions.loadUser(userID, this);
                        break;
                    case 'N':
                        Snackbar.make(findViewById(R.id.container), "User not found", BaseTransientBottomBar.LENGTH_SHORT).show();
                        break;
                    case 'R':
                        Snackbar.make(findViewById(R.id.container), "Wait until your account has been approved!", BaseTransientBottomBar.LENGTH_SHORT).show();
                }
            }

        }
        catch(Exception e) {
            e.printStackTrace();
        }

    }

    AlertDialog dialog;
    EditText registerNameInput;
    EditText registerIdInput;
    EditText registerYrLevelInput;
    Button registerAcc;
    Button registerClose;
    TextView registerUsrMsg;
    Register_waitForThreads register_waitForThreads;

    public void createAcc(View view){
        AlertDialog.Builder builder;

        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        builder = new AlertDialog.Builder((MainActivity) this);
        View popupView = inflater.inflate(R.layout.popup, null);

        registerNameInput = (EditText) popupView.findViewById(R.id.register_name);
        registerIdInput = (EditText) popupView.findViewById(R.id.register_id);
        registerYrLevelInput = (EditText) popupView.findViewById(R.id.register_yearlevel);
        registerAcc = (Button) popupView.findViewById(R.id.register_createAcc);
        registerClose = (Button) popupView.findViewById(R.id.register_cancel);
        registerUsrMsg = (TextView) popupView.findViewById(R.id.register_userMsg);

        builder.setView(popupView);
        dialog = builder.create();
        registerAcc.setOnClickListener(new View.OnClickListener() {

//            Run UI shit, then on a separate thread, wait for server to respond(which is running on ANOTHER thread).
//            After server responds, act accordingly on the separate thread.
            @Override
            public void onClick(View v) {
                registerAcc.setClickable(false);
                registerClose.setClickable(false);
                registerUsrMsg.setVisibility(View.VISIBLE);
                registerUsrMsg.setText("processing");

                register_waitForThreads = new Register_waitForThreads();
                new Thread(register_waitForThreads).start();
            }
        });

        registerClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

    }

    class Register_waitForThreads implements Runnable{

        public Register_waitForThreads() {
        }
        @Override
        public void run() {
            Server_makeRequest server_makeRequest = new Server_makeRequest(""+ registerIdInput.getText(), ""+ registerNameInput.getText(), ""+ registerYrLevelInput.getText());

            ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
            ScheduledFuture future = scheduledExecutorService.schedule( server_makeRequest , 0, TimeUnit.SECONDS );

            while (!future.isDone()){

            }

            if (server_makeRequest.makeRequestOut == -1){
                registerUsrMsg.setText("Please provide valid inputs");
                registerClose.setClickable(true);
                registerAcc.setClickable(true);
            }
            else if (server_makeRequest.makeRequestOut == -2) {
                registerUsrMsg.setText("User ID is already in use");
                registerClose.setClickable(true);
                registerAcc.setClickable(true);
            }
            else{
                Snackbar.make(findViewById(android.R.id.content), "account requested with ID: " + registerIdInput.getText(), BaseTransientBottomBar.LENGTH_LONG).show();
                dialog.dismiss();
            }
            Log.d("server", "finished running wait for threads");
        }
    }
    class Server_makeRequest implements Runnable{
        String id;
        String name;
        String yrLevel;
        public int makeRequestOut = 0;
        public Server_makeRequest(String id, String name, String yrLevel) {
            this.id = id;
            this.name = name;
            this.yrLevel = yrLevel;
            this.makeRequestOut = 0;
        }

        @Override
        public void run() {
            try{
                Request req = new Request("MAKE_REGISTER_REQUEST");
                req.addParam(CISConstants.USER_ID_PARAM, id);
                req.addParam(CISConstants.USER_NAME_PARAM, name);
                req.addParam(CISConstants.YEAR_LEVEL_PARAM, yrLevel);
                String result = SimpleClient.makeRequest(CISConstants.HOST, req);
                Log.d("server", "MAKE_REGISTER_REQUEST: " + result);
                if (result.equals("SUCCESS"))
                    makeRequestOut = 1;
                else if (result.equals("MISSING_PARAMS")){
                    makeRequestOut = -1;
                }
                else {
                    makeRequestOut = -2;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.d("server", "ERR: "+ e.getMessage());
                makeRequestOut = -2;
            }
            Log.d("server", "finished running make request with out: " + makeRequestOut);
        }
    }

}