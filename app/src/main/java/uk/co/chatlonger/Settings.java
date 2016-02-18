package uk.co.chatlonger;

import android.app.Activity;
import android.os.Bundle;

import com.google.android.gms.gcm.GoogleCloudMessaging;

public class Settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }

    protected boolean disableGCM()
    {
        try {
            GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
            gcm.unregister();
            return true;
        } catch (Exception e){
            return false;
        }
    }

    protected void enableGCM()
    {

    }
}
