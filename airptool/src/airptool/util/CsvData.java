package airptool.util;

public class CsvData {

	private long id;
	private long id2;
	private String classe;
	private String pacote;
	private String classeDoPacote;
	private int a, b ,c, d;
	private String tipo;
	/**
	 * @param id
	 * @param classe
	 * @param pacote
	 * @param classeDoPacote
	 * @param a
	 * @param b
	 * @param c
	 * @param d
	 * @param tipo
	 */
	public CsvData(long id, long id2, String classe, String pacote, String classeDoPacote, int a,
			int b, int c, int d, String tipo) {
		super();
		this.id = id;
		this.id2 = id2;
		this.classe = classe;
		this.pacote = pacote;
		this.classeDoPacote = classeDoPacote;
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
		this.tipo = tipo;
	}
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	/**
	 * @return the id2
	 */
	public long getId2() {
		return id2;
	}
	/**
	 * @param id the id to set
	 */
	public void setId2(long id2) {
		this.id2 = id2;
	}
	/**
	 * @return the classe
	 */
	public String getClasse() {
		return classe;
	}
	/**
	 * @param classe the classe to set
	 */
	public void setClasse(String classe) {
		this.classe = classe;
	}
	/**
	 * @return the pacote
	 */
	public String getPacote() {
		return pacote;
	}
	/**
	 * @param pacote the pacote to set
	 */
	public void setLastName(String pacote) {
		this.pacote = pacote;
	}
	
	public String getClasseDoPacote() {
		return classeDoPacote;
	}
	/**
	 * @param classe the classe to set
	 */
	public void setClassedoPacote(String classeDoPacote) {
		this.classeDoPacote = classeDoPacote;
	}
	/**
	 * @return the a
	 */
	public int getA() {
		return a;
	}
	/**
	 * @param a the a to set
	 */
	public void setA(int a) {
		this.a = a;
	}
	/**
	 * @return the b
	 */
	public int getB() {
		return b;
	}
	/**
	 * @param b the b to set
	 */
	public void setB(int b) {
		this.b = b;
	}
	/**
	 * @return the c
	 */
	public int getC() {
		return c;
	}
	/**
	 * @param c the c to set
	 */
	public void setC(int c) {
		this.c = c;
	}
	/**
	 * @return the d
	 */
	public int getD() {
		return d;
	}
	/**
	 * @param d the d to set
	 */
	public void setD(int d) {
		this.d = d;
	}
	/**
	 * @return the tipo
	 */
	public String getTipo() {
		return tipo;
	}
	/**
	 * @param tipo the tipo to set
	 */
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}
	
	@Override
	public String toString() {
		return "Data [id=" + id + ", classe=" + classe
				+ ", pacote=" + pacote + ", a=" + a + ", b="
				+ b + ", c=" + c + ", d=" + d + ", type=" + tipo +"]";
	}
}