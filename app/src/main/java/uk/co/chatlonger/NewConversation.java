package uk.co.chatlonger;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONObject;

public class NewConversation extends Activity {
    private Context activityContext;
    private DatabaseConnector connector;
    private JsonConnector jsonConnector;
    private String apiKey;
    private int userID;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        connector = new DatabaseConnector(this);
        activityContext = this;
        jsonConnector = new JsonConnector();
        apiKey = connector.getUserAPI();
        userID = connector.getUserID();

        final Button button = (Button) findViewById(R.id.submitButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editTxt = (EditText) findViewById(R.id.editText);
                getUser(editTxt.getText().toString());
            }
        });

    }

    protected void getUser(final String email){
        boolean result = false;
        new Thread(new Runnable() {
            public void run() {
                try {
                    Message msg = new Message();
                    Bundle b = new Bundle();
                    JSONObject object = new JSONObject();
                    object.put("userid", userID);
                    object.put("user_api_key", apiKey);
                    object.put("email", email);
                    JSONObject usrObj = jsonConnector.getJson(object, "http://comms.chatlonger.co.uk:80/messages/conversation");
                    if (usrObj == null){
                        b.putInt("success", 0);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                    else{
                        b.putInt("success", 1);
                        int id = Integer.parseInt(usrObj.getString("id"));
                        int user1 = Integer.parseInt(usrObj.getString("user1"));
                        int user2 = Integer.parseInt(usrObj.getString("user2"));
                        String name = usrObj.getString("name");
                        connector.putConversation(id, user1, user2, name);
                        msg.setData(b);
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {}
            }

            public Handler handler = new Handler() {

                public void handleMessage(Message msg) {
                    if(msg.getData().getInt("success") == 1){
                        EditText editTxt = (EditText) findViewById(R.id.editText);
                        editTxt.setText(null);
                        TextView msgTextView = (TextView) findViewById(R.id.screenMsg);
                        msgTextView.setText("Added!");
                    } else {
                        TextView msgTextView = (TextView) findViewById(R.id.screenMsg);
                        msgTextView.setText("User Not Found");
                    }

                }
            };

        }).start();

    }

}
