package cn.com.wupower.net.okhttp.callback;


import android.os.Handler;
import android.os.Looper;

import com.alibaba.fastjson.JSON;
import com.google.gson.internal.$Gson$Types;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import cn.com.wupower.net.okhttp.HttpException;
import cn.com.wupower.net.okhttp.expand.UIExpand;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

/**
 * Created by Tao.xiaolong on 2017/10/31.
 */

public abstract class HttpCallback<T> implements Callback {
    protected Type mType;
    private Handler handler;
    protected Call call;
    boolean showNetWork = true;
    protected List<UIExpand> uiExpandList = new ArrayList<>();

    public HttpCallback(){
        mType = getSuperclassTypeParameter(getClass());
    }

    /**
     * 是否显示网络请求异常TOAST
     * 默认为true
     * FALSE的引用场景：隐式请求，不论成功与否都不希望弹出提示
     *
     * @param showNetWork
     */
    public HttpCallback(boolean showNetWork) {
        this.showNetWork = showNetWork;
        mType = getSuperclassTypeParameter(getClass());
    }

    protected Type getSuperclassTypeParameter(Class<?> subclass) {
        Type superclass = subclass.getGenericSuperclass();
        if (superclass instanceof Class) {
            return String.class;
        }
        ParameterizedType parameterized = (ParameterizedType) superclass;
        Type type = $Gson$Types.canonicalize(parameterized.getActualTypeArguments()[0]);
        return type;
    }

    public HttpCallback addUIExpand(UIExpand uiExpand) {
        uiExpand.setCallback(this);
        uiExpandList.add(uiExpand);
        return this;
    }

    public void onStart() {
        for (int i = 0; i < uiExpandList.size(); i++) {
            try {
                uiExpandList.get(i).onRequestStart();
            } catch (NullPointerException e) {
            }
        }
    }

    public void onError(Exception e) {
        e.printStackTrace();
        if (showNetWork) {
        }
        for (int i = 0; i < uiExpandList.size(); i++) {
            try {
                uiExpandList.get(i).onRequestError(e);
            } catch (NullPointerException ne) {
            }
        }
    }

    public static String getMessageFromException(Exception e) {
        if (e instanceof HttpException) {
            return e.getMessage();
        } else {
            return "网络异常";
        }
    }

    public void onResponse(T response) {
    }

    @Override
    public void onFailure(Call call, final IOException e) {
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                try {
                    if (e instanceof SocketTimeoutException) {
                        onError(new HttpException("请求超时"));
                    } else if (e.getMessage().contains("closed")) {
                        //如果是主动取消的情况下
                        for (int i = 0; i < uiExpandList.size(); i++) {
                            uiExpandList.get(i).onRequestCancel();
                        }
                    } else {
                        //其他情况下
                        onError(e);
                    }
                } catch (NullPointerException e) {
                }
            }
        });
    }

    @Override
    public void onResponse(Call call, final Response response) throws IOException {
        final String string = response.body().string();
        final int code = response.code();
        getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (code != 200) {
                    onError(new HttpException("请求异常,错误码:" + code));
                } else {
                    T t = (T) gsonFormat(string);
                    if (t != null) {
                        try {
                            onResponse(t);
                            try {
                                for (int i = 0; i < uiExpandList.size(); i++) {
                                    uiExpandList.get(i).onRequestSuccess();
                                }
                            } catch (NullPointerException e1) {
                            }
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                            onError(new HttpException("处理数据时遇到点问题"));
                        }
                    }
                }
            }
        });
    }

    protected Object gsonFormat(String json) {
        if (mType == String.class) {
            return json;
        } else {
            try {
//                return new Gson().fromJson(json, mType);
                return JSON.parseObject(json, mType);
            } catch (Exception e) {
                onError(new HttpException("数据异常"));
                return null;
            }
        }
    }

    public void setCall(Call call) {
        this.call = call;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public Handler getHandler() {
        if (handler == null) {
            handler = new Handler(Looper.getMainLooper());
        }
        return handler;
    }

    public void cancel() {
        if (call != null && !call.isCanceled()) {
            call.cancel();
        }
    }
}
