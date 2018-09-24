package ru.runa.wfe.graph.image.util;

import java.awt.geom.Point2D;

public class GraphicsMath {
	/**
	 * Returns the middle point of segment joining points a and b. 
	 */
	public static Point2D.Double getMiddle(Point2D.Double a, Point2D.Double b) {
		return new Point2D.Double((a.x+b.x)/2, (a.y+b.y)/2); 
	}
	
	/**
	 * Returns angle between to vectors (in radians: 0 ... +2*Math.Pi).
	 * Attention: Basis vectors are (1,0) and (0,-1)!
	 * @param a - first vector
	 * @param b - second vector
	 * @return angle
	 */
	public static double getVectorAngle(Point2D.Double a, Point2D.Double b) {
		double angle = 0;
		
		/** acos of angle. Obtained via scalar product */
		double acos=Math.acos((a.x*b.x+a.y*b.y)/(getLength(a)*getLength(b)));
		
		/** determinant */
		// Determinant has unary minus, because bases vectors are (1,0), (0,-1) 
		double d=-(a.x*b.y-a.y*b.x);
		
		// If determinant > 0 - right couple. Else - left.
		if(d>0) angle=acos; // a->b
		else angle=2*Math.PI-acos; // b->a
				
		return angle;
	}

	/**
	 * Returns signed angle between to vectors (in radians: -Math.Pi ... +Math.Pi).
	 * Attention: Basis vectors are (1,0) and (0,-1)!
	 * @param a - first vector
	 * @param b - second vector
	 * @return angle
	 */
	public static double getVectorSignedAngle(Point2D.Double a, Point2D.Double b) {
		double angle = getVectorAngle(a, b);		
		if(angle>Math.PI) angle=-2*Math.PI+angle;
		return angle;
	}

	
    /**
     * Function to calculate the angle between vector (1,0) and vector (x2-x1, y2-y1).  
     * @return angle
     */
    public static double getAngle(double x1, double y1, double x2, double y2) {
    	return getVectorAngle(new Point2D.Double(1, 0), new Point2D.Double(x2-x1, y2-y1));
    }
     
    /**
     * Function to calculate coordinates of point which is on beam from source point to destination point and on fixed distance from source point    
     * @param f - source point
     * @param t - destination point
     * @param len - fixed distance
     * @return point
     */
    public static Point2D.Double getScaledVector(Point2D.Double f, Point2D.Double t, double len) {
    	Point2D.Double r = new Point2D.Double();
    	double clen = getLength(f, t);
    	r.x = f.x+(t.x-f.x)*(len/clen);   	
    	r.y = f.y+(t.y-f.y)*(len/clen);
    	return r;
    }
    
    
    
    /**
     * Function to calculate distance between two point
     * @param x1 x-coordinate of first point
     * @param y1 y-coordinate of first point
     * @param x2 x-coordinate of second point
     * @param y2 y-coordinate of second point
     * @return distance
     */
    public static double getLength(double x1, double y1, double x2, double y2) {
    	return Math.sqrt(Math.pow(x1-x2, 2)+Math.pow(y1-y2, 2));
    }

    /**
     * Function to calculate distance between two point
     * @param a first point
     * @param b second point
     * @return distance
     */
    public static double getLength(Point2D.Double a, Point2D.Double b) {
    	return getLength(a.getX(), a.getY(), b.getX(), b.getY());
    }

    
    /**
     * Function to calculate vector length
     * @param a vector
     * @return vector length
     */
    public static double getLength(Point2D.Double a) {
    	return Math.sqrt(Math.pow(a.x, 2)+Math.pow(a.y, 2));
    }

    
    /**
     * Get vector by two points
     * @param a - source point
     * @param b - destination point
     * @return vector
     */
    public static Point2D.Double getVector(Point2D.Double a, Point2D.Double b) {
    	return new Point2D.Double(b.x-a.x, b.y-a.y);
    }
}
