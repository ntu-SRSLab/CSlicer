class Ball extends Shape{
	private double radius;
	
	public Ball (){
		super();
		radius = 1.0;
	}
	
	public double getVolume (){
		return (4 * Math.PI * java.lang.Math.pow(radius,3)) / 3;
	}
	
	public double getSurfaceArea (){
		return 4 * Math.PI * java.lang.Math.pow(radius, 2);
	}
}