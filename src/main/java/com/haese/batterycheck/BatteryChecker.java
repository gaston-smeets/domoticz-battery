package com.haese.batterycheck;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class BatteryChecker {

    private <ConnectionSocketFactory> CloseableHttpClient getClient() throws Exception {
        SSLContext context = SSLContexts.custom().loadTrustMaterial(TrustSelfSignedStrategy.INSTANCE).build();

        Registry<ConnectionSocketFactory> registry =
                                                     RegistryBuilder.<ConnectionSocketFactory> create()
                                                                    .register("http",
                                                                              (ConnectionSocketFactory)PlainConnectionSocketFactory.INSTANCE)
                                                                    .register("https",
                                                                              (ConnectionSocketFactory)new SSLConnectionSocketFactory(
                                                                                                                                      context,
                                                                                                                                      NoopHostnameVerifier.INSTANCE))
                                                                    .build();

        PoolingHttpClientConnectionManager connectionManager =
                                                               new PoolingHttpClientConnectionManager(
                                                                                                      (Registry<org.apache.http.conn.socket.ConnectionSocketFactory>)registry);

        return HttpClients.custom().setConnectionManager(connectionManager).build();
    }

    public void sendGetRequest() {
        try {
            CloseableHttpClient httpclient = this.getClient();
            //CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet("https://haese.freeddns.org:8443/json.htm?type=devices&order=name");
            httpGet.addHeader("Authorization", "Basic xxx");
            CloseableHttpResponse response = httpclient.execute(httpGet);
            HttpEntity entity = response.getEntity();
            String json_string = EntityUtils.toString(response.getEntity());
            JSONObject jsonResponse = new JSONObject(json_string);
            JSONArray devices = jsonResponse.getJSONArray("result");
            // Read the contents of an entity and return it as a String.
            for (int i = 0; i < devices.length(); ++i) {
                JSONObject device = devices.getJSONObject(i);
                int level = device.getInt("BatteryLevel");
                int used = device.getInt("Used");
                if ((level < 100) && (used == 1)) {
                    System.out.println(device.getString("Name") + " heeft niveau " + level);
                }
            }
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
