package uk.co.chatlonger;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

public class GCMHandler extends IntentService {

        String mes;
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
            mes = extras.getString("title");
            showToast();
            Log.i("GCM", "Received : (" +messageType+")  "+extras.getString("title"));
        }
        public void showToast(){
            handler.post(new Runnable() {
                public void run() {
                    Toast.makeText(getApplicationContext(),mes , Toast.LENGTH_LONG).show();
                }
            });
        }
}

