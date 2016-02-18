package uk.co.chatlonger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.widget.Toast;

public class GCMHandler extends IntentService {

        String message, timestamp;
        final static String MY_ACTION = "MY_ACTION";
        int sender, receiver, id;
        private Handler handler;
        public GCMHandler() {
            super("GcmMessageHandler");
        }
        @Override
        public void onCreate() {
            super.onCreate();
            handler = new Handler();
        }
        @Override
        protected void onHandleIntent(Intent intent) {
            Bundle extras = intent.getExtras();
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
            String messageType = gcm.getMessageType(intent);
            id = Integer.parseInt(extras.getString("id"));
            message = extras.getString("message");
            sender = Integer.parseInt(extras.getString("sender"));
            receiver = Integer.parseInt(extras.getString("receiver"));
            timestamp = extras.getString("timestamp");

            putMessage();
        }
        public void putMessage(){
            handler.post(new Runnable() {
                public void run() {
                    DatabaseConnector connector = new DatabaseConnector(getApplicationContext());
                    connector.message(id, sender, receiver, message, timestamp);
                    try {
                        Intent intent = new Intent();
                        intent.putExtra("message", message);
                        intent.putExtra("sender", sender);
                        intent.setAction(MY_ACTION);
                        sendBroadcast(intent);
                    } catch (Exception e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            });
        }
}

