package uk.co.chatlonger;

import android.os.Bundle;
import android.os.Message;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Ben Freke on 31/01/2016.
 */
public class JsonConnector {

    public JSONObject getJson(JSONObject reqObj, String targetUrl){

      try {

          URL url;
          url = new URL(targetUrl);
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
          out.write(reqObj.toString());
          out.close();
          BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
          StringBuilder sb = new StringBuilder();
          String line;
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
