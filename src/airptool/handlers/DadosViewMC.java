package airptool.handlers;

public class DadosViewMC {
	private double fx;
	private String pkg_ori;
	private String clss_ori;
	private String pkg_des;
	
	public DadosViewMC(Double fx, String pkg_ori, String clss_ori, String pkg_des){
		this.fx=fx;
		this.pkg_ori=pkg_ori;
		this.clss_ori=clss_ori;
		this.pkg_des=pkg_des;
	}

	public double getFx() {
		return fx;
	}

	public void setFx(double fx) {
		this.fx = fx;
	}

	public String getPkg_ori() {
		return pkg_ori;
	}

	public void setPkg_ori(String pkg_ori) {
		this.pkg_ori = pkg_ori;
	}

	public String getClss_ori() {
		return clss_ori;
	}

	public void setClss_ori(String clss_ori) {
		this.clss_ori = clss_ori;
	}

	public String getPkg_des() {
		return pkg_des;
	}

	public void setPkg_des(String pkg_des) {
		this.pkg_des = pkg_des;
	}
}
