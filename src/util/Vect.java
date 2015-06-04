package util;

public final class Vect {
	
	public static double normE(Double[] v) {
		// returns Euclidean norm
		double temp = 0.0;
		for(int i=0; i<v.length; i++)
			temp += v[i] * v[i];
		return Math.sqrt(temp);
	}
	
	public static double normSquare(Double[] v) {
		// returns Euclidean norm
		double temp = 0.0;
		for(int i=0; i<v.length; i++)
			temp += v[i] * v[i];
		return temp;
	}
	
	public static Double[] minus(Double[] v, Double[] second) {
		Double[] c = v.clone();
		for(int i=0; i<c.length; i++) 
			c[i] -= second[i];
		return c;	
	}
	
	public static Double[] normalize(Double[] v) {
		Double[] c = v.clone();
		double norm = normE(v);
		for(int i=0; i<v.length; i++)
			v[i] /= norm;
		return c;
	}
	
	public static void add(Double[] vector, Double second) {
		for(int i=0; i<vector.length; i++) 
			vector[i] += second;
	}
	
	public static void add(Double[] vector, Double[] second) {
		for(int i=0; i<vector.length; i++) 
			vector[i] += second[i];
	}
	
	public static Double[] sum(Double[] vector, Double[] second) {
		Double[] c = vector.clone();
		for(int i=0; i<c.length; i++) 
			c[i] += second[i];
		return c;
	}
	
	public static void addIffy(Double[] vector, Double[] second) {
		double l = Math.min(vector.length, second.length);
		for(int i=0; i<l; i++) 
			vector[i] += second[i];
	}
	
	public static Double[] product(Double[] vector, double scalar) {
		Double[] c = vector.clone();
		for (int i = 0; i < c.length; i++)
			c[i] *= scalar;
		return c;
	}
	
	public static double dot(Double[] vector, Double[] second) {
		double dot = 0.0;
		for (int i = 0; i < vector.length; i++)
			dot += vector[i] * second[i];	
		return dot;
	}

	public static void reset(Double[] vector) {
		for(int i=0; i<vector.length; i++) 
			vector[i] = 0.0;
	}
	
	public static Double[] randomDirection(int n) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = (Math.random()-0.5)*2.0;
		return v;
	}
	
	public static Double[] randomDirection(int n, double scalar) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = scalar*(Math.random()-0.5)*2.0;
		return v;
	}
	
	public static Double[] zeros(int n) {
		Double[] v = new Double[n];
		for(int i = 0; i < n; i++)
			v[i] = 0.0;
		return v;
	}
	
	public static double squareLength(Double[] v) {
		// returns the squared Euclidean norm
		double temp = 0.0;
		for(int i=0; i<v.length; i++)
			temp += v[i] * v[i];
		return temp;
	}

	public static Double[] inverse(Double[] vector) {
		Double[] v = new Double[vector.length];
		for(int i = 0; i < vector.length; i++)
			v[i] = 0.0 - vector[i];
		return v;
	}
	
	public static Double[] between(Double[] v0, Double[] v1, double shift) {
		Double[] h 		= minus(v0,v1);
		h 				= normalize(h);
		double distance	= normE(h);
		add(h,0.005*(Math.random()-0.5));
		return minus(v0,product(h,(distance/2.0)-shift)); 
	}

}
