package battlecall.ml.multimedia;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by BattleCall on 2018/10/16.
 */

public class CustomView extends View{
	private Paint paint;
	private final String path = "/sdcard/name.jpg";
	private Bitmap bitmap2;

	public CustomView(Context context) {
		super(context);
		init();
	}

	public CustomView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init();
	}

	private void init() {
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.STROKE);

		bitmap2 = BitmapFactory.decodeFile(path);
	}

	public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public CustomView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	@Override
	protected void onDraw(Canvas canvas) {

		if (bitmap2 != null){
			canvas.drawBitmap(bitmap2,0,0,paint);
		}
	}
}
