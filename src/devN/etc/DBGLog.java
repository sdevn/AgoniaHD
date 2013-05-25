package devN.etc;

import android.util.Log;

public final class DBGLog
{
	public static void dbg(String msg)
	{
		Log.d("dbg", msg);
	}
	
	public static void ads(String msg)
	{
		Log.i("ads", msg);
	}
	
	public static void iads(String msg)
	{
		Log.i("iads", msg);
	}
	
	public static void srlz(String msg)
	{
		Log.v("srlz", msg);
	}
}
