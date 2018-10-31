package battlecall.ml.multimedia.uitls;

/**
 * Created by BattleCall on 2018/10/30.
 */

public interface Encoder {
	public void startEncoder();
	public void stopEncoder();
	public void putData(byte[] buffer);
	public boolean outMaxSize();
}
