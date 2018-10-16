package battlecall.ml.multimedia;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.ImageView;

import java.io.File;

public class MainActivity extends AppCompatActivity {
	private ImageView imageView;
	private SurfaceView surfaceView;
	private CustomView customView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		imageView = findViewById(R.id.iv);
		surfaceView = findViewById(R.id.sv);

//		Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.name);

		final String path = "/sdcard/name.jpg";
		final Bitmap bitmap = BitmapFactory.decodeFile(path);

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
				Bitmap bitmap2 = BitmapFactory.decodeFile(path);
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
}
