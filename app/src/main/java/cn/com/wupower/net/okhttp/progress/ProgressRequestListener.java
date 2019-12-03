package cn.com.wupower.net.okhttp.progress;

public interface ProgressRequestListener {
    void onRequestProgress(long bytesWritten, long contentLength, boolean done);
}