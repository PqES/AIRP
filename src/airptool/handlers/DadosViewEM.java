package airptool.handlers;

public class DadosViewEM {
	private double fx;
	private String pkg_ori;
	private String clss_ori;
	private String met_ori;
	private int blo_ori;
	
	public DadosViewEM(Double fx, String pkg_ori, String clss_ori, String met_ori, int blo_ori){
		this.fx=fx;
		this.pkg_ori=pkg_ori;
		this.clss_ori=clss_ori;
		this.met_ori=met_ori;
		this.blo_ori=blo_ori;
	}

	public String getMet_ori() {
		return met_ori;
	}

	public void setMet_ori(String met_ori) {
		this.met_ori = met_ori;
	}

	public int getBlo_ori() {
		return blo_ori;
	}

	public void setBlo_ori(int blo_ori) {
		this.blo_ori = blo_ori;
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
}
