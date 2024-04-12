package com.arcvideo.avmdemo;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArcvideoCamera implements Camera.PreviewCallback, SurfaceHolder.Callback {
    private final String TAG = "ArcvideoCamera";
    private int cameraid;
    private String deviceid;
    private Camera camera;
    private FileOutputStream fileOutputStream;
    private SurfaceView surfaceView;
    private TextView cameraIDtext;
    private boolean isRecoder = false;
    private int width, height;
    private VideoEncoder avcEncoder;

    public ArcvideoCamera(int cameraid, View Cameraview) {
        this.cameraid = cameraid;
        surfaceView = Cameraview.findViewById(R.id.surfacearea);
        cameraIDtext = Cameraview.findViewById(R.id.cameraid);
        surfaceView.getHolder().addCallback(this);
        avcEncoder = new VideoEncoder();
    }

    public void setResolution(Size size){
        this.width = size.getWidth();
        this.height = size.getHeight();
    }

    private void initConfig(){
        camera = Camera.open(cameraid);
        if (camera != null) {
            Log.d(TAG, "initCamera: camera:"+ cameraid +" initial is success.");
        } else {
            Log.d(TAG, "initCamera: camera:"+ cameraid +" initial is failed.");
            return;
        }
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(width, height);
        parameters.setPreviewFormat(ImageFormat.NV21);
        camera.setParameters(parameters);
        camera.setPreviewCallback(this);
    }

    private void startPreview(SurfaceHolder surfaceHolder){
        try {
            Log.d(TAG, "startPreview: camera "+getDeviceid() +" work resolution is "+width+"x"+height);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopPreview(){
        if (isRecoder) {
            avcEncoder.stopEncode();
        }
        camera.stopPreview();
        camera.release();
    }

    public void startRecoder(){
        isRecoder =! isRecoder;
        avcEncoder.setVideoOptions(width, height, 10000000,30,
                Environment.getExternalStorageDirectory() + File.separator+ getDeviceid()+".mp4");
        avcEncoder.startEncode();
        try {
            fileOutputStream = new FileOutputStream(CameraFrameUtil.FrameRecod, true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopRecoder(){
        isRecoder =! isRecoder;
        avcEncoder.stopEncode();
        try {
            fileOutputStream.flush();
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        if (isRecoder){
            // 视频编码
            avcEncoder.fireVideo(bytes);

            //帧生成日志
            String content = getDeviceid()+"——"+CameraFrameUtil.getFrameOutTime()+"\n";
            synchronized (CameraFrameUtil.syncobject) {
                try {
                    fileOutputStream.write(content.getBytes());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder surfaceHolder) {
        Log.d(TAG, "surfaceCreated: ");
        initConfig();
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.d(TAG, "surfaceChanged: ");
        new Thread(new Runnable() {
            @Override
            public void run() {
                startPreview(surfaceHolder);
            }
        }).start();
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder surfaceHolder) {

    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid;
        cameraIDtext.setText(deviceid);
    }
}
