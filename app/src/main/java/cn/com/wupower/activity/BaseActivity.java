package cn.com.wupower.activity;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity {

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("unchecked")
    public <T> T $(int viewId){
        return (T) findViewById(viewId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
