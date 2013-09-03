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
		Log.i("devads", msg);
	}
	
	public static void iads(String msg)
	{
		Log.i("deviads", msg);
	}
	
	public static void srlz(String msg)
	{
		Log.v("srlz", msg);
	}
	
	public static void rltm(String msg)
	{
		Log.d("rltm", msg);
	}
}
