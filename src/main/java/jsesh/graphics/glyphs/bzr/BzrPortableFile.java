package jsesh.graphics.glyphs.bzr;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * A low level binary reader for reading BZR files. 
 *
 *
 * Created: Sat May 25 17:09:52 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */


public class BzrPortableFile extends FilterInputStream implements BzrCodes {

    public static final double MAXREAL= (255.0 + 255.0/256 + 255.0/(256*256));
    
    // This variable is used locally by read?Double.
    // but we create it here for performance.
    
    private double buffer[];

    // was last real positive ?
    private boolean isPositive;

    public BzrPortableFile (InputStream in) {
	super(in);
	buffer= new double[3];
    }

    public double readBDouble() throws IOException
    {
	double d;
	readBuffer(3);
	d= buffer[0]+ buffer[1] / (1 << 8) + buffer[2] / (1 << 16);
	if (! isPositive)
	    d= d - MAXREAL;
	return d;
    }
    
    public double readSDouble() throws IOException
    {
	double d;
	readBuffer(2);
	d= buffer[0]/ (1 << 8) + buffer[1]/ (1 << 16);
	if (!isPositive)
	    d= d - 1;
	return d;
    }
    
    private void readBuffer(int n) throws IOException {
	int hi= read();
	if ((hi & (1 << 7)) != 0)
	    isPositive= false;
	else
	    isPositive= true;

	buffer[0]= hi;	
	for (int i= 1; i<n; i++)
	    {
		buffer[i]= read();
	    }
    }

    public int skipNops() throws IOException
    {
	int c;
	while ((c=read()) == NO_OP);
	return c;
    }
  
    /**
     * Read a String from a portable file.
     * string storage : 
     * first, a byte (string size)
     * then size bytes (the chars in ASCII)
     * 
     * @return a <code>String</code> value. No attempt is currently made
     * to have a correct representation for non ASCII chars.
     * @throws IOException
     *
     */

    public String readString() throws IOException
    {
	int size;
	byte b[];
	size= read();
	b= new byte[size];
	read(b);
	return new String(b, "ISO-8859-1");
    }
    
}// BzrPortableFile
