package cn.com.wupower.net.okhttp.expand;


import cn.com.wupower.net.okhttp.callback.HttpCallback;

/**
 * Created by Administrator on 2017/9/13.
 */

public abstract class UIExpand {
    protected HttpCallback callback;

    public abstract void onRequestStart();

    public abstract void onRequestError(Exception e);

    public abstract void onRequestSuccess();

    public abstract void onRequestCancel();

    public void setCallback(HttpCallback callback) {
        this.callback = callback;
    }
}
