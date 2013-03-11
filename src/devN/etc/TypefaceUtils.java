package devN.etc;

import java.util.Hashtable;
import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * v2.1 added
 * @author http://code.google.com/p/android/issues/detail?id=9904
 * @author Nilos
 */
public final class TypefaceUtils
{
	private static final String TAG = "Typefaces";

	private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

	public static void setAllTypefaces(ViewGroup root, Context c, String assetPath)
	{
		Typeface typeface = get(c, assetPath);

		for (int i = 0; i < root.getChildCount(); i++)
		{
			View child = root.getChildAt(i);
			if (child instanceof TextView)
			{
				((TextView) child).setTypeface(typeface);
			}
			else if (child instanceof ViewGroup) 
			{
				setAllTypefaces((ViewGroup) child, c, assetPath);
			}
		}
	}
	
	public static Typeface get(Context c, String assetPath) 
	{
		synchronized (cache) 
		{
			if (!cache.containsKey(assetPath)) 
			{
				try 
				{
					Typeface t = Typeface.createFromAsset(c.getAssets(), assetPath);
					
					cache.put(assetPath, t);
				} 
				catch (Exception e) 
				{
					Log.e(TAG, "Could not get typeface ' " + assetPath + " ' because " + e.getMessage());
					
					return Typeface.DEFAULT;
				}
			}
			
			return cache.get(assetPath);
		}
	}
}