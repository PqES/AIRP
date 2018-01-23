package airptool.handlers;

public class DadosView {
	private double fx;
	private String tipo;
	private String pkg_ori;
	private String clss_ori;
	private String met_ori;
	private String blo_ori;
	private String pkg_des;
	private String clss_des;
	
	public DadosView(Double fx, String pkg_ori, String clss_ori, String met_ori, String pkg_des, String clss_des){
		this.fx=fx;
		this.pkg_ori=pkg_ori;
		this.clss_ori=clss_ori;
		this.met_ori=met_ori;
		this.pkg_des=pkg_des;
		this.clss_des=clss_des;
		this.tipo="MM";
	}
	
	public DadosView(Double fx, String pkg_ori, String clss_ori, String met_ori, String blo_ori){
		this.fx=fx;
		this.pkg_ori=pkg_ori;
		this.clss_ori=clss_ori;
		this.met_ori=met_ori;
		this.blo_ori=blo_ori;
		this.tipo="EM";
	}
	
	public DadosView(Double fx, String pkg_ori, String clss_ori, String pkg_des){
		this.fx=fx;
		this.pkg_ori=pkg_ori;
		this.clss_ori=clss_ori;
		this.pkg_des=pkg_des;
		this.tipo="MC";
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getBlo_ori() {
		return blo_ori;
	}

	public void setBlo_ori(String blo_ori) {
		this.blo_ori = blo_ori;
	}

	public String getMet_ori() {
		return met_ori;
	}

	public void setMet_ori(String met_ori) {
		this.met_ori = met_ori;
	}

	public String getClss_des() {
		return clss_des;
	}

	public void setClss_des(String clss_des) {
		this.clss_des = clss_des;
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
