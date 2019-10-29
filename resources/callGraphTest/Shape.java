class Shape extends java.lang.Object {
	private int color; // color of the object

	// constructor
	public Shape() {
		this.color = 0;
	}

	// print the shape
	public void printShape() {
		if (dyeShape(color) == 1) {
			System.out.println("Color is red.");
		} else {
			System.out.println("Color is blue.");
		}
	}

	// helper function
	private int dyeShape(int color) {
		return 1; // success or failure
	}
	
	public int getColor() {
		return this.color;
	}
}