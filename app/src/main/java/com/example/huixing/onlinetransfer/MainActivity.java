package com.example.huixing.onlinetransfer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends AppCompatActivity {

    private EditText IMSIEdit;
    private Button transfer;
    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {



        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IMSIEdit = (EditText) findViewById(R.id.imsi);
        transfer = (Button) findViewById(R.id.transfer);
        textView = (TextView) findViewById(R.id.showPhone);

        IMSIEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                TextView t = (TextView)view;
                t.setText("");
            }
        });

        transfer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread thread = new Thread(new NetWorkThread());
                thread.start();
            }
        });

    }

    class NetWorkThread implements Runnable{

        @Override
        public void run() {
            try {
                String IMSI = String.valueOf(IMSIEdit.getText());
                if (IMSI.length() != 15) {
                    textView.setText("请输入15位的IMSI号");
                    return;
                }
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[] { new TrustAllManager() }, new SecureRandom());

                URL url = new URL("https://112.74.33.184/v1/phone?IMSI=" + IMSI);
                HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
                HttpsURLConnection.setDefaultHostnameVerifier(new TrustHostnameVerifier());
                HttpsURLConnection httpURLConnection = (HttpsURLConnection) url.openConnection();


                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.connect();

                if(200 == httpURLConnection.getResponseCode()){
                    //得到输入流
                    InputStream is =httpURLConnection.getInputStream();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));
                    StringBuilder stringBuilder = new StringBuilder();
                    String temp;
                    while((temp = bufferedReader.readLine()) != null){
                        stringBuilder.append(temp);
                    }
                    if (stringBuilder.toString() == null) {
                        textView.setText("无号码");

                    } else {
                        textView.setText(stringBuilder.toString());
                    }
                } else {
                    textView.setText("无号码");
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (KeyManagementException e) {
                e.printStackTrace();
            }
        }
    }

    private class TrustHostnameVerifier implements HostnameVerifier {

        // 信任所有主机-对于任何证书都不做检查
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private class TrustAllManager implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}
