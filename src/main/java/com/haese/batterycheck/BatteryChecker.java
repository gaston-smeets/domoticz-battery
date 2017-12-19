package com.haese.batterycheck;

import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class BatteryChecker {

    private <ConnectionSocketFactory> CloseableHttpClient getClient() throws Exception {
        SSLContext context = SSLContexts.custom().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build();

        Registry<ConnectionSocketFactory> registry =
            RegistryBuilder.<ConnectionSocketFactory> create()
            .register("http", (ConnectionSocketFactory)PlainConnectionSocketFactory.INSTANCE)
            .register("https", (ConnectionSocketFactory)new SSLConnectionSocketFactory(
                context, NoopHostnameVerifier.INSTANCE))
            .build();
        PoolingHttpClientConnectionManager connectionManager =
            new PoolingHttpClientConnectionManager(
                (Registry<org.apache.http.conn.socket.ConnectionSocketFactory>)registry);
        return HttpClients.custom().setConnectionManager(connectionManager).build();
    }
    
    public String getBatteryStatus(String response) throws Exception {
        String status = "";
        JSONObject json = new JSONObject(response);
        JSONArray devices = json.getJSONArray("result");
        // Read the contents of  entity and return it as a String.
        for (int i = 0; i < devices.length(); ++i) {
            JSONObject device = devices.getJSONObject(i);
             int level = device.getInt("BatteryLevel");
             int used = device.getInt("Used");
                if ((level < 100) && (used == 1)) {
                    // System.out.println(device.getString("Name") + " heeft niveau " + level + "%\n");
                    status += device.getString("Name") + " heeft batterij niveau " + level + "%\n";
                }
            }
        return status;
    }
    
    public void sendBatteryStatus(CloseableHttpClient httpclient, String message) throws Exception {
        HttpPost httpPost = new HttpPost("https://api.prowlapp.com/publicapi/add");
        List <NameValuePair> nvps = new ArrayList <NameValuePair>();
        nvps.add(new BasicNameValuePair("apikey", "76dc7cc947cca54c7771dc105ebc74e2ff25ceff"));
        nvps.add(new BasicNameValuePair("priority", "high"));
        nvps.add(new BasicNameValuePair("providerkey", "7725729c6d1ecb09e7914397b0eaf0163f281b2b"));
        nvps.add(new BasicNameValuePair("application", "domoticz"));
        nvps.add(new BasicNameValuePair("event", "Batterij status"));
        nvps.add(new BasicNameValuePair("description", message));     
        
        httpPost.setEntity(new UrlEncodedFormEntity(nvps));
        CloseableHttpResponse response2 = httpclient.execute(httpPost);
        try {
            //System.out.println(response2.getStatusLine());
            HttpEntity entity2 = response2.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity2);
        } finally {
            response2.close();
        }
    }


    public String sendGetRequest(CloseableHttpClient httpclient) {
        String json_string = "";
        try {
            HttpGet httpGet = new HttpGet("http://192.168.178.102:8080/json.htm?type=devices&order=name");
            //httpGet.addHeader("Authorization", "Basic xxx");
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            json_string = EntityUtils.toString(response.getEntity());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return json_string;
    }

    public static void main(String[] args) {
        try {
            BatteryChecker checker = new BatteryChecker();
            CloseableHttpClient httpclient = checker.getClient();
            String result = checker.sendGetRequest(httpclient);
            String message = checker.getBatteryStatus(result);
            checker.sendBatteryStatus(httpclient, message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
