package battlecall.ml.multimedia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import battlecall.ml.multimedia.uitls.Convert;
import battlecall.ml.multimedia.uitls.PcmToWavUtil;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
	private ImageView imageView;
	private SurfaceView surfaceView;
	private CustomView customView;
	private Button btnRecord,btnPlay;

	private AudioRecord audioRecord = null;
	private int recordBufSize = 0;
	private boolean isRecording = false;

	private AudioTrack audioPlayer;
	private byte[] audioData;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = findViewById(R.id.iv);
		surfaceView = findViewById(R.id.sv);
		btnRecord = findViewById(R.id.record);
		btnPlay = findViewById(R.id.play);

		findViewById(R.id.convert).setOnClickListener(this);

		btnPlay.setOnClickListener(this);
		btnRecord.setOnClickListener(this);
		
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
			default:
				break;
		}
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

		@Override
		protected Object doInBackground(Object[] objects) {
			Log.d("cjl", "GetDataTask ---------doInBackground:      start write to out");
			try {
				InputStream in = new FileInputStream(Config.CONVERT_PATH);


				ByteArrayOutputStream out = new ByteArrayOutputStream();
				//读取数据耗时8秒
//				10-17 20:26:05.708 11884-12284/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      start write to out
//				10-17 20:26:13.708 11884-12284/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      out write end
//				for (int bData;(bData = in.read()) != -1;){
//					out.write(bData);
//				}

//				10-17 20:30:10.908 16192-17230/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      start write to out
//				10-17 20:30:10.918 16192-17230/battlecall.ml.multimedia D/cjl: GetDataTask ---------doInBackground:      out write end
				//读取数据耗时10毫秒
				byte[] buffer = new byte[1024];
				while ( in.read(buffer) != -1){
					out.write(buffer);
				}


				Log.d("cjl", "GetDataTask ---------doInBackground:      out write end");
				audioData = out.toByteArray();

				audioPlayer = new AudioTrack(
						AudioManager.STREAM_MUSIC,
						Config.SAMPLE_RATE,
						AudioFormat.CHANNEL_OUT_MONO,
						Config.AUDIO_FORMAT,
						audioData.length,
						AudioTrack.MODE_STATIC);

				Log.d("cjl", "GetDataTask ---------doInBackground:      writing data ...");
				audioPlayer.write(audioData,0,audioData.length);
				Log.d("cjl", "GetDataTask ---------doInBackground:      play start");
				audioPlayer.play();
				Log.d("cjl", "GetDataTask ---------doInBackground:      playing");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}
