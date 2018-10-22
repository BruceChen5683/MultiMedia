package battlecall.ml.multimedia;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioAttributes;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import battlecall.ml.multimedia.uitls.Convert;
import battlecall.ml.multimedia.uitls.PcmToWavUtil;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
public class MainActivity extends AppCompatActivity implements View.OnClickListener{
	private ImageView imageView;
	private SurfaceView surfaceView,surfaceViewCamera;
	private TextureView textureView;
	private CustomView customView;
	private Button btnRecord,btnPlay,btnConvert,btnAction,btnExtract;

	private AudioRecord audioRecord = null;
	private int recordBufSize = 0;
	private boolean isRecording = false;

	private AudioTrack audioPlayer;
	private byte[] audioData;

	private Camera camera;

	private MediaExtractor mediaExtractor = new MediaExtractor();
	private int videoTrackIndex = -1,audioTrackIndex = -1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = findViewById(R.id.iv);
		surfaceView = findViewById(R.id.sv);
		surfaceViewCamera = findViewById(R.id.camera1);

		btnRecord = findViewById(R.id.record);
		btnPlay = findViewById(R.id.play);
		btnConvert = findViewById(R.id.convert);
		btnAction = findViewById(R.id.action);
		btnExtract = findViewById(R.id.extract);

		textureView = findViewById(R.id.camera2);

		btnConvert.setOnClickListener(this);
		btnPlay.setOnClickListener(this);
		btnRecord.setOnClickListener(this);
		btnAction.setOnClickListener(this);
		btnExtract.setOnClickListener(this);

		textureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
			@Override
			public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
				Log.d("cjl", "MainActivity ---------onSurfaceTextureAvailable:      ");
			}

			@Override
			public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
				Log.d("cjl", "MainActivity ---------onSurfaceTextureSizeChanged:      ");
			}

			@Override
			public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
				Log.d("cjl", "MainActivity ---------onSurfaceTextureDestroyed:      ");
				return false;
			}

			@Override
			public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
				Log.d("cjl", "MainActivity ---------onSurfaceTextureUpdated:      ");

			}
		});

