package uk.co.chatlonger;

import android.os.Bundle;
import android.os.Message;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
public class JsonConnector {

    public JSONObject getJson(JSONObject reqObj, String targetUrl){

        try {

            URL url;
            url = new URL(targetUrl);
            final HttpURLConnection urlConn;
            DataOutputStream printout;
            DataInputStream  input;
            urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setRequestMethod("POST");
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setRequestProperty("Content-Type", "application/json");
            urlConn.setRequestProperty("Host", "android.schoolportal.gr");
            urlConn.connect();
            OutputStreamWriter out = new   OutputStreamWriter(urlConn.getOutputStream());
            out.write(reqObj.toString());
            out.close();
            BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            StringBuilder sb = new StringBuilder();
            String line;
            int status = urlConn.getResponseCode();
            while ((line = br.readLine()) != null) {
                sb.append(line+"\n");
            }
            br.close();
            String jsonStuff = sb.toString();
            JSONObject returnedObject = new JSONObject(jsonStuff);
            return returnedObject;

        } catch (Exception e){
            return null;
        }

    }

}
