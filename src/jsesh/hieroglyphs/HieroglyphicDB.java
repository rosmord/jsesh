/*
 * Created on 28 nov. 2004
 */

package jsesh.hieroglyphs;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import jsesh.bzr.simple.ShapeChar;

/**
 * Test database for hieroglyphs. We mostly want to test the efficiency of an
 * hsqldb solution.
 * 
 * @author rosmord
 */
public class HieroglyphicDB extends HieroglyphicFontManager {
	Connection db;
	HashMap glyphs;
	
	public HieroglyphicDB() throws SQLException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		db = DriverManager.getConnection("jdbc:hsqldb:file:test", "sa", "");
		glyphs= new HashMap();
	}

	/*
	 * IMPORTANT : test only. Should cache result. (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.HieroglyphsManagerInterface#get(java.lang.String)
	 */
	public ShapeChar getFromDatabase(String code) {
		ShapeChar result = null;
		System.err.print(code + " ");
		try {
			PreparedStatement pst = db
					.prepareStatement("select data from glyph where code = ?");
			pst.setString(1, code);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				String data = rs.getString(1);
				result = decodeChar(data);
			}
			rs.close();
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see jsesh.hieroglyphs.HieroglyphsManagerInterface#get(java.lang.String)
	 */
	public ShapeChar get(String code) {
		if (glyphs.containsKey(code))
			return (ShapeChar)glyphs.get(code);
		else
		{
			ShapeChar result= getFromDatabase(code);
			if (result!= null)
				glyphs.put(code,result);
			return result;
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.HieroglyphsManagerInterface#getCodes()
	 */
	public Set getCodes() {
		TreeSet set = new TreeSet();
		try {
			Statement st = db.createStatement();

			ResultSet rs = st.executeQuery("select code from glyph");
			while (rs.next()) {
				set.add(rs.getString(1));
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}

	private String encodeChar(ShapeChar glyph) {
		PathIterator iterator = glyph.getShape().getPathIterator(
				new AffineTransform());
		StringBuffer buff = new StringBuffer();
		while (!iterator.isDone()) {
			double coords[] = new double[6];
			int type = iterator.currentSegment(coords);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				buff.append("s");
				break;
			case PathIterator.SEG_CUBICTO:
				buff.append("c");
				for (int i = 0; i < 6; i++) {
					buff.append(" ");
					buff.append(coords[i]);
				}
				break;
			case PathIterator.SEG_QUADTO:
				buff.append("q");
				for (int i = 0; i < 4; i++) {
					buff.append(" ");
					buff.append(coords[i]);
				}
				break;
			case PathIterator.SEG_MOVETO:
				buff.append("m");
				for (int i = 0; i < 2; i++) {
					buff.append(" ");
					buff.append(coords[i]);
				}
				break;
			case PathIterator.SEG_LINETO:
				buff.append("l");
				for (int i = 0; i < 2; i++) {
					buff.append(" ");
					buff.append(coords[i]);
				}
				break;
			}
			iterator.next();
		}
		return buff.toString();
	}

	private ShapeChar decodeChar(String glyph) {
		GeneralPath path = new GeneralPath();
		StreamTokenizer tok = new StreamTokenizer(new StringReader(glyph));
		try {
			int curr = 0;
			float coords[] = new float[6];
			char command = ' '; // No command. 
			do {
				tok.nextToken();

				if (tok.ttype == StreamTokenizer.TT_NUMBER) {
					coords[curr++] = (float) tok.nval;
				}

				if (tok.ttype == StreamTokenizer.TT_WORD
						|| tok.ttype == StreamTokenizer.TT_EOF) {
					switch (command) {
					case 'm':
						path.moveTo(coords[0], coords[1]);
						break;
					case 's':
						path.closePath();
						break;
					case 'l':
						path.lineTo(coords[0], coords[1]);
						break;
					case 'q':
						path.quadTo(coords[0], coords[1], coords[2], coords[3]);
						break;
					case 'c':
						path.curveTo(coords[0], coords[1], coords[2],
								coords[3], coords[4], coords[5]);
						break;
					}
					curr = 0;
				}
				if (tok.ttype == StreamTokenizer.TT_WORD)
					command = tok.sval.charAt(0);

			} while (tok.ttype != StreamTokenizer.TT_EOF);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		ShapeChar result = new ShapeChar();
		result.setShape(path);
		result.setBbox(path.getBounds2D());
		return result;
	}

	private void prepareDataBase() {
		HieroglyphicFontManager manager = HieroglyphicFontManager.getInstance();
		Set codes = manager.getCodes();
		try {
			Statement st= db.createStatement();
			st.executeUpdate("create cached table glyph (code varchar(10) primary key,	data varchar(10240));");
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		try {	
			PreparedStatement pst = db
			.prepareStatement("insert into glyph values (?,?)");
			for (Iterator i = codes.iterator(); i.hasNext();) {
				String code = (String) i.next();
				ShapeChar shapeChar = manager.get(code);
								pst.setString(1, code);
				pst.setString(2, encodeChar(shapeChar));
				pst.executeUpdate();
			}
			pst.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			db.close();
			db= null;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if (db != null)
		{
			db.close();
			db= null;
		}
	}
	/**
	 * Calling the main method creates the database file.
	 * 
	 * @param args
	 * @throws SQLException
	 */
	public static void main(String[] args) throws SQLException {
		if (args.length > 0 && "create".equals(args[0])) {

			HieroglyphicDB db= new HieroglyphicDB();
			db.prepareDataBase();
			db.close();
		} else {
			HieroglyphicDB db= new HieroglyphicDB();
			ShapeChar c= db.get("p");
			System.out.println("shape is "+ c.getShape());
			System.out.println(db.encodeChar(c));
			db.close();
		}
	}

}