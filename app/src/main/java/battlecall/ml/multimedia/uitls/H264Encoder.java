package battlecall.ml.multimedia.uitls;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by BattleCall on 2018/10/30.
 */

public class H264Encoder implements Encoder {
	private final static int TIMEOUT_USEC = 12000;
	private final static String H264 = "video/avc";
	private final static int ARRAY_SIZE = 10;

	private MediaCodec mediaCodec;
	private boolean isRuning = false;
	private int width,height,framerate;
	private String path;
	private BufferedOutputStream outputStream;

	public ArrayBlockingQueue<byte[]> yuv420Queue = new ArrayBlockingQueue<byte[]>(ARRAY_SIZE);
	public byte[] configbyte;

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public H264Encoder(int width, int height, int framerate,String path) {
		Log.d("cjl", "H264Encoder ---------H264Encoder:      Constructor");
		this.width = width;
		this.height = height;
		this.framerate = framerate;
		this.path = path;

		MediaFormat mediaFormat = MediaFormat.createVideoFormat(H264,width,height);
		mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
		mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
		mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, 30);
		mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1);

		try {
			mediaCodec = MediaCodec.createEncoderByType(H264);
			mediaCodec.configure(mediaFormat,null,null,MediaCodec.CONFIGURE_FLAG_ENCODE);
			mediaCodec.start();
			createfile(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createfile(String path) {
		Log.d("cjl", "H264Encoder ---------createfile:      ");
		File file = new File(path);
		if (null != file && file.exists()){
			file.delete();
		}
		try {
			outputStream = new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
		if (nv21 == null || nv12 == null) return;
		int framesize = width * height;
		int i = 0, j = 0;
		System.arraycopy(nv21, 0, nv12, 0, framesize);
		for (i = 0; i < framesize; i++) {
			nv12[i] = nv21[i];
		}
		for (j = 0; j < framesize / 2; j += 2) {
			nv12[framesize + j - 1] = nv21[j + framesize];
		}
		for (j = 0; j < framesize / 2; j += 2) {
			nv12[framesize + j] = nv21[j + framesize - 1];
		}
	}

	@Override
	public void startEncoder() {
		Log.d("cjl", "H264Encoder ---------startEncoder:      ");
		new Thread(new Runnable() {

			@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
			@Override
			public void run() {
				isRuning = true;
				byte[] input = null;
				long pts = 0;
				long generateIndex = 0;

				while (isRuning) {
					if (yuv420Queue.size() > 0) {
						input = yuv420Queue.poll();
						byte[] yuv420sp = new byte[width * height * 3 / 2];
						// 必须要转格式，否则录制的内容播放出来为绿屏
						NV21ToNV12(input, yuv420sp, width, height);
						input = yuv420sp;
					}
					if (input != null) {
						Log.d("cjl", "H264Encoder ---------run:      encoding "+yuv420Queue.size()+ " input "+input[0]);

						try {
							ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
							ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
							int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
							if (inputBufferIndex >= 0) {
								Log.d("cjl", "H264Encoder ---------run:      >= 0");
								pts = computePresentationTime(generateIndex);
								ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
								inputBuffer.clear();
								inputBuffer.put(input);
								mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, System.currentTimeMillis(), 0);
								generateIndex += 1;
							}

							MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
							int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							while (outputBufferIndex >= 0) {
								Log.d("cjl", "H264Encoder ---------run:      out ");
								ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
								byte[] outData = new byte[bufferInfo.size];
								outputBuffer.get(outData);
								if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_CODEC_CONFIG) {
									configbyte = new byte[bufferInfo.size];
									configbyte = outData;
								} else if (bufferInfo.flags == MediaCodec.BUFFER_FLAG_SYNC_FRAME) {
									byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
									System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
									System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
									outputStream.write(keyframe, 0, keyframe.length);
								} else {
									outputStream.write(outData, 0, outData.length);
								}

								mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
								outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					} else {
						Log.d("cjl", "H264Encoder ---------run:      sleep");
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}

				// 停止编解码器并释放资源
				try {
					mediaCodec.stop();
					mediaCodec.release();
				} catch (Exception e) {
					e.printStackTrace();
				}

				// 关闭数据流
				try {
					outputStream.flush();
					outputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	@Override
	public void stopEncoder() {
		Log.d("cjl", "H264Encoder ---------stopEncoder:      ");
		isRuning = false;
	}

	/**
	 * 根据帧数生成时间戳
	 */
	private long computePresentationTime(long frameIndex) {
		return 132 + frameIndex * 1000000 / framerate;
	}

	@Override
	public void putData(byte[] buffer) {
		if (yuv420Queue.size() >= ARRAY_SIZE) {
			Log.d("cjl", "H264Encoder ---------putData:      poll ");
			yuv420Queue.poll();
		}
		yuv420Queue.add(buffer);
	}

	public boolean outMaxSize(){
		return yuv420Queue.size() >= ARRAY_SIZE ? true : false;
	}

}
