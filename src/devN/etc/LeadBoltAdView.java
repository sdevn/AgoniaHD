package devN.etc;

import static java.lang.String.format;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URLEncoder;
import java.util.Enumeration;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.webkit.WebView;
import android.webkit.WebViewClient;

@SuppressWarnings("unused")
public class LeadBoltAdView extends WebView
{
	private final static long AD_REFRESH_INTERVAL = 30000;
	private final static String DATA = "<html><body style='margin:0;padding:0;'><script type='text/javascript' " +
										"src='http://ad.leadboltads.net/show_app_ad.js?section_id=%s'></script></body></html>";
	private final static String[] SEC_IDS = {"333453485", "149341985", "953292552", "682157046"};
	
	private final static String MOB_FOX_ID = "819563d36a65ebca90245302554ddb33";
	
	private final String myAdId;
	private final String mobFoxURL;
	private long nextRefresh = AD_REFRESH_INTERVAL;
	private boolean visible;
	private boolean fixed;
	
	public LeadBoltAdView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		
		String agent = "";
		
		if (isInEditMode())
		{
			mobFoxURL = myAdId = "";
			return;
		}

		getSettings().setJavaScriptEnabled(true);

		int screenSize;

		if (fixed = "fixed".equals(getTag()))
		{
			int[] attrsArray = {android.R.attr.layout_width,
			                    android.R.attr.layout_height};
			
			TypedArray a = context.obtainStyledAttributes(attrs, attrsArray);
			
			screenSize = a.getDimensionPixelSize(0, 640);
			
			a.recycle();

			setContentDescription(getContentDescription() + " + " + screenSize);

			mobFoxURL = "";
		}
		else 
		{
			DisplayMetrics dm = getResources().getDisplayMetrics();
			screenSize = (int) (dm.widthPixels / dm.density);
			
			try
			{
				agent = URLEncoder.encode(getSettings().getUserAgentString(), "UTF-8");
			}
			catch (UnsupportedEncodingException ex)
			{
				DBGLog.ads("Cant get agent " + ex.getMessage());
			}
			
			mobFoxURL = "http://my.mobfox.com/request.php?rt=api" +
						"&s=" + MOB_FOX_ID +
						"&v=2.0" +
						"&u=" + agent +
						"&i=" + getIP() +
						"&m=live";
		}
		
		if (screenSize >= 720)
		{
			myAdId = SEC_IDS[3];
		}
		else if (screenSize >= 640)
		{
			myAdId = SEC_IDS[2];
		}
		else if (screenSize >= 468)
		{
			myAdId = SEC_IDS[1];
		}
		else 
		{
			myAdId = SEC_IDS[0];
		}

		setWebViewClient(new AutoHideClient());
		setVisibility(GONE);
	}

	public LeadBoltAdView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	@Override
	public void onWindowFocusChanged(boolean hasWindowFocus)
	{
		if (visible = hasWindowFocus)
		{
			refresher.run();
		}
		
		super.onWindowFocusChanged(hasWindowFocus);
	}

	private void refreshAd()
	{
		if (!fixed)
		{
			new AdReciver().execute(mobFoxURL);
		}
		else 
		{
			DBGLog.ads(getContentDescription() + " is fixed " + myAdId);
			refreshAd("");
		}
	}
	
	private void refreshAd(String data)
	{
		if ("".equals(data))
		{
			return;
//			data = format(DATA, myAdId);
		}
		
		loadData(data, "text/html", "UTF-8");		
	}
	
	/** http://stackoverflow.com/a/10199498/1545525 */
	private String getIP()
	{
		try
		{
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();)
			{
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
				{
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress())
					{
						@SuppressWarnings("deprecation")
						String ip = Formatter.formatIpAddress(inetAddress.hashCode());
						return ip;
					}
				}
			}
		}
		catch (SocketException ex)
		{
			DBGLog.ads("Error on getIP()" + ex.getMessage());
		}
		
		return "";
	}
	
	private final Runnable refresher = new Runnable(){
		public void run()
		{
			if (visible)
			{
				refreshAd();
				postDelayed(refresher, nextRefresh);
			}
			else 
			{
				DBGLog.ads(getContentDescription() + ", im paused! " + myAdId);
			}
		}
	};
	
	private final class AutoHideClient extends WebViewClient
	{
		@Override
		public void onPageFinished(WebView view, String url)
		{
			super.onPageFinished(view, url);
			view.setVisibility(VISIBLE);
		}

		@Override
		public void onPageStarted(WebView view, String url, Bitmap favicon)
		{
			view.setVisibility(GONE);
			super.onPageStarted(view, url, favicon);
		}	
	}
	
	private final class AdReciver extends RequestTask
	{

		@Override
		protected void onPostExecute(String result)
		{
			String data = "";
			
			if (result == null)
			{	// v2.3.1b
				return;
			}
			
			if ("".equals(result) || result.contains("<request type=\"noAd\">"))
			{
				DBGLog.ads("Empty or noAd. Len " + result.length() + " " + getContentDescription());
			}
			else 
			{	
				try
				{
					DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
					DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
					Document doc = dBuilder.parse(new InputSource(new ByteArrayInputStream(result.getBytes("utf-8"))));
					
					doc.getDocumentElement().normalize();
					
					data = doc.getElementsByTagName("htmlString")
								.item(0)
								.getTextContent();
					
					nextRefresh = Long.parseLong(doc.getElementsByTagName("refresh")
													.item(0).getTextContent()) * 1000;
					
					// DBGLog.ads(getContentDescription() + " nextRefresh " + nextRefresh);
				}
				catch (Exception ex)
				{
					DBGLog.ads("MobFox request error " + ex.toString() + ex.getMessage());
				}
			}
			
			refreshAd(data);
		}
	}
}