
public final class Ponto4D {
	private double x;
	private double y;
	private double z;
	private double w;

	public Ponto4D() {
		this(0, 0, 0, 1);
	}
	
	public Ponto4D(double x, double y, double z, double w) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}
	
	// inverte o sinal das coordenadas do ponto
	public void inverterSinal() {
		this.atribuirX(this.obterX()*-1);
		this.atribuirY(this.obterY()*-1);
		this.atribuirZ(this.obterZ()*-1);
	}
	
	public double obterX() {
		return x;
	}
	
	public double obterY() {
		return y;
	}
	
	public double obterZ() {
		return z;
	}
	
	public double obterW() {
		return w;
	}

	public void atribuirX(double x) {
		this.x = x;
	}
	
	public void atribuirY(double y) {
		this.y = y;
	}
	
	public void atribuirZ(double z) {
		this.z = z;
	}
}
