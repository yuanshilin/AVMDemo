package com.arcvideo.avmdemo;

import android.os.Environment;
import android.util.Size;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CameraFrameUtil {
    public static Object syncobject = new Object();
    public static File FrameRecod;
    public static String getFrameOutTime(){
        return new SimpleDateFormat("mm:ss.SSS").format(new Date());
    }

    public static String getCurrentTime(){
        return new SimpleDateFormat("yyyyMMdd-HH_mm_ss-").format(new Date());
    }

    public static void CreateFrameRecod(){
        FrameRecod = new File(Environment.getExternalStorageDirectory() +
                File.separator+getCurrentTime()+"FrameRecod.txt");
    }

    public static Size getPreviewResolution(boolean HD){
        if (HD) {
            return new Size(1920, 1080);
        } else {
            return new Size(1024, 768);
        }
    }
}
