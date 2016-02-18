package uk.co.chatlonger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

import java.io.IOException;

public class LoginActivity extends Activity {
    Button btnRegId;
    EditText etRegId;
    GoogleCloudMessaging gcm;
    String PROJECT_NUMBER = "536741991679";
    private DatabaseConnector connector;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
       // this.deleteDatabase("ChatLonger");
        setContentView(R.layout.activity_login);
        connector = new DatabaseConnector(this);
        if (checkStatus()) {
            /**
             * If the user is logged in, redirect them to the MessagesScreen
             */
            Intent intent = new Intent(this, MessagesScreen.class);
            startActivity(intent);
        }
        final Button button = (Button) findViewById(R.id.loginButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                build();
            }
        });
    }

    private void build()
    {
        new Thread(new Runnable() {
            public void run() {
                try {
                    EditText emailInput = (EditText) findViewById(R.id.emailInput);
                    EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    String email = emailInput.getText().toString();
                    String password = passwordInput.getText().toString();
                    JsonConnector jsonConnector = new JsonConnector();
                    JSONObject obj = new JSONObject();
                    obj.put("email", email);
                    obj.put("password", password);
                    JSONObject receivedObj = jsonConnector.getJson(obj, "http://comms.chatlonger.co.uk/users/authenticate");
                    if (receivedObj != null){
                        connector.authenticate(receivedObj.getInt("id"), receivedObj.getString("name"), receivedObj.getString("email"), receivedObj.getString("apikey"));
                        b.putInt("success", 1);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    } else {
                        b.putInt("success", 0);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                } catch (Exception e){}
            }

            public Handler handler = new Handler() {

                public void handleMessage(Message msg) {
                    if(msg.getData().getInt("success") == 1){
                        if (connector.getConvigVar("GCMRegID") == null ) devID();
                        Intent intent = new Intent(context, MessagesScreen.class);
                        startActivity(intent);
                    } else {
                        EditText emailInput = (EditText) findViewById(R.id.emailInput);
                        emailInput.setText(null);
                        EditText passwordInput = (EditText) findViewById(R.id.passwordInput);
                        passwordInput.setText(null);
                        TextView msgTextView = (TextView) findViewById(R.id.screenInf);
                        msgTextView.setText("Login Failed");
                    }
                }
            };
        }).start();
    }

    private boolean checkStatus()
    {
        if (connector.getUserID() == -1) return false;
        return true;
    }

    private void devID(){

        String msgResponse = "";
        new Thread(new Runnable() {
            public void run() {
                String regid;
                try {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    regid = gcm.register(PROJECT_NUMBER);
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    b.putString("message", regid);
                    msg.setData(b);
                    handler.sendMessage(msg);
                } catch (Exception ex) {
                    regid = "ERROR";
                }

            }

            public Handler handler = new Handler() {
                public void handleMessage(Message msg) {
                    String aResponse = msg.getData().getString("message");
                    try {
                        connector.addConfigVar("GCMRegID", aResponse);
                        JsonConnector jsonConnector = new JsonConnector();
                        final JSONObject obj = new JSONObject();
                        obj.put("id", connector.getUserID());
                        obj.put("api", connector.getUserAPI());
                        obj.put("regid", aResponse);
                        new Thread(new Runnable() {
                            public void run() {
                                JSONObject receivedObj = new JsonConnector().getJson(obj, "http://comms.chatlonger.co.uk/users/gcmid");
                            }
                            }).start();
                    } catch (Exception e) {}
                }
            };

        }).start();
    }

}
