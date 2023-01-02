package org.zeith.onlinedisplays.ext.gif.decoders;

import org.zeith.onlinedisplays.ext.gif.lib.GIFFrame;
import org.zeith.onlinedisplays.util.io.IFileContainer;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ADecoder
{
	public static GIFFrame[] decode(IFileContainer f)
	{
		String pkg = ADecoder.class.getPackage().getName();
		int i = 0;
		while(true)
		{
			try
			{
				while(true)
				{
					Method m = Class.forName(pkg + ".D" + i).getDeclaredMethod("decode", IFileContainer.class);
					++i;
					m.setAccessible(true);
					try
					{
						return (GIFFrame[]) m.invoke(null, f);
					} catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException exception)
					{
						continue;
					}
				}
			} catch(ClassNotFoundException e)
			{
			} catch(NoSuchMethodException | SecurityException noSuchMethodException)
			{
				continue;
			}
			break;
		}
		return null;
	}
}
