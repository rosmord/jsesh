/*
 * Created on 28 nov. 2004
 */

package jsesh.hieroglyphs;

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
 * <p> Test result :
 * <p> Hsqldb is quite efficient, as long as we don't use the shutdown command to close the database.
 * The main problem is the lock file, which prevents two programs from using the database at the same time.
 * In version 1.8, it's possible to open a database in readonly mode, which allows to run more than one software at the same time.
 * <p> The other, better solution, would be to start a server. Now, there are two problems :
 * <ul>
 * <li> a port should be chosen for this, and it's a bit technical if the port is already used.
 * <li> running a daemon on a machine is always dangerous.
 * </ul>
 * @author rosmord
 */
public class HieroglyphicDB implements HieroglyphicFontManager {
	Connection db;

	HashMap glyphs;

	boolean hasNewSigns;
	
	public HieroglyphicDB() throws SQLException {
		try {
			Class.forName("org.hsqldb.jdbcDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		hasNewSigns= true;
		glyphs = new HashMap();
	}

	private void open() throws SQLException {
		db = DriverManager.getConnection(
				"jdbc:hsqldb:file:test;readonly=false", "sa", "");
	}

	private void openRO() throws SQLException {
		db = DriverManager.getConnection("jdbc:hsqldb:file:test;readonly=true",
				"sa", "");
	}

	/*
	 * IMPORTANT : test only. Should cache result. (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.HieroglyphsManagerInterface#get(java.lang.String)
	 */
	public ShapeChar getFromDatabase(String code) {
		ShapeChar result = null;
		// System.err.print(code + " ");
		try {
			openRO();
			PreparedStatement pst = db
					.prepareStatement("select minx, miny, maxx, maxy, data from glyph where code = ?");
			pst.setString(1, code);
			ResultSet rs = pst.executeQuery();
			if (rs.next()) {
				String data = rs.getString(5);
				result = decodeChar(data);
				result.setBBox(rs.getDouble("minx"), rs.getDouble("maxy"), rs
						.getDouble("maxx"), rs.getDouble("miny"));
			}
			rs.close();
			pst.close();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see jsesh.hieroglyphs.HieroglyphsManagerInterface#get(java.lang.String)
	 */
	public ShapeChar get(String code) {
		if (glyphs.containsKey(code))
			return (ShapeChar) glyphs.get(code);
		else {
			ShapeChar result = getFromDatabase(code);
			if (result != null)
				glyphs.put(code, result);
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
			openRO();
			Statement st = db.createStatement();

			ResultSet rs = st.executeQuery("select code from glyph");
			while (rs.next()) {
				set.add(rs.getString(1));
			}
			rs.close();
			st.close();
			close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return set;
	}

	/**
	 * add the representation of a double to a stringbuffer. This function is
	 * necessary, because the simple "toString" can use scientific notation, and
	 * the string tokenizer doesn't understand it.
	 * 
	 * @param buf
	 * @param d
	 */
	private void appendNumberToStringBuffer(StringBuffer buf, double d) {
		if (d < 0) {
			buf.append("-");
			d = -d;
		}
		buf.append((int) d);
		double frac = (d - (int) d);
		buf.append('.');
		for (int i = 0; i < 6; i++) {
			frac = frac * 10;
			buf.append((int) (frac));
			frac = frac - (int) frac;
		}
		for (int i = buf.length() - 1; buf.charAt(i) != '.'; i--) {
			if (buf.charAt(i) == '0')
				buf.delete(i, i + 1);
			else
				break;
		}
	}

	private String encodeChar(ShapeChar glyph) {
		PathIterator iterator = glyph.getShape().getPathIterator(null);
		StringBuffer buff = new StringBuffer();
		String toAdd = "";
		while (!iterator.isDone()) {
			double coords[] = new double[6];
			int type = iterator.currentSegment(coords);
			buff.append(toAdd);
			switch (type) {
			case PathIterator.SEG_CLOSE:
				buff.append("s");
				break;
			case PathIterator.SEG_CUBICTO:
				buff.append("c");
				for (int i = 0; i < 6; i++) {
					buff.append(" ");
					appendNumberToStringBuffer(buff, coords[i]);
				}
				break;
			case PathIterator.SEG_QUADTO:
				buff.append("q");
				for (int i = 0; i < 4; i++) {
					buff.append(" ");
					appendNumberToStringBuffer(buff, coords[i]);
				}
				break;
			case PathIterator.SEG_MOVETO:
				buff.append("m");
				for (int i = 0; i < 2; i++) {
					buff.append(" ");
					appendNumberToStringBuffer(buff, coords[i]);
				}
				break;
			case PathIterator.SEG_LINETO:
				buff.append("l");
				for (int i = 0; i < 2; i++) {
					buff.append(" ");
					appendNumberToStringBuffer(buff, coords[i]);
				}
				break;
			default:
				System.err.println("unexpected code " + type + " in "
						+ glyph.toString());
			}
			toAdd = " ";
			iterator.next();
		}
		return buff.toString();
	}

	private ShapeChar decodeChar(String glyph) {
		GeneralPath path = new GeneralPath();
		// path.setWindingRule(GeneralPath.WIND_EVEN_ODD);
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

				if (command != ' ') {
					switch (command) {
					case 'm':
						if (curr == 2) {
							path.moveTo(coords[0], coords[1]);
							command = ' ';
							curr = 0;
						}
						break;
					case 's':
						if (curr == 0) {
							path.closePath();
							command = ' ';
							curr = 0;
						}
						break;
					case 'l':
						if (curr == 2) {
							path.lineTo(coords[0], coords[1]);
							command = ' ';
							curr = 0;
						}
						break;
					case 'q':
						if (curr == 4) {
							path.quadTo(coords[0], coords[1], coords[2],
									coords[3]);
							command = ' ';
							curr = 0;
						}
						break;
					case 'c':
						if (curr == 6) {
							path.curveTo(coords[0], coords[1], coords[2],
									coords[3], coords[4], coords[5]);
							command = ' ';
							curr = 0;
						}
						break;
					}
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
		HieroglyphicFontManager manager = new MemoryHieroglyphicFontManager();
		Set codes = manager.getCodes();
		try {
			open();
			try {
				Statement st = db.createStatement();
				st.executeUpdate("drop table glyph");
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				Statement st = db.createStatement();
				st
						.executeUpdate("create cached table glyph (code varchar(10) primary key, minx float, miny float, maxx float, maxy float, data varchar(10240));");
				st.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			try {
				PreparedStatement pst = db
						.prepareStatement("insert into glyph values (?,?,?,?,?,?)");
				for (Iterator i = codes.iterator(); i.hasNext();) {
					String code = (String) i.next();
					ShapeChar shapeChar = manager.get(code);
					pst.setString(1, code);
					pst.setDouble(2, shapeChar.getBbox().getMinX());
					pst.setDouble(3, shapeChar.getBbox().getMinY());
					pst.setDouble(4, shapeChar.getBbox().getMaxX());
					pst.setDouble(5, shapeChar.getBbox().getMaxY());
					pst.setString(6, encodeChar(shapeChar));
					pst.executeUpdate();
				}
				pst.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			shutdownAndClose();
		}
	}

	public void insertNewSign(String code, ShapeChar shapeChar) {
		try {

			open();
			shutdownAndClose();
			open();
			PreparedStatement pst = db
					.prepareStatement("insert into glyph values (?,?,?,?,?,?)");
			pst.setString(1, code);
			pst.setDouble(2, shapeChar.getBbox().getMinX());
			pst.setDouble(3, shapeChar.getBbox().getMinY());
			pst.setDouble(4, shapeChar.getBbox().getMaxX());
			pst.setDouble(5, shapeChar.getBbox().getMaxY());
			pst.setString(6, encodeChar(shapeChar));
			pst.executeUpdate();
			pst.close();
		} catch (SQLException e1) {
			e1.printStackTrace();
		} finally {
			shutdownAndClose();
		}

	}

	public void shutdownAndClose() {
		if (db == null)
			return;
		try {
			Statement st = db.createStatement();
			st.executeUpdate("SHUTDOWN COMPACT");
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	public boolean hasNewSigns() {
		return false;
	}
	
	public void close() {
		if (db == null)
			return;
		try {
			/*
			 * Statement st= db.createStatement(); st.executeUpdate("SHUTDOWN");
			 * st.close();
			 */
			db.close();
			db = null;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		if (db != null) {
			db.close();
			db = null;
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

			HieroglyphicDB db = new HieroglyphicDB();
			db.prepareDataBase();
			db.close();
		} else {
			HieroglyphicDB db = new HieroglyphicDB();
			ShapeChar c = db.get("p");
			System.out.println("shape is " + c.getShape());
			System.out.println(db.encodeChar(c));
			db.close();
		}
	}

}