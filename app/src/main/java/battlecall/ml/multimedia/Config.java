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
	public static final int SAMPLE_RATE = 44100;
	public static final int CHANNEL_CONFIG =  AudioFormat.CHANNEL_IN_MONO;
	public static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
	public static final int AUDIO_RESOURCE = MediaRecorder.AudioSource.MIC;
}
