package com.haese.batterycheck;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;


public class BatteryChecker {

    public void sendGetRequest() {
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("https://haese.freeddns.org:8443/json.htm?type=command&param=getlightswitches");
            CloseableHttpResponse response = httpclient.execute(httpGet);
            System.out.println(response);
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Hello World!");
        BatteryChecker checker = new BatteryChecker();
        checker.sendGetRequest();
        System.out.println("Hello World!");
    }
}
