package uk.co.chatlonger;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.apache.http.*;
import org.json.JSONObject;
import android.os.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.OutputStreamWriter;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private String apiKey;
    private int userID;
    private Context messageContext;
    private int conversationRecipient;
    private DatabaseConnector connector;
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
                TextView title = (TextView) findViewById(R.id.convName);
                conversationRecipient = Integer.parseInt(connector.getConversationRecipient(conversationID));
                title.setText(connector.getConversationName(conversationID));


       /** new Thread(new Runnable() {
            public void run() { **/
                String messages[][] = connector.getMessages(conversationID);
                LinearLayout linearLayout = (LinearLayout)findViewById(R.id.msgLayout);
                if (messages != null){

                    for (int i = 0; i < messages.length; i++){
                        TextView msg = new TextView(messageContext);
                        msg.setText(messages[i][2]);
                        msg.setTextSize(25);
                        if (messages[i][0].equals(String.valueOf(userID))) msg.setGravity(Gravity.RIGHT);
                        else msg.setGravity(Gravity.LEFT);
                        linearLayout.addView(msg);
                    }
                }
            /**}
        }).start();**/
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
        int duration = Toast.LENGTH_SHORT;
        Context context = getApplicationContext();
        LinearLayout linearLayout = (LinearLayout)findViewById(R.id.msgLayout);
        TextView msg = new TextView(messageContext);
        msg.setText(message);
        msg.setTextSize(25);
        msg.setGravity(Gravity.RIGHT);
        linearLayout.addView(msg);
        new Thread(new Runnable() {
            public void run() {
                try {
                    JsonConnector json = new JsonConnector();
                    JSONObject object = new JSONObject();
                    object.put("userid", userID);
                    object.put("user_api_key", apiKey);
                    object.put("recipient", conversationRecipient);
                    object.put("message", message);
                    JSONObject msgObj = json.getJson(object, "http://10.0.2.2:8181/messages/send");
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

    public void postData(final String message, final int recipient) {

        new Thread(new Runnable() {
            public void run() {

                URL url;

                String response = "";
                try {
                    JSONObject object = new JSONObject();
                    object.put("userid", userID);
                    object.put("user_api_key", apiKey);
                    object.put("recipient", recipient);
                    object.put("message", message);

                    url = new URL("http://10.0.2.2:8181/messages/send");

                    /** HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                     conn.setReadTimeout(15000);
                     conn.setConnectTimeout(15000);
                     conn.setRequestMethod("GET");
                     conn.setDoInput(true);
                     conn.setDoOutput(true); **/

                    HttpURLConnection urlConn;
                    DataOutputStream printout;
                    DataInputStream  input;
                    urlConn = (HttpURLConnection) url.openConnection();
                    urlConn.setDoInput (true);
                    urlConn.setDoOutput(true);
                    urlConn.setUseCaches(false);
                    urlConn.setRequestProperty("Content-Type", "application/json");
                    urlConn.setRequestProperty("Host", "android.schoolportal.gr");
                    urlConn.connect();
                    OutputStreamWriter out = new   OutputStreamWriter(urlConn.getOutputStream());
                    out.write(object.toString());
                    out.close();

                    int HttpResult = urlConn.getResponseCode();
                    if(HttpResult ==HttpURLConnection.HTTP_OK){
                    }else{
                        Context context = getApplicationContext();
                    }

                }
                catch (Exception e) {
                    e.printStackTrace();
                }


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
                        URL url;
                        url = new URL("http://10.0.2.2:8181/messages/receive");
                        final HttpURLConnection urlConn;
                        DataOutputStream printout;
                        DataInputStream  input;
                        urlConn = (HttpURLConnection) url.openConnection();
                        urlConn.setDoInput(true);
                        urlConn.setDoOutput(true);
                        urlConn.setUseCaches(false);
                        urlConn.setRequestProperty("Content-Type", "application/json");
                        urlConn.setRequestProperty("Host", "android.schoolportal.gr");
                        urlConn.connect();
                        OutputStreamWriter out = new   OutputStreamWriter(urlConn.getOutputStream());
                        out.write(object.toString());
                        out.close();
                        BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                        StringBuilder sb = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            sb.append(line+"\n");
                        }
                        br.close();
                        Message msg = new Message();
                        if (!(sb.toString().equals("[]"))){
                            Bundle b = new Bundle();
                            b.putString("message", sb.toString());
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
                               LinearLayout linearLayout = (LinearLayout)findViewById(R.id.msgLayout);
                               TextView textMsg = new TextView(messageContext);
                               textMsg.setText(msgObj.getString("content"));
                               textMsg.setTextSize(25);
                               if (msgObj.getString("sender").equals(String.valueOf(userID))) textMsg.setGravity(Gravity.RIGHT);
                               else textMsg.setGravity(Gravity.LEFT);
                               linearLayout.addView(textMsg);

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
}


