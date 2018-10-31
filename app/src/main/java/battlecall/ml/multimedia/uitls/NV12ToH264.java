package battlecall.ml.multimedia.uitls;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by BattleCall on 2018/10/26.
 */

public class NV12ToH264 implements Convert{

	private int width,height,framerate;

	public NV12ToH264(int width, int height, int framerate) {
		this.width = width;
		this.height = height;
		this.framerate = framerate;
	}

	@Override
	public void convert(String inFileName, String outFileName) {
		Encoder encoder = new H264Encoder(width,height,framerate,outFileName);

		FileInputStream fips = null;
		encoder.startEncoder();

		int i = 0;
		byte[] buffers = new byte[width*height/4*6];
		try {
			fips = new FileInputStream(inFileName);

			while ((-1 != fips.read(buffers))){

//				Thread.sleep(500);
				encoder.putData(buffers);
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != fips){
				try {
					fips.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
