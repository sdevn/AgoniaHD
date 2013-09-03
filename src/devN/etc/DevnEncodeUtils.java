package devN.etc;

import java.util.Random;

public final class DevnEncodeUtils
{
	private final static Random RANDOM = new Random();
	
	public static byte[] encode(byte[] data)
	{
		for (int i = 0; i < data.length; i++)
		{
			data[i] += i;
		}
		
		byte[] encoded = Base64.encode(data, Base64.DEFAULT);
		
		for (int i = 0; i < data.length; i++)
		{
			data[i] -= i;
		}
		
		int pref;
		StringBuilder b = new StringBuilder();
		
		b.append(String.format("%02d%s", pref = RANDOM.nextInt(100), 
										new String(encoded)).replace("\n", ""));
			
		for (int i = 0; i < pref; i++)
		{
			b.append((char) (RANDOM.nextInt('~' - ' ') + ' '));
		}
		
		return Base64.encode(b.toString().getBytes(), Base64.DEFAULT);
	}
	
	public static String encode(String string)
	{
		return new String(encode(string.getBytes()));
	}
	
	public static byte[] decode(byte[] data)
	{
		String builded = new String(Base64.decode(data, Base64.DEFAULT));
		int pref = Integer.parseInt(builded.substring(0, 2));
		
		String encoded = builded.substring(2, builded.length() - pref);
		
		byte[] salted = Base64.decode(encoded, Base64.DEFAULT);
		
		for (int i = 0; i < salted.length; i++)
		{
			salted[i] -= i;
		}
		
		return salted;
	}
	
	public static String decode(String string)
	{
		return new String(decode(string.getBytes()));
	}
	
	public static void main(String[] args)
	{ }
}
