package com.arcvideo.avmdemo;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class CameraFactory {
    private final String TAG = "ArcvideoCamera_Factory";
    private int cameracount;
    private RelativeLayout relativeLayout;
    private Activity activity;
    private List<View> mSurfaceViewList = new ArrayList<>();
    private List<ArcvideoCamera> arcvideoCameras = new ArrayList<>();
    private CameraManager cameraManager;
    private String[] cameraids;
    private boolean isRecodering = false;

    public CameraFactory(RelativeLayout relativeLayout, Activity activity) {
        this.relativeLayout = relativeLayout;
        this.activity = activity;
        cameraManager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraids = cameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
        cameracount = cameraids.length;
    }

    public void initCameraViews(){
        Log.d(TAG, "initCameraViews: current camera number is " + cameracount);
        if (cameracount > 0) {
            for (int i=0; i<cameracount; i++) {
                View previewLayout = LayoutInflater.from(activity.getApplicationContext()).inflate(R.layout.camerasurface, null);
                previewLayout.setVisibility(View.GONE);
                mSurfaceViewList.add(previewLayout);
            }
        }

        Point sizePoint = new Point();
        // 通过getRealSize可以获取到正确的屏幕分辨率
        activity.getWindow().getWindowManager().getDefaultDisplay().getRealSize(sizePoint);
        int nScreenWidth = sizePoint.x;
        int nScreenHeight = sizePoint.y;
        boolean isLandscape = nScreenWidth > nScreenHeight ? true:false;
        int viewCount = mSurfaceViewList.size();
        Log.e(TAG,"initCameraViews: window width = " + sizePoint.x + ", height = " + sizePoint.y + ", viewCount = " + viewCount);

        if (relativeLayout.getChildCount() == 0){
            for (int i = 0; i < viewCount; i++){
                relativeLayout.addView(mSurfaceViewList.get(i));
            }
        }
        if (viewCount == 1) {
            RelativeLayout.LayoutParams lParams = (RelativeLayout.LayoutParams)mSurfaceViewList.get(0).getLayoutParams();
            lParams.width = nScreenWidth;
            lParams.height = nScreenHeight;
            lParams.topMargin = 0;
            lParams.bottomMargin = 0;
            lParams.leftMargin = 0;
            lParams.rightMargin = 0;
            mSurfaceViewList.get(0).setLayoutParams(lParams);
            mSurfaceViewList.get(0).setVisibility(View.VISIBLE);

        }else if (viewCount == 2) {
            RelativeLayout.LayoutParams lParams0 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(0).getLayoutParams();
            RelativeLayout.LayoutParams lParams1 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(1).getLayoutParams();

            if (isLandscape) {
                lParams0.width = nScreenWidth/2;
                lParams0.height = nScreenHeight;
                lParams0.topMargin = 0;
                lParams0.bottomMargin = 0;
                lParams0.leftMargin = 0;
                lParams0.rightMargin = nScreenWidth/2;


                lParams1.width = nScreenWidth/2;
                lParams1.height = nScreenHeight;
                lParams1.topMargin = 0;
                lParams1.bottomMargin = 0;
                lParams1.leftMargin = nScreenWidth/2;
                lParams1.rightMargin = 0;
            }else{
                lParams0.width = nScreenWidth;
                lParams0.height = nScreenHeight/2;
                lParams0.topMargin = 0;
                lParams0.bottomMargin = nScreenHeight/2;
                lParams0.leftMargin = 0;
                lParams0.rightMargin = 0;


                lParams1.width = nScreenWidth;
                lParams1.height = nScreenHeight/2;
                lParams1.topMargin = nScreenHeight/2;
                lParams1.bottomMargin = 0;
                lParams1.leftMargin = 0;
                lParams1.rightMargin = 0;
            }

            mSurfaceViewList.get(0).setLayoutParams(lParams0);
            mSurfaceViewList.get(0).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(1).setLayoutParams(lParams1);
            mSurfaceViewList.get(1).setVisibility(View.VISIBLE);

        }else if (viewCount == 3) {
            RelativeLayout.LayoutParams lParams0 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(0).getLayoutParams();
            RelativeLayout.LayoutParams lParams1 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(1).getLayoutParams();
            RelativeLayout.LayoutParams lParams2 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(2).getLayoutParams();

            if (isLandscape) {
                lParams0.width = nScreenWidth/3;
                lParams0.height = nScreenHeight;
                lParams0.topMargin = 0;
                lParams0.bottomMargin = 0;
                lParams0.leftMargin = 0;
                lParams0.rightMargin = nScreenWidth/3;

                lParams1.width = nScreenWidth/3;
                lParams1.height = nScreenHeight;
                lParams1.topMargin = 0;
                lParams1.bottomMargin = 0;
                lParams1.leftMargin = nScreenWidth/3;
                lParams1.rightMargin = nScreenWidth/3;

                lParams2.width = nScreenWidth/3;
                lParams2.height = nScreenHeight;
                lParams2.topMargin = 0;
                lParams2.bottomMargin = 0;
                lParams2.leftMargin = nScreenWidth*2/3;
                lParams2.rightMargin = 0;
            }else{
                lParams0.width = nScreenWidth;
                lParams0.height = nScreenHeight/3;
                lParams0.topMargin = 0;
                lParams0.bottomMargin = nScreenHeight*2/3;
                lParams0.leftMargin = 0;
                lParams0.rightMargin = 0;

                lParams1.width = nScreenWidth;
                lParams1.height = nScreenHeight/3;
                lParams1.topMargin = nScreenHeight/3;
                lParams1.bottomMargin = nScreenHeight/3;
                lParams1.leftMargin = 0;
                lParams1.rightMargin = 0;

                lParams2.width = nScreenWidth;
                lParams2.height = nScreenHeight/3;
                lParams2.topMargin = nScreenHeight*2/3;
                lParams2.bottomMargin = 0;
                lParams2.leftMargin = 0;
                lParams2.rightMargin = 0;
            }

            mSurfaceViewList.get(0).setLayoutParams(lParams0);
            mSurfaceViewList.get(0).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(1).setLayoutParams(lParams1);
            mSurfaceViewList.get(1).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(2).setLayoutParams(lParams2);
            mSurfaceViewList.get(2).setVisibility(View.VISIBLE);

        }else if (viewCount == 4) {
            RelativeLayout.LayoutParams lParams0 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(0).getLayoutParams();
            RelativeLayout.LayoutParams lParams1 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(1).getLayoutParams();
            RelativeLayout.LayoutParams lParams2 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(2).getLayoutParams();
            RelativeLayout.LayoutParams lParams3 = (RelativeLayout.LayoutParams)mSurfaceViewList.get(3).getLayoutParams();

            lParams0.width = nScreenWidth/2;
            lParams0.height = nScreenHeight/2;
            lParams0.topMargin = 0;
            lParams0.bottomMargin = nScreenHeight/2;
            lParams0.leftMargin = 0;
            lParams0.rightMargin = nScreenWidth/2;

            lParams1.width = nScreenWidth/2;
            lParams1.height = nScreenHeight/2;
            lParams1.topMargin = 0;
            lParams1.bottomMargin = nScreenHeight/2;
            lParams1.leftMargin = nScreenWidth/2;
            lParams1.rightMargin = 0;

            lParams2.width = nScreenWidth/2;
            lParams2.height = nScreenHeight/2;
            lParams2.topMargin = nScreenHeight/2;
            lParams2.bottomMargin = 0;
            lParams2.leftMargin = 0;
            lParams2.rightMargin = nScreenWidth/2;

            lParams3.width = nScreenWidth/2;
            lParams3.height = nScreenHeight/2;
            lParams3.topMargin = nScreenHeight/2;
            lParams3.bottomMargin = 0;
            lParams3.leftMargin = nScreenWidth/2;
            lParams3.rightMargin = 0;

            mSurfaceViewList.get(0).setLayoutParams(lParams0);
            mSurfaceViewList.get(0).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(1).setLayoutParams(lParams1);
            mSurfaceViewList.get(1).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(2).setLayoutParams(lParams2);
            mSurfaceViewList.get(2).setVisibility(View.VISIBLE);
            mSurfaceViewList.get(3).setLayoutParams(lParams3);
            mSurfaceViewList.get(3).setVisibility(View.VISIBLE);
        }else{
            Log.d(TAG, "initCameraViews: no camera is plugged.");
        }
    }

    public void startPreview(){
        for (int i=0; i<cameracount; i++) {
            View view = mSurfaceViewList.get(i);
            ArcvideoCamera arcvideoCamera = new ArcvideoCamera(i, view);
            arcvideoCamera.setDeviceid(cameraids[i]);
            arcvideoCamera.setResolution(CameraFrameUtil.getPreviewResolution(true));
            arcvideoCameras.add(arcvideoCamera);
        }
    }

    public void releaseCameras(){
        Log.d(TAG, "releaseCameras: release open cameras.");
        for (ArcvideoCamera arcvideoCamera: arcvideoCameras) {
            arcvideoCamera.stopPreview();
        }
    }

    public void startRecoder(){
        if (isRecodering) {
            Log.d(TAG, "startRecoder: camera is already start.");
            return;
        }
        CameraFrameUtil.CreateFrameRecod();
        Log.d(TAG, "startRecoder: start to recoder video. framerecod file is "+CameraFrameUtil.FrameRecod.toString());
        for (ArcvideoCamera arcvideoCamera: arcvideoCameras) {
            arcvideoCamera.startRecoder();
        }
        isRecodering = true;
    }

    public void stopRecoder(){
        if (!isRecodering) {
            Log.d(TAG, "stopRecoder: camera recoder is not start.");
            return;
        }
        Log.d(TAG, "stopRecoder: stop to recoder video.");
        for (ArcvideoCamera arcvideoCamera: arcvideoCameras) {
            arcvideoCamera.stopRecoder();
        }
        isRecodering = false;
    }

    public int getCameracount() {
        return cameracount;
    }
}
