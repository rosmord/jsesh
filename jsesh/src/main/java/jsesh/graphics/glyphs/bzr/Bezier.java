package jsesh.graphics.glyphs.bzr;

/**
 * Utility functions related to bezier splines.
 *
 *
 * Created: Sun Jun  2 14:38:58 2002
 *
 * @author <a href="mailto:rosmord@djedefhor.iut.univ-paris8.fr">Serge ROSMORDUC</a>
 * 
 */

public class Bezier {
  public java.awt.geom.Point2D.Double controls[];

  static final int MAXPTS= 8;
    
 
  public Bezier()
  {
    controls= new java.awt.geom.Point2D.Double[4];
    for (int i= 0; i< 4; i++)
      {
	controls[i]= new java.awt.geom.Point2D.Double(0.0, 0.0);
      }
  }

  public Bezier(double p1x,double  p1y,
		double  c1x,double  c1y,
		double  c2x,double  c2y,
		double  p2x,double  p2y)
  {
    controls= new java.awt.geom.Point2D.Double[4];
    controls[0]= new java.awt.geom.Point2D.Double(p1x, p1y);
    controls[1]= new java.awt.geom.Point2D.Double(c1x, c1y);
    controls[2]= new java.awt.geom.Point2D.Double(c2x, c2y);
    controls[3]= new java.awt.geom.Point2D.Double(p2x, p2y);
  }
    

  public double getControlX(int i)
  {
    return controls[i].getX();
  }

  public double getControlY(int i)
  {
    return controls[i].getY();
  }

  public void setControl(int i, double x, double y)
  {
    controls[i].setLocation(x,y);
  }

  public double getArea()
  {
    return area(getControlX(0), getControlY(0),
		getControlX(1), getControlY(1),
		getControlX(2), getControlY(2),
		getControlX(3), getControlY(3));

  }

  public static double area(double p1x,double p1y,
			    double c1x,double c1y,
			    double c2x,double c2y,
			    double p2x,double p2y)
  {
    double result;
    
    double ux, uy,vx, vy ;
    double wx, wy, zx, zy;
    double x, y, t;

    /*
     * Approximating the spline by just the control points 
     * is far too rough.
     */
	
    result= 0.0;
    
    ux= - p1x + 3*c1x - 3*c2x + p2x;
    vx= 3*c2x + 3*p1x -6 * c1x;
    wx= 3*c1x- 3*p1x;
    zx= p1x;
    uy= - p1y + 3*c1y - 3*c2y + p2y;
    vy= 3*c2y + 3*p1y -6 * c1y;
    wy= 3*c1y- 3*p1y;
    zy= p1y;
    for (t= 0.0; t < 1.0; t= t+1.0/MAXPTS)
      {
	x= ((ux*t+vx)*t+wx)*t+zx;
	y= ((uy*t+vy)*t+wy)*t+zy;
	result+= (x -p1x) * (y +p1y);
	p1x= x;
	p1y= y;
      }
    p1x= p2x;
    p1y= p2y;

    return result;
  }
}// Bezier
