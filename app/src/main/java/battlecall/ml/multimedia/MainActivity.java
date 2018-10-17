package battlecall.ml.multimedia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

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

	private AudioTrack audioTrack;


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
	}

	private void startPlay() {
	}
}
