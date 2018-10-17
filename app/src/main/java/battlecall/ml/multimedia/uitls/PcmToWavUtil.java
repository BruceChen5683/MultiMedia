package battlecall.ml.multimedia.uitls;

import android.media.AudioFormat;
import android.media.AudioRecord;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by BattleCall on 2018/10/17.
 *
 *from  https://blog.csdn.net/u011520181/article/details/59482762
 * http://www.cnblogs.com/renhui/p/7457321.html
 *
 * http://www.cnblogs.com/cheney23reg/archive/2010/08/08/1795067.html
 */

public class PcmToWavUtil implements Convert{
	/**
	 * 缓存的音频大小
	 */
	private int mBufferSize;
	/**
	 * 采样率
	 */
	private int mSampleRate;
	/**
	 * 声道数
	 */
	private int mChannel;

	public PcmToWavUtil(int encoding, int mSampleRate, int mChannel) {
		this.mSampleRate = mSampleRate;
		this.mChannel = mChannel;
		this.mBufferSize = AudioRecord.getMinBufferSize(mSampleRate,mChannel,encoding);
	}

	/**
	 * 加入wav文件头
	 */
	private void writeWaveFileHeader(FileOutputStream out, long totalAudioLen,
									 long totalDataLen, long longSampleRate, int channels, long byteRate)
			throws IOException {
		int bitsPerSample = 16;//
		byte[] header = new byte[44];
		//ckid：4字节 RIFF 标志，大写
		header[0] = 'R';
		header[1] = 'I';
		header[2] = 'F';
		header[3] = 'F';
		//cksize：4字节文件长度，这个长度不包括"RIFF"标志(4字节)和文件长度本身所占字节(4字节),即该长度等于整个文件长度 - 8
		header[4] = (byte) (totalDataLen & 0xff);
		header[5] = (byte) ((totalDataLen >> 8) & 0xff);
		header[6] = (byte) ((totalDataLen >> 16) & 0xff);
		header[7] = (byte) ((totalDataLen >> 24) & 0xff);
		//fcc type：4字节 "WAVE" 类型块标识, 大写
		header[8] = 'W';
		header[9] = 'A';
		header[10] = 'V';
		header[11] = 'E';
		//ckid：4字节 表示"fmt" chunk的开始,此块中包括文件内部格式信息，小写, 最后一个字符是空格
		header[12] = 'f';
		header[13] = 'm';
		header[14] = 't';
		header[15] = ' ';
		//cksize：4字节，文件内部格式信息数据的大小，过滤字节（一般为00000010H）
		header[16] = 0x10;//16
		header[17] = 0;
		header[18] = 0;
		header[19] = 0;
		//FormatTag：2字节，音频数据的编码方式，1：表示是PCM 编码
		header[20] = 1;
		header[21] = 0;
		//Channels：2字节，声道数，单声道为1，双声道为2
		header[22] = (byte) channels;
		header[23] = 0;
		header[24] = (byte) (longSampleRate & 0xff);
		header[25] = (byte) ((longSampleRate >> 8) & 0xff);
		header[26] = (byte) ((longSampleRate >> 16) & 0xff);
		header[27] = (byte) ((longSampleRate >> 24) & 0xff);

		//BytesPerSec：4字节，音频数据传送速率, 单位是字节。其值为采样率×每次采样大小。播放软件利用此值可以估计缓冲区的大小
		//byteRate = sampleRate * (bitsPerSample / 8) * channels
		header[28] = (byte) (byteRate & 0xff);
		header[29] = (byte) ((byteRate >> 8) & 0xff);
		header[30] = (byte) ((byteRate >> 16) & 0xff);
		header[31] = (byte) ((byteRate >> 24) & 0xff);
		// block align
		//BlockAlign：2字节，每次采样的大小 = 采样精度*声道数/8(单位是字节); 这也是字节对齐的最小单位, 譬如 16bit 立体声在这里的值是 4 字节。
		//播放软件需要一次处理多个该值大小的字节数据，以便将其值用于缓冲区的调整
		header[32] =  (byte)(bitsPerSample * channels / 8);
		header[33] = 0;
		//BitsPerSample：2字节，每个声道的采样精度; 譬如 16bit 在这里的值就是16。如果有多个声道，则每个声道的采样精度大小都一样的
		header[34] = (byte) bitsPerSample;
		header[35] = 0;
		//ckid：4字节，数据标志符（data），表示 "data" chunk的开始。此块中包含音频数据，小写
		header[36] = 'd';
		header[37] = 'a';
		header[38] = 't';
		header[39] = 'a';
		header[40] = (byte) (totalAudioLen & 0xff);
		header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
		header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
		header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
		out.write(header, 0, 44);
	}

	@Override
	public void convert(String inFileName, String outFileName) {
		FileInputStream in;
		FileOutputStream out;
		long totalAudioLen;
		long totalDataLen;
		long longSampleRate = mSampleRate;
		int channels = mChannel == AudioFormat.CHANNEL_IN_MONO ? 1 : 2;
		long byteRate = 16 * mSampleRate * channels / 8;

		byte[] data = new byte[mBufferSize];
		try {
			in = new FileInputStream(inFileName);
			out = new FileOutputStream(outFileName);
			totalAudioLen = in.getChannel().size();
			totalDataLen = totalAudioLen + 36;

			writeWaveFileHeader(out, totalAudioLen, totalDataLen,
					longSampleRate, channels, byteRate);
			while (in.read(data) != -1) {
				out.write(data);
			}
			in.close();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

