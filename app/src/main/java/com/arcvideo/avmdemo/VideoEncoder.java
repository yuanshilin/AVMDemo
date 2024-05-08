package com.arcvideo.avmdemo;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.text.TextUtils;

import java.nio.ByteBuffer;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class VideoEncoder {
	private final static String MINE_TYPE = "video/avc";

	public static int BitRate = 10000000;
	public static int FPS = 30;

	private MediaCodec mediaCodec = null;
	private int mWidth;
	private int mHeight;
	private MediaMuxer mMuxer = null;
	private int frameIndex = 0;
	private int mTrackIndex;
	private long startTime;
	public VideoEncoder() {
//		initialize();
	}

//	protected void initialize() {
//		for (int i = 0; i < MediaCodecList.getCodecCount(); i++) {
//			MediaCodecInfo mediaCodecInfo = MediaCodecList.getCodecInfoAt(i);
//			for (String type : mediaCodecInfo.getSupportedTypes()) {
//				if (TextUtils.equals(type, MINE_TYPE)
//						&& mediaCodecInfo.isEncoder()) {
//					CodecCapabilities codecCapabilities = mediaCodecInfo
//							.getCapabilitiesForType(MINE_TYPE);
//					for (int format : codecCapabilities.colorFormats) {
//						if (format == CodecCapabilities.COLOR_FormatYUV420SemiPlanar) {
//							return;
//						}
//					}
//				}
//			}
//		}
//	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	public void setVideoOptions(int width, int height, int bit, int fps, String outputPath) {
		mWidth = width;
		mHeight = height;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			try {
				mediaCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC);
				MediaFormat mediaFormat = MediaFormat.createVideoFormat(
						MINE_TYPE, width, height);

				mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bit);
				mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, fps);
				mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1); // 关键帧间隔时间
																				// 单位s
				mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
				// mediaFormat.setInteger(MediaFormat.KEY_PROFILE,
				// CodecProfileLevel.AVCProfileBaseline);
				// mediaFormat.setInteger(MediaFormat.KEY_LEVEL,
				// CodecProfileLevel.AVCLevel52);
				mediaCodec.configure(mediaFormat, null, null,
						MediaCodec.CONFIGURE_FLAG_ENCODE);
				mMuxer = new MediaMuxer(outputPath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);
			} catch (Exception e) {
				System.out.println(e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void fireVideo(byte[] data) {
		// 拿到有空闲的输入缓存区下标
		int inputBufferId = mediaCodec.dequeueInputBuffer(-1);
		if (inputBufferId >= 0) {
			// 从相机预览帧缓冲队列中取出一帧待处理的数据，需要NV21->NV12的转换
			byte[] tempByte = nv212nv12(data);
			//有效的空的缓存区
			ByteBuffer inputBuffer = mediaCodec.getInputBuffer(inputBufferId);
			if (tempByte == null) {
				return;
			}
			inputBuffer.put(tempByte);
			frameIndex++;
			// 微秒时间戳
//			long presentationTime = frameIndex * 40 * 1000;

//			获取当前系统的时间 System.nanoTime()（这个是纳秒时间），然后减去开始时间后再除以 1000就可以得到编码时间戳
			long presentationTime = (System.nanoTime() - startTime) / 1000;
			//将数据放到编码队列
			mediaCodec.queueInputBuffer(inputBufferId, 0, tempByte.length, presentationTime, 0);
		}
		MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
		//得到成功编码后输出的out buffer Id
		int outputBufferId = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
		if (outputBufferId >= 0) {
			ByteBuffer outputBuffer = mediaCodec.getOutputBuffer(outputBufferId);
			// mediacodec的直接编码输出是h264
			byte[] h264= new byte[bufferInfo.size];
			outputBuffer.get(h264);
			outputBuffer.position(bufferInfo.offset);
			outputBuffer.limit(bufferInfo.offset + bufferInfo.size);
			// 将编码后的数据写入到MP4复用器
			mMuxer.writeSampleData(mTrackIndex, outputBuffer, bufferInfo);
			//释放output buffer
			mediaCodec.releaseOutputBuffer(outputBufferId, false);
		} else if (outputBufferId == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
			MediaFormat mediaFormat = mediaCodec.getOutputFormat();
			mTrackIndex = mMuxer.addTrack(mediaFormat);
			mMuxer.start();
		}
	}

	private byte[] nv212nv12(byte[] data) {
		int len = mWidth * mHeight;
		byte[] buffer = new byte[len * 3 / 2];
		byte[] y = new byte[len];
		byte[] uv = new byte[len / 2];
		System.arraycopy(data, 0, y, 0, len);
		for (int i = 0; i < len / 4; i++) {
			uv[i * 2] = data[len + i * 2 + 1];
			uv[i * 2 + 1] = data[len + i * 2];
		}
		System.arraycopy(y, 0, buffer, 0, y.length);
		System.arraycopy(uv, 0, buffer, y.length, uv.length);
		return buffer;
	}

	public void startEncode(){
		if (mediaCodec != null) {
			mediaCodec.start();
		}
	}

	public void stopEncode(){
		if (null != mediaCodec) {
			mediaCodec.stop();
			mediaCodec.release();
			mediaCodec = null;
		}

		if (-1 != mTrackIndex && mMuxer != null) {
			mMuxer.stop();
			mMuxer.release();
			mMuxer = null;
		}
	}

//	标记开始时间
	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}
}
