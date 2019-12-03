package cn.com.wupower.net.okhttp;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.com.wupower.net.okhttp.callback.HttpCallback;
import cn.com.wupower.net.okhttp.progress.ProgressRequestBody;
import cn.com.wupower.net.okhttp.progress.ProgressRequestListener;
import okhttp3.Call;
import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * Created by Tao.xiaolong on 2017/10/31.
 */

public class HttpClientHelper {
    private static final String TAG = HttpClientHelper.class.getName();
    private static final int NORMAL_CONNECT_SECONDS = 20;
    private static final int FILE_CONNECT_SECONDS = 120;

    public static final MediaType FORM_CONTENT_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    private static final int UPDATE_REQUEST = 1;

    private static HttpClientHelper mInstance;
    private static HttpClientHelper mFileInstance;
    private OkHttpClient mOkHttpClient;
    private Handler handler;

    private HttpClientHelper(boolean normal){
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(normal?NORMAL_CONNECT_SECONDS:FILE_CONNECT_SECONDS, TimeUnit.SECONDS)
                .sslSocketFactory(createSSLSocketFactory(),new TrustAllCerts())
                .hostnameVerifier(new TrustAllHostnameVerifier())
                .cookieJar(new CookieJar() {
                    private final HashMap<String, List<Cookie>> cookieStore = new HashMap<>();
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        cookieStore.put(url.toString(),cookies);
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        List<Cookie> cookies = cookieStore.get(url.toString());
                        return cookies == null ? new ArrayList<Cookie>() : cookies;
                    }
                }).build();
        handler = new Handler(Looper.getMainLooper());
    }

    public static HttpClientHelper getInstance(){
        if (mInstance == null) {
            synchronized (HttpClientHelper.class) {
                if (mInstance == null) {
                    mInstance = new HttpClientHelper(true);
                }
            }
        }
        return  mInstance;
    }

    public static HttpClientHelper getFileInstance(){
        if (mFileInstance==null){
            synchronized (HttpClientHelper.class){
                if (mFileInstance == null){
                    mFileInstance = new HttpClientHelper(true);
                }
            }
        }
        return mFileInstance;
    }


    private static class TrustAllCerts implements X509TrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    private static class TrustAllHostnameVerifier implements HostnameVerifier {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    private static SSLSocketFactory createSSLSocketFactory() {
        SSLSocketFactory ssfFactory = null;

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, new TrustManager[]{new TrustAllCerts()}, new SecureRandom());

            ssfFactory = sc.getSocketFactory();
        } catch (Exception e) {
        }

        return ssfFactory;
    }

    /**
     * 同步get请求
     * @param url
     * @return Response
     * @throws IOException
     */
    public Response getSync(String url) throws IOException {
        return sync(buildRequest(url,null));
    }

    /**
     *同步get请求
     * @param url
     * @return 字符串
     * @throws IOException
     */
    public String getSyncString(String url) throws IOException {
        return getSync(url).body().string();
    }

    /**
     * 异步get请求
     * @param url
     * @param callback
     * @return
     */
    public Call getAsync(String url, HttpCallback callback){
        return async(buildRequest(url,null),callback);
    }

    /**
     * 同步post请求
     * @param url
     * @param params
     * @return Response
     * @throws IOException
     */
    public Response postSync(String url, Map<String,String> params) throws IOException {
        return sync(buildRequest(url,params));
    }

    /**
     * 同步post请求
     * @param url
     * @param params
     * @return 字符串
     * @throws IOException
     */
    public String postSyncString(String url, Map<String,String> params) throws IOException {
        return postSync(url,params).body().string();
    }

    /**
     * 异步的post请求
     *
     * @param url
     * @param callback
     * @param params
     */
    public Call postAsync(String url, Map<String, String> params, HttpCallback callback) {
        return async(buildRequest(url, params), callback);
    }

    public Call postAsync(String cookie, String url, Map<String, String> params, HttpCallback callback) {
        return async(buildRequest(url, params, cookie), callback);
    }

    public Call postStreamAsync(String url, String json, HttpCallback callback) {
        return async(buildStreamRequest(url, json), callback);
    }

    private Call async(Request request, HttpCallback callback) {
        callback.setHandler(handler);
        callback.onStart();
        Call call = mOkHttpClient.newCall(request);
        callback.setCall(call);
        call.enqueue(callback);
        return call;
    }

    private Response sync(Request request) throws IOException {
        Call call = mOkHttpClient.newCall(request);
        return call.execute();
    }

    private Request buildRequest(String url, Map<String, String> params) {
        return buildRequest(url, params, null);
    }

    private Request buildRequest(String url, Map<String, String> params, String cookie) {
//        StringBuffer sb = new StringBuffer();
//        if (params != null) {
//            //设置表单参数
//            for (String key : params.keySet()) {
//                sb.append(key + "=" + params.get(key) + "&");
//            }
//        }
//        RequestBody body = RequestBody.create(FORM_CONTENT_TYPE, sb.toString());
//
//        Request.Builder builder = new Request.Builder().url(url).post(body);
//        if (cookie != null) {
//            builder.addHeader("Cookie", cookie);
//        }
//        return builder.build();

        Request.Builder requestBuilder = new Request.Builder().url(url);
        if (params != null) {
            FormBody.Builder builder = new FormBody.Builder();
            for (String key : params.keySet()) {
                builder.add(key, params.get(key));
            }
            requestBuilder.post(builder.build());
        }
        if (cookie != null) {
            requestBuilder.addHeader("Cookie", cookie);
        }
        return requestBuilder.build();
    }

    private Request buildStreamRequest(String url, String params) {
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), params);
        Request.Builder requestBuilder = new Request.Builder().url(url);
        requestBuilder.post(body);
        return requestBuilder.build();
    }

    private Request buildMultipartFormRequest(String url, String key, List<String> files, Map<String, String> params, final ProgressRequestListener listener) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (files != null) {
            for (int i = 0; i < files.size(); i++) {
                File f = new File(files.get(i));
                String fileName = f.getName();
                builder.addFormDataPart(key, fileName, RequestBody.create(MediaType.parse(guessMimeType(fileName)), f));
            }
        }
        return buildFormRequest(builder, url, params, listener);
    }

    private Request buildMultipartFormRequest(String url, Map<String, String> files, Map<String, String> params, final ProgressRequestListener listener) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        if (files != null) {
            for (String key : files.keySet()) {
                File f = new File(files.get(key));
                String fileName = f.getName();
                builder.addFormDataPart(key, fileName, RequestBody.create(MediaType.parse(guessMimeType(fileName)), f));
            }
        }
        return buildFormRequest(builder, url, params, listener);
    }

    private Request buildFormRequest(MultipartBody.Builder builder, String url, Map<String, String> params, final ProgressRequestListener listener) {
        if (params != null) {
            for (String key : params.keySet()) {
                builder.addFormDataPart(key, params.get(key));
            }
        }

        RequestBody requestBody;
        if (listener == null) {
            requestBody = builder.build();
        } else {
            requestBody = new ProgressRequestBody(builder.build(), listener);
        }
        Request.Builder requestBuilder = new Request.Builder()
                .url(url)
                .post(requestBody);
        return requestBuilder.build();
    }

    private String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String contentTypeFor = fileNameMap.getContentTypeFor(path);
        if (contentTypeFor == null) {
            contentTypeFor = "application/octet-stream";
        }
        return contentTypeFor;
    }

    public static String getUrl(String str) {
        StringBuffer buffer = new StringBuffer("");
        buffer.append(str);
        Log.i(TAG, buffer.toString());
        return buffer.toString();
    }

    public static String getUrl(String str, Map<String, String> params) {
        StringBuffer buffer = new StringBuffer(getUrl(str));
        if (params != null && !params.isEmpty()) {
            buffer.append("?");
            for (String key : params.keySet()) {
                buffer.append(key + "=" + params.get(key) + "&");
            }
        }
        Log.i(TAG, buffer.toString());
        return buffer.toString();
    }

}
