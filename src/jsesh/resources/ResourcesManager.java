/*
 * Created on 3 oct. 2004 by rosmord
 * This code can be distributed under the Gnu Library Public Licence.
 **/
package jsesh.resources;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

/**
 * A container for various resources (like fonts).
 * @author rosmord
 *
 */
public class ResourcesManager
{
	static private ResourcesManager instance;
	private Font transliterationFont;

	private ResourcesManager()
	{
		InputStream in= null;

		try
		{
			in= ResourcesManager.class.getResourceAsStream("MDCTranslitLC.ttf");
			if (in == null) throw new FileNotFoundException("file not found");
			transliterationFont= Font.createFont(Font.TRUETYPE_FONT, in);
			transliterationFont= transliterationFont.deriveFont(12f);
		}
		catch (FontFormatException e)
		{
			System.err.println("problem with font file format " + e);
			e.printStackTrace();
			transliterationFont= new Font(null, Font.ITALIC, 12);
		}
		catch (IOException e)
		{
			System.err.println("problem with font loading " + e);
			transliterationFont= new Font(null, Font.ITALIC, 12);
			
			e.printStackTrace();
		}
		finally
		{
			try
			{
				if (in != null)
					in.close();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	static public ResourcesManager getInstance()
	{
		if (instance == null)
		{
			instance= new ResourcesManager();
		}
		return instance;
	}
	
	
	/**
	 * @return
	 */
	public Font getTransliterationFont()
	{
		return transliterationFont;
	}
	
	public Reader getLigatureData() {
		Reader in;
		in= new InputStreamReader(ResourcesManager.class.getResourceAsStream("ligatures.txt"));
		return in;
	}
	
	public Reader getDemoData() {
		Reader in;
		in= new InputStreamReader(ResourcesManager.class.getResourceAsStream("allmdc.gly"));
		return in;
	}

}
