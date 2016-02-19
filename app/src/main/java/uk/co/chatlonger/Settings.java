package uk.co.chatlonger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import org.json.JSONObject;

public class Settings extends Activity {
    private String PROJECT_NUMBER = "536741991679";
    private DatabaseConnector connector;
    private GoogleCloudMessaging gcm;
    private EditText pollFreqBox;
    private RadioGroup radioButtonGroup;
    private Button button;
    private Context thisContext;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        connector = new DatabaseConnector(this);
        pollFreqBox= (EditText) findViewById(R.id.pollFreqBox);
        radioButtonGroup = (RadioGroup) findViewById(R.id.deliveryRadioGroup);
        button = (Button) findViewById(R.id.saveButton);
        pollFreqBox.setText(connector.getConvigVar("pollFreq"));
        thisContext = this;
        if (connector.getConvigVar("deliveryPref").equals("PULL")) ((RadioButton) findViewById(R.id.PULL)).setChecked(true);
        else if (connector.getConvigVar("deliveryPref").equals("PUSH")) ((RadioButton) findViewById(R.id.PUSH)).setChecked(true);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                save();
            }
        });
        ((findViewById(R.id.resetButton))).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                thisContext.deleteDatabase("ChatLonger");
                Toast toast = Toast.makeText(thisContext, "Database Deleted. Please restart the app.", Toast.LENGTH_LONG);
                toast.show();
            }
        });

    }

    public void save()
    {
        int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
        View radioButton = radioButtonGroup.findViewById(radioButtonID);
        int idx = radioButtonGroup.indexOfChild(radioButton);
        if (idx == 0)
        {
            connector.updateConfigVar("deliveryPref", "PULL");
            startPollingService();
            disableGCM();

        }
        else if (idx == 1)
        {
            connector.updateConfigVar("deliveryPref", "PUSH");
            enableGCM();
            stopPollingService();
        }
        connector.updateConfigVar("pollFreq", String.valueOf(pollFreqBox.getText().toString()));

        Toast toast = Toast.makeText(this, "Please restart ChatLonger for these changes to take effect", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 0);
        toast.show();

    }

    public void disableGCM()
    {
        new Thread(new Runnable() {
            public void run() {
                try {
                    GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    gcm.unregister();
                    JSONObject obj = new JSONObject();
                    obj.put("id", connector.getUserID());
                    obj.put("api", connector.getUserAPI());
                    obj.put("regid", "NULL");
                    JSONObject receivedObj = new JsonConnector().getJson(obj, "http://comms.chatlonger.co.uk/users/gcmid");
                } catch (Exception e){
                    Looper.prepare();
                    Toast toast = Toast.makeText(thisContext, "An error occurred whilst disabling PUSH Notifications", Toast.LENGTH_LONG);
                    toast.show();
                }
            }
        }).start();

    }

    public void enableGCM()
    {

        String msgResponse = "";
        new Thread(new Runnable() {
            public void run() {
                String regid;
                try {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    regid = gcm.register(PROJECT_NUMBER);
                    android.os.Message msg = new android.os.Message();
                    Bundle b = new Bundle();
                    b.putString("message", regid);
                    msg.setData(b);
                    handler.sendMessage(msg);
                } catch (Exception ex) {
                    regid = "ERROR";
                }

            }

            public Handler handler = new Handler() {
                public void handleMessage(android.os.Message msg) {
                    String aResponse = msg.getData().getString("message");
                    try {
                        connector.updateConfigVar("GCMRegID", aResponse);
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

    public void startPollingService() {
        Intent mServiceIntent = new Intent(getApplicationContext(), PollService.class);
        this.startService(mServiceIntent);
    }

    public void stopPollingService()
    {
        Intent mServiceIntent = new Intent(getApplicationContext(), PollService.class);
        this.stopService(mServiceIntent);
    }
}
