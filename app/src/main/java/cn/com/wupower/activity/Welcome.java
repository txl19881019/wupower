package cn.com.wupower.activity;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import cn.com.wupower.R;

public class Welcome extends BaseActivity {
    private static final String TAG = Welcome.class.getSimpleName();

    private Handler handler;
    private Runnable runnable;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Welcome.this,MainActivity.class));
                finish();
            }
        };
        handler.postDelayed(runnable,5000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}
