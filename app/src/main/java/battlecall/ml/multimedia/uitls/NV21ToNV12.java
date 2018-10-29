package battlecall.ml.multimedia.uitls;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by BattleCall on 2018/10/29.
 */

public class NV21ToNV12 implements Convert{
	private int framesize;

	public NV21ToNV12(int framesize){
		this.framesize = framesize;
	}
	@Override
	public void convert(String inFileName, String outFileName) {
		FileInputStream fileInputStream = null;
		FileOutputStream fileOutputStream = null;
		try {
			fileInputStream = new FileInputStream(inFileName);
			fileOutputStream = new FileOutputStream(outFileName);
			byte[] buffer = new byte[framesize/4*6];
			byte[] dstBuffer = new byte[framesize/4*6];
			while (-1 != (fileInputStream.read(buffer))){
				System.arraycopy(buffer,0,dstBuffer,0,framesize);

				int i = 0, j = 0;
				for (j = 0; j < framesize / 2; j += 2) {//U
					dstBuffer[framesize + j - 1] = buffer[j + framesize];
				}
				for (j = 0; j < framesize / 2; j += 2) {//V
					dstBuffer[framesize + j] = buffer[j + framesize - 1];
				}

				fileOutputStream.write(dstBuffer);
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally {
			try {
				if (null != fileInputStream){
					fileInputStream.close();
				}
				if (null != fileOutputStream){
					fileOutputStream.close();
				}
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
