package uk.co.chatlonger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class LoginActivity extends Activity {

    private DatabaseConnector connector;
    private Context context;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.deleteDatabase("ChatLonger");
        setContentView(R.layout.activity_login);
        connector = new DatabaseConnector(this);
        context = this;
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

}
