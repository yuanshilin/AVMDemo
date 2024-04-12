package com.arcvideo.avmdemo;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaRecorder;
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
    private MediaRecorder mediaRecorder;
    private FileOutputStream fileOutputStream;
    private SurfaceView surfaceView;
    private TextView cameraIDtext;
    private boolean isRecoder = false;
    private int width, height;

    public ArcvideoCamera(int cameraid, View Cameraview) {
        this.cameraid = cameraid;
        surfaceView = Cameraview.findViewById(R.id.surfacearea);
        cameraIDtext = Cameraview.findViewById(R.id.cameraid);
        surfaceView.getHolder().addCallback(this);
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

    private void initMediaRecord() {
        mediaRecorder = new MediaRecorder();// 创建mediarecorder对象
        camera.unlock();
        mediaRecorder.setCamera(camera);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);//设置音频输入源，也可以使用 MediaRecorder.AudioSource.MIC
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);//设置视频输入源
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);//音频输出格式,TS格式可解决设备突然断电后视频文件不能保存的问题
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);//设置音频的编码格式
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);//设置图像编码格式
        mediaRecorder.setVideoEncodingBitRate(10 * width * height);//设置比特率,比特率是每一帧所含的字节流数量,比特率越大每帧字节越大,画面就越清晰,算法一般是 5 * 选择分辨率宽 * 选择分辨率高,一般可以调整5-10,比特率过大也会报错
        mediaRecorder.setOrientationHint(0);//设置视频的摄像头角度 只会改变录制的视频文件的角度(对预览图像角度没有效果)
        mediaRecorder.setPreviewDisplay(surfaceView.getHolder().getSurface()); // 设置视频文件输出的路径
        String path = Environment.getExternalStorageDirectory() + File.separator+ getDeviceid()+".mp4";
        Log.d(TAG, "initMediaRecord: camera "+ getDeviceid()+" save recoding file is "+path);
        mediaRecorder.setOutputFile(path);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            mediaRecorder.stop();
            mediaRecorder.release();
        }
        camera.stopPreview();
        camera.release();
    }

    public void startRecoder(){
        isRecoder =! isRecoder;
        initMediaRecord();
        mediaRecorder.start();
        try {
            fileOutputStream = new FileOutputStream(CameraFrameUtil.FrameRecod, true);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void stopRecoder(){
        isRecoder =! isRecoder;
        mediaRecorder.stop();
        mediaRecorder.reset();
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
            String content = getDeviceid()+"——"+CameraFrameUtil.getFrameOutTime()+"\n";
            Log.d(TAG, "onPreviewFrame: yuanshilin content is "+content);
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
