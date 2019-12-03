package cn.com.wupower.application;

import android.app.Application;
import android.util.Log;

public class App extends Application {
    private static final String TAG = App.class.getSimpleName();

    private App INSTANCE;

    public App getINSTANCE(){
        if (INSTANCE == null){
            Log.d(TAG,"getINSTANCE");
            INSTANCE = new App();
        }
        return INSTANCE;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        INSTANCE = getINSTANCE();
    }
}
