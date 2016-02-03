package uk.co.chatlonger;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
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


public class MainActivity extends AppCompatActivity {
    private String apiKey;
    private int userID;
    private Context messageContext;
    private int conversationRecipient;
    private DatabaseConnector connector;
    private JsonConnector jsonConnector;


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
        //ggthis.deleteDatabase("ChatLonger");
        build();
        pollData();

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
                    JSONObject msgObj = jsonConnector.getJson(object, "http://10.0.2.2:8181/messages/send");
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

    public void pollData(){
        try {

            final String json;

           new Thread (new Runnable() {

               public void run(){
                   while (true){
                       try{
                           getData();
                           Thread.sleep(3000);
                       } catch (Exception e){}
                   }
               }

               public void getData() {
                    try {
                        JSONObject object = new JSONObject();
                        object.put("userid", userID);
                        object.put("user_api_key", apiKey);
                        JSONObject msgObj = jsonConnector.getJson(object, "http://10.0.2.2:8181/messages/receive");
                        if (!(msgObj.toString().equals("[]"))){
                            Message msg = new Message();
                            Bundle b = new Bundle();
                            b.putString("message", msgObj.toString());
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    } catch (Exception e){}
                }

               public Handler handler = new Handler() {

                   public void handleMessage(Message msg) {
                       String aResponse = msg.getData().getString("message");
                       try {
                           JSONObject jObject = new JSONObject(aResponse);
                           int i = 1;


                           while (jObject.getJSONObject(String.valueOf(i)).getString("content") != null){
                               JSONObject msgObj = jObject.getJSONObject(String.valueOf(i));
                               putMessage(msgObj.getString("content"), false);
                               int id = Integer.parseInt(msgObj.getString("id"));
                               int sender = Integer.parseInt(msgObj.getString("sender"));
                               int receiver = Integer.parseInt(msgObj.getString("receiver"));
                               String timestamp = msgObj.getString("timestamp");
                               String content = msgObj.getString("content");
                               connector.message(id, sender, receiver, content, timestamp);

                               i++;
                           }
                       } catch (Exception e) {}
                   }
               };
            }).start();
        } catch (Exception e){}
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

    private void putMessage(String message, boolean sender){
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

}


