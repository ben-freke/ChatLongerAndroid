package uk.co.chatlonger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONObject;
import android.os.*;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private String apiKey;
    final static String MY_ACTION = "MY_ACTION";
    private int userID;
    private Context messageContext;
    private int conversationRecipient;
    private DatabaseConnector connector;
    private JsonConnector jsonConnector;
    private static String CHATLONGER_URL_SEND = "http://comms.chatlonger.co.uk:80/messages/send";
    private static String CHATLONGER_URL_RECEIVE = "http://comms.chatlonger.co.uk:80/messages/receive";
    MyReceiver myReceiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final Button button = (Button) findViewById(R.id.msgButton);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText editTxt = (EditText) findViewById(R.id.msgEditTxt);
                sendMessage(editTxt.getText().toString());
                editTxt.setText(null);
            }
        });
        jsonConnector = new JsonConnector();

        messageContext = this;
        build();
        //pollData();

    }

    public void build(){
        Intent intent = getIntent();
        String convID = intent.getStringExtra("conversationID");
        final int conversationID = Integer.parseInt(convID);
        connector = new DatabaseConnector(messageContext);


                apiKey = connector.getUserAPI();
                userID = connector.getUserID();
                conversationRecipient = Integer.parseInt(connector.getConversationRecipient(conversationID));
                setTitle(connector.getConversationName(conversationID));


       new Thread(new Runnable() {
            public void run() {
                String messages[][] = connector.getMessages(conversationID);
                if (messages != null){

                    for (int i = 0; i < messages.length; i++){
                        putMessage(messages[i][2], messages[i][0].equals(String.valueOf(userID)));
                    }
                }
            }
        }).start();
        doScroll();
    }

    public void onStart()
    {
        super.onStart();
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(GCMHandler.MY_ACTION);
        registerReceiver(myReceiver, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void sendMessage(final String message) {
        putMessage(message, true);
        new Thread(new Runnable() {
            public void run() {
                try {
                    JSONObject object = new JSONObject();
                    object.put("userid", userID);
                    object.put("user_api_key", apiKey);
                    object.put("recipient", conversationRecipient);
                    object.put("message", message);
                    JSONObject msgObj = jsonConnector.getJson(object, CHATLONGER_URL_SEND);
                    int id = Integer.parseInt(msgObj.getString("id"));
                    int sender = Integer.parseInt(msgObj.getString("sender"));
                    int receiver = Integer.parseInt(msgObj.getString("receiver"));
                    String timestamp = msgObj.getString("timestamp");
                    String content = msgObj.getString("content");
                    connector.message(id, sender, receiver, content, timestamp);
                } catch (Exception e){}
            }
        }).start();

    }

    private void doScroll(){
        final ScrollView scroll = (ScrollView)findViewById(R.id.scroll);
        scroll.post(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    public void putMessage(String message, boolean sender){
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.msgLayout);
        TextView msg = new TextView(messageContext);
        msg.setText(message);
        msg.setTextSize(18);
        if (sender){
            msg.setGravity(Gravity.RIGHT);
            msg.setTextColor(Color.BLUE);
        }
        else msg.setGravity(Gravity.LEFT);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        msg.setMaxWidth((size.x)/2);
        linearLayout.addView(msg);

        doScroll();
    }

    private class MyReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            if (arg1.getIntExtra("sender", -1) == conversationRecipient)
            {
                putMessage(arg1.getStringExtra("message"), false);
            }
        }
    }
}


