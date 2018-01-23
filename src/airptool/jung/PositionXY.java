package airptool.jung;

public class PositionXY {

	String pkg;
	public String getPkg() {
		return pkg;
	}

	public void setPkg(String pkg) {
		this.pkg = pkg;
	}

	double x, y, h;
	
	public PositionXY(String pkg, double x, double y, double h){
		this.pkg=pkg;
		this.x=x;
		this.y=y;
		this.h=h;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getH() {
		return h;
	}

	public void setH(double h) {
		this.h = h;
	}
}
