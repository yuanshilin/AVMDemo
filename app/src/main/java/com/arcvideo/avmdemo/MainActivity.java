package com.arcvideo.avmdemo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    public final String TAG = "ArcvideoCamera_MainActivity";
    private boolean isRecoder = false;
    private WindowManager windowManager;
    private View controlpanel = null;
    private RelativeLayout relativeLayout;
    private CameraFactory cameraFactory;
    private TextView recodertime;
    private Handler handler;
    public static final int UPDATETIME = -1000;
    public static final int STOPRECODER = -1001;
    // 视频录制时间
    private int RECODER_TIME;
    private void setWindowFlag(){
        Window window = getWindow();
        View decorView = window.getDecorView();
        int flag = decorView.getSystemUiVisibility();

        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // 状态栏隐藏
        flag |= View.SYSTEM_UI_FLAG_FULLSCREEN;
        // 导航栏隐藏
        flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        // 布局延伸到导航栏
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        // 布局延伸到状态栏
        flag |= View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
        // 全屏时,增加沉浸式体验
        flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        //  部分国产机型适用.不加会导致退出全屏时布局被状态栏遮挡
        // activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        // android P 以下的刘海屏,各厂商都有自己的适配方式,具体在manifest.xml中可以看到
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            WindowManager.LayoutParams pa = window.getAttributes();
            pa.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            window.setAttributes(pa);
        }
        decorView.setSystemUiVisibility(flag);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setWindowFlag();

        windowManager = getWindowManager();

        initview();
        initdata();
        initControlPanel();
    }

    private void initview() {
        relativeLayout = findViewById(R.id.surfaceviews);
    }

    private void initdata() {
        try {
            RECODER_TIME = Settings.System.getInt(getContentResolver(), "arc_recoder_time");
        } catch (Settings.SettingNotFoundException e) {
            RECODER_TIME = 300;
        }
        cameraFactory = new CameraFactory(relativeLayout, this);
        cameraFactory.initCameraViews();
        handler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                switch (msg.what) {
                    case UPDATETIME:
                        Integer timecount = (Integer) ((Bundle)msg.obj).get("timecount");
                        if (timecount < 3600){
                            recodertime.setText(String.format(Locale.CHINA, "%02d:%02d",
                                    timecount.intValue() / 60, timecount.intValue() % 60));
                        }else{
                            recodertime.setText(String.format(Locale.CHINA, "%02d:%02d:%02d",
                                    timecount.intValue() / 3600,(timecount.intValue() % 3600) / 60, timecount.intValue() % 60));
                        }
                        break;
                    case STOPRECODER:
                        stopRecoder();
                        break;
                }
            }
        };
    }

    private void initControlPanel(){
        if (cameraFactory.getCameracount() < 1) {
            Log.d(TAG, "initControlPanel: No camera plugged, recoder function is disabled.");
            return;
        }
        if (controlpanel != null) {
            windowManager.removeView(controlpanel);
        }
        controlpanel = LayoutInflater.from(this).inflate(R.layout.control, null);
        recodertime = controlpanel.findViewById(R.id.recodertime);
        controlpanel.findViewById(R.id.alterbut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 暂时保留
            }
        });

        controlpanel.findViewById(R.id.recodfunbut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isRecoder) {
                    // 开始录制
                    startRecoder();
                } else {
                    // 结束录制
                    stopRecoder();
                }
            }
        });

        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.setTitle("control_panel");
        int flag = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH |
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        params.type = WindowManager.LayoutParams.TYPE_PHONE;
        params.flags = flag;
        params.format = PixelFormat.RGBA_8888; /*透明背景,否则会黑色*/
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        params.gravity = Gravity.CENTER;
        windowManager.addView(controlpanel, params);
    }

    private void startRecoder(){
        isRecoder = !isRecoder;
        print("开始录制");
        cameraFactory.startRecoder();
        recodertime.setVisibility(View.VISIBLE);
        updateRecoderTime();
    }

    private void stopRecoder(){
        isRecoder = !isRecoder;
        print("结束录制");
        cameraFactory.stopRecoder();
        recodertime.setVisibility(View.GONE);
        recodertime.setText("00:00");
    }

    private void updateRecoderTime() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: current recoder time is "+RECODER_TIME);
                int count = 0;
                while (isRecoder) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    count++;
                    // 限制最长录制时间
                    if (count > RECODER_TIME){
                        handler.sendEmptyMessage(STOPRECODER);
                        break;
                    }else{
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("timecount", new Integer(count));
                        handler.sendMessage(handler.obtainMessage(UPDATETIME, bundle));
                    }
                }
            }
        }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraFactory.startPreview();
    }

    @Override
    protected void onPause() {
        super.onPause();
        onDestroy();
    }

    @Override
    protected void onDestroy() {
        if (controlpanel != null) {
            windowManager.removeView(controlpanel);
        }
        cameraFactory.stopRecoder();
        cameraFactory.releaseCameras();
        super.onDestroy();
    }

    private void print(String content) {
        Toast.makeText(this, content, Toast.LENGTH_SHORT).show();
    }
}