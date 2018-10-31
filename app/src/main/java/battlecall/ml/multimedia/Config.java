package battlecall.ml.multimedia;

import android.media.AudioFormat;
import android.media.MediaRecorder;

/**
 * Created by BattleCall on 2018/10/17.
 */

public class Config {
	public static final String IMAGE_PATH = "/sdcard/name.jpg";
	public static final String RECORD_PATH = "/sdcard/record.pcm";
	public static final String CONVERT_PATH = "/sdcard/record.wav";
	public static final String MP4_PATH = "/sdcard/korea.mp4";

	public static final String CAMERA_OUTPUT_PATH_NV12 = "/sdcard/camera_nv12.video";
	public static final String CAMERA_OUTPUT_PATH_NV21 = "/sdcard/camera_nv21.video";
	public static final String CAMERA_OUTPUT_PATH_I420 = "/sdcard/camera_i420.video";
	public static final String CAMERA_OUTPUT_PATH_YV12 = "/sdcard/camera_yv12.video";
	public static final String CAMERA_OUTPUT_PATH_MP4 = "/sdcard/camera_mp4.video";



	public static final int SAMPLE_RATE = 44100;
	public static final int CHANNEL_CONFIG =  AudioFormat.CHANNEL_IN_MONO;
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.MIC;
}
