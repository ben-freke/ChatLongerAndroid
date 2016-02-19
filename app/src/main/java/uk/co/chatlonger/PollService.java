package uk.co.chatlonger;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

/**
 * Created by Ben Freke on 19/02/2016.
 */
public class PollService extends IntentService {

    final static String MY_ACTION = "MY_ACTION";
    private DatabaseConnector connector;
    private JsonConnector jsonConnector;
    private String apiKey;
    private int userID;
    private static String CHATLONGER_URL_RECEIVE = "http://comms.chatlonger.co.uk:80/messages/receive";

    public PollService()
    {
        super("PollService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        connector = new DatabaseConnector(getApplicationContext());
        jsonConnector = new JsonConnector();
        apiKey = connector.getUserAPI();
        userID = connector.getUserID();
    }

    @Override
    protected void onHandleIntent(Intent recInt) {
        int pollFreq = Integer.parseInt(connector.getConvigVar("pollFreq"));
        while (true)
        {
            try {
                poll();
                Thread.sleep(pollFreq);
            } catch (InterruptedException e) {
                //Error
            }
        }

    }

    public void poll()
    {
        try {

            JSONObject object = new JSONObject();
            object.put("userid", userID);
            object.put("user_api_key", apiKey);
            JSONObject msgObj = jsonConnector.getJson(object, CHATLONGER_URL_RECEIVE);
            if (!(msgObj.toString().equals("[]"))){
                int i = 1;
                while (msgObj.getJSONObject(String.valueOf(i)).getString("content") != null){
                    JSONObject object1 = msgObj.getJSONObject(String.valueOf(i));
                    connector.message(
                            Integer.parseInt(object1.getString("id")),
                            Integer.parseInt(object1.getString("sender")),
                            Integer.parseInt(object1.getString("receiver")),
                            object1.getString("content"),
                            object1.getString("timestamp")
                    );
                    Intent intent = new Intent();
                    intent.putExtra("message", object1.getString("content"));
                    intent.putExtra("sender", Integer.parseInt(object1.getString("sender")));
                    intent.setAction(MY_ACTION);
                    sendBroadcast(intent);
                    i++;
                }
            }
        } catch (Exception e)
        {
            //Error
        }
    }



}