//		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.name);



		final Bitmap bitmap = BitmapFactory.decodeFile(Config.IMAGE_PATH);

		imageView.setImageBitmap(bitmap);

		surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder surfaceHolder) {
				if (surfaceHolder == null){
					return;
				}

				Paint paint = new Paint();
				paint.setAntiAlias(true);
				paint.setStyle(Paint.Style.STROKE);

				Canvas canvas = surfaceHolder.lockCanvas();
				Bitmap bitmap2 = BitmapFactory.decodeFile(Config.IMAGE_PATH);
				int width = bitmap2.getWidth();
				int height = bitmap2.getHeight();

				Matrix matrix = new Matrix();
				matrix.postScale((float) surfaceView.getWidth()/width/2,(float) surfaceView.getHeight()/height/2);

				canvas.drawBitmap(Bitmap.createBitmap(bitmap2,0,0,width,height,matrix,true),
						surfaceView.getWidth()/2,
						surfaceView.getHeight()/2,
						paint);
				surfaceHolder.unlockCanvasAndPost(canvas);

			}

			@Override
			public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

			}

			@Override
			public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

			}
		});

		surfaceViewCamera.getHolder().addCallback(new SurfaceHolder.Callback() {
			@Override
			public void surfaceCreated(SurfaceHolder surfaceHolder) {
				Log.d("cjl", "MainActivity ---------surfaceCreated:      ");
			}

			@Override
			public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
				Log.d("cjl", "MainActivity ---------surfaceChanged:      ");
			}

			@Override
			public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
				Log.d("cjl", "MainActivity ---------surfaceDestroyed:      ");
			}
		});

	}

	private void startRecord() {
		new Thread(new Runnable() {
			@Override
			public void run() {

					createAudioRecord();

					byte data[] = new byte[recordBufSize];
					audioRecord.startRecording();
					isRecording = true;

					FileOutputStream fos = null;
					int alreadyRead = 0;
					try {

						checkFile();

						fos = new FileOutputStream(Config.RECORD_PATH);
						if (null != fos){
							while (isRecording){
								alreadyRead = audioRecord.read(data,0,recordBufSize);
								if (AudioRecord.ERROR_INVALID_OPERATION != alreadyRead){
									fos.write(data);
								}
							}
						}
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}finally {
						if (null != fos){
							try {
								fos.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					}
			}


		}).start();
	}

	private void checkFile() {
		File file = new File(Config.RECORD_PATH);
		if (null != file && file.exists()){
			file.delete();
		}
	}

	private void releaseAudio() {
		isRecording = false;
		if (null != audioRecord){
			audioRecord.stop();
			audioRecord.release();
			audioRecord = null;
		}
	}

	private void createAudioRecord() {
		recordBufSize = AudioRecord.getMinBufferSize(Config.SAMPLE_RATE, Config.CHANNEL_CONFIG,Config.AUDIO_FORMAT);
		Log.d("cjl", "MainActivity ---------createAudioRecord:     recordBufSize "+recordBufSize);
		audioRecord = new AudioRecord(Config.AUDIO_RESOURCE,Config.SAMPLE_RATE, Config.CHANNEL_CONFIG,Config.AUDIO_FORMAT,recordBufSize);
	}

	@Override
	public void onClick(View view) {
		switch (view.getId()){
			case R.id.record:
				if (btnRecord.getText().toString().equals(getString(R.string.stop_record))){
					btnRecord.setText(R.string.start_record);
					releaseAudio();
				}else {
					btnRecord.setText(R.string.stop_record);
					startRecord();
				}
				break;
			case R.id.play:
				if (btnPlay.getText().toString().equals(getString(R.string.play))){
					btnPlay.setText(R.string.stop);
					startPlay();
				}else {
					btnPlay.setText(R.string.play);
					stopPlay();
				}
				break;
			case R.id.convert:
				Convert convert = new PcmToWavUtil(Config.AUDIO_FORMAT,Config.SAMPLE_RATE,Config.CHANNEL_CONFIG);
				convert.convert(Config.RECORD_PATH,Config.CONVERT_PATH);
				break;
			case R.id.action:
				Log.d("cjl", "MainActivity ---------onClick:      Action .....");

				openCamera();
				changePreView();
				break;
			case R.id.extract:
				extractVideoAudio();
				break;
			default:
				break;
		}
	}

	private void extractVideoAudio() {
		Log.d("cjl", "MainActivity ---------extractVideoAudio:      ");
		FileOutputStream videoOps = null,audioOps = null;
		try {
			videoOps = new FileOutputStream("/sdcard/video.test");
			audioOps = new FileOutputStream("/sdcard/audio.test");
			mediaExtractor.setDataSource(Config.MP4_PATH);

			int numTracks = mediaExtractor.getTrackCount();
			Log.d("cjl", "MainActivity ---------extractVideoAudio:      numTracks "+numTracks);
			for (int i = 0;i < numTracks;++i){
				MediaFormat format = mediaExtractor.getTrackFormat(i);
				String mineType = format.getString(MediaFormat.KEY_MIME);
//				mediaExtractor.selectTrack(i);

				Log.d("cjl", "MainActivity ---------extractVideoAudio:     mineType  "+mineType);
				//视频信道
				if (mineType.startsWith("video/")) {
					videoTrackIndex = i;
				}
				//音频信道
				if (mineType.startsWith("audio/")) {
					audioTrackIndex = i;
				}
			}

			ByteBuffer bytebuffer = ByteBuffer.allocate(1024*500);
			Log.d("cjl", "MainActivity ---------extractVideoAudio:   videoTrackIndex   "+videoTrackIndex);
			Log.d("cjl", "MainActivity ---------extractVideoAudio:    audioTrackIndex  "+audioTrackIndex);
			mediaExtractor.selectTrack(videoTrackIndex);
			int readCount;
			Log.d("cjl", "MainActivity ---------extractVideoAudio:      start video extract");
			while ((readCount = mediaExtractor.readSampleData(bytebuffer,0)) >= 0){
//				Log.d("cjl", "MainActivity ---------extractVideoAudio:      extract video ing");
				byte[] buffer = new byte[readCount];
				bytebuffer.get(buffer);
				videoOps.write(buffer);
				bytebuffer.clear();
				mediaExtractor.advance();
			}
			mediaExtractor.selectTrack(audioTrackIndex);
			Log.d("cjl", "MainActivity ---------extractVideoAudio:      start audio extract");
			while ((readCount = mediaExtractor.readSampleData(bytebuffer,0)) >= 0){
//				Log.d("cjl", "MainActivity ---------extractVideoAudio:      extract audio ing");

				byte[] buffer = new byte[readCount];
				bytebuffer.get(buffer);
				audioOps.write(buffer);
				bytebuffer.clear();
				mediaExtractor.advance();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			mediaExtractor.release();
			try {
				audioOps.close();
				videoOps.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void changePreView() {
		try {
			if (btnAction.getText().toString().equals(getString(R.string.preview1))){
				btnAction.setText(R.string.preview2);
				camera.setPreviewDisplay(surfaceViewCamera.getHolder());
			}else {
				btnAction.setText(R.string.preview1);
				camera.setPreviewTexture(textureView.getSurfaceTexture());
			}
			camera.startPreview();

			Camera.Parameters parameters = camera.getParameters();
			parameters.setPreviewFormat(ImageFormat.NV21);
			camera.setParameters(parameters);

			camera.setPreviewCallback(new Camera.PreviewCallback() {
				@Override
				public void onPreviewFrame(byte[] bytes, Camera camera) {
					Log.d("cjl", "MainActivity ---------onPreviewFrame:      "+bytes.length);
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void openCamera() {
		if(camera != null){
			camera.release();
		}
		camera = Camera.open();
		camera.setDisplayOrientation(90);
	}

	private void stopPlay() {
		if (null != audioPlayer){
			Log.d("cjl", "MainActivity ---------stopPlay:      stopping");
			audioPlayer.stop();
			audioPlayer.release();
			Log.d("cjl", "MainActivity ---------stopPlay:    null  ");
		}
	}

	private void startPlay() {
		new GetDataTask().execute();
	}


	private class GetDataTask extends AsyncTask{
		//MODE_STREAM
		@Override
		protected Object doInBackground(Object[] objects) {
			Log.d("cjl", "GetDataTask ---------doInBackground:     MODE_STREAM ");
			try {
				InputStream in = new FileInputStream(Config.CONVERT_PATH);
				DataInputStream dis = new DataInputStream(in);

				final int minBufferSize = AudioTrack.getMinBufferSize(Config.SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, Config.AUDIO_FORMAT);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					Log.d("cjl", "GetDataTask ---------doInBackground:      >=");
					audioPlayer = new AudioTrack(
							new AudioAttributes.Builder()
									.setUsage(AudioAttributes.USAGE_MEDIA)
									.setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
									.build(),
							new AudioFormat.Builder().setSampleRate(Config.SAMPLE_RATE)
									.setEncoding(Config.AUDIO_FORMAT)
									.setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
									.build(),
							minBufferSize,
							AudioTrack.MODE_STREAM,
							AudioManager.AUDIO_SESSION_ID_GENERATE);
				}else{
					audioPlayer = new AudioTrack(
						AudioManager.STREAM_MUSIC,
						Config.SAMPLE_RATE,
						AudioFormat.CHANNEL_OUT_MONO,
						Config.AUDIO_FORMAT,
							minBufferSize,
						AudioTrack.MODE_STREAM);
					Log.d("cjl", "GetDataTask ---------doInBackground:      os version to low");
				}

				byte[] tempBuffer = new byte[1024];
				int readCount = 0;
				while (dis.available() > 0){
					readCount = dis.read(tempBuffer);
					if (readCount == AudioTrack.ERROR_INVALID_OPERATION || readCount == AudioTrack.ERROR_BAD_VALUE){
						continue;
					}

					if (readCount != 0 && readCount != AudioTrack.ERROR ){
						audioPlayer.play();
						audioPlayer.write(tempBuffer,0,readCount);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

		// MODE_STATIC
		//		@Override
//		protected Object doInBackground(Object[] objects) {
//			Log.d("cjl", "GetDataTask ---------doInBackground:      start write to out");
//			try {
//				InputStream in = new FileInputStream(Config.CONVERT_PATH);
//
//
//				ByteArrayOutputStream out = new ByteArrayOutputStream();
//				//读取数据耗时8秒
////				10-17 20:26:05.708 11884-12284/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      start write to out
////				10-17 20:26:13.708 11884-12284/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      out write end
////				for (int bData;(bData = in.read()) != -1;){
////					out.write(bData);
////				}
//
////				10-17 20:30:10.908 16192-17230/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      start write to out
////				10-17 20:30:10.918 16192-17230/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      out write end
//				//读取数据耗时10毫秒
//				byte[] buffer = new byte[1024];
//				while ( in.read(buffer) != -1){
//					out.write(buffer);
//				}
//
//
//				Log.d("cjl", "GetDataTask ---------doInBackground:      out write end");
//				audioData = out.toByteArray();
//
//				audioPlayer = new AudioTrack(
//						AudioManager.STREAM_MUSIC,
//						Config.SAMPLE_RATE,
//						AudioFormat.CHANNEL_OUT_MONO,
//						Config.AUDIO_FORMAT,
//						audioData.length,
//						AudioTrack.MODE_STATIC);
//
//				Log.d("cjl", "GetDataTask ---------doInBackground:      writing data ...");
//				audioPlayer.write(audioData,0,audioData.length);
//				Log.d("cjl", "GetDataTask ---------doInBackground:      play start");
//				audioPlayer.play();
//				Log.d("cjl", "GetDataTask ---------doInBackground:      playing");
//
//
//			} catch (FileNotFoundException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//			return null;
//		}
	}

}
