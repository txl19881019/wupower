package cn.com.wupower.net.okhttp.progress;

public interface ProgressResponseListener {
    void onResponseProgress(long bytesRead, long contentLength, boolean done);
}