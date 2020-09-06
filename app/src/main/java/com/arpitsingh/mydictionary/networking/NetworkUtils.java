package com.arpitsingh.mydictionary.networking;
/**
 * Created by ARPIT SINGH
 * 11/10/19
 */

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;

import com.arpitsingh.mydictionary.model.SearchResult;

import java.security.cert.CertificateException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class NetworkUtils {


    public static void queryWord(String mSearchKey, Callback<SearchResult> resultCallback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://gist.githubusercontent.com/")
                .client(getUnsafeOkHttpClient().build())
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        OxfordApi api = retrofit.create(OxfordApi.class);

        Log.v("BaseUrl ", retrofit.baseUrl().toString());

        /**
         * using hardcoded .json response since oxford api is not allowing
         * search endpoint for prototype accounts.
         * Call<SearchResult> call = api.getSearchResults(mSearchKey.toLowerCase());
         */
        Call<SearchResult> call = api.getSearchResults();
        call.enqueue(resultCallback);
    }

    public static void queryDefinitions(String mWordId, Callback<String> resultCallback) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(OxfordApi.BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        OxfordApi api = retrofit.create(OxfordApi.class);

        Call<String> call = api.getDefinitions(mWordId);

        call.enqueue(resultCallback);
    }

    public static void queryWordOfDay(Uri url, Callback<String> resultCallback) {
        Log.d("Base url: ","Scheme: "+ url.getScheme() + " Author: " + url.getAuthority());
        Log.d("Path url: ",url.getPath());
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://10.0.2.2/api/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();

        OxfordApi api = retrofit.create(OxfordApi.class);
        Call<String> call = api.getWordOfDay(url.toString());
        call.enqueue(resultCallback);
    }

    //helper method to monitor network status
    public static boolean checkConnectivity(ConnectivityManager mConnectivityManager) {
        NetworkInfo activeNetwork = mConnectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }

    public static OkHttpClient.Builder getUnsafeOkHttpClient() {

        try {
            // Create a trust manager that does not validate certificate chains
            final TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        @Override
                        public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                        }

                        @Override
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return new java.security.cert.X509Certificate[]{};
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            OkHttpClient.Builder builder = new OkHttpClient.Builder();
            builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
            builder.hostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            });
            return builder;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
