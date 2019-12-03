package cn.com.wupower.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import cn.com.wupower.R;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG,"onCreate");
        setContentView(R.layout.activity_main);
        button = $(R.id.btn_call);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Log.d(TAG,Thread.currentThread().getName());
                    }
                }).start();
            }
        });
    }
}
