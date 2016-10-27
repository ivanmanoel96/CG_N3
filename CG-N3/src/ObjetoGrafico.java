import java.util.*;
import javax.media.opengl.*;

public final class ObjetoGrafico {
	private GL gl;

	private int primitiva;
	private List<Ponto4D> vertices;
	private List<ObjetoGrafico> filhos;
	private Ponto4D verticeSelecionado;
	private BoundingBox bBox;
	private short cor = 0;
	private double rastroX, rastroY;

	private Transformacao4D matrizObjeto; 
	private Transformacao4D matrizTmpTranslacao;
	private Transformacao4D matrizTmpTranslacaoInversa;
	private Transformacao4D matrizTmpEscala;
	private Transformacao4D matrizGlobal;
	
	public ObjetoGrafico() {
		this.matrizTmpTranslacao = new Transformacao4D();
		this.matrizTmpTranslacaoInversa = new Transformacao4D();
		this.matrizTmpEscala = new Transformacao4D();
		this.matrizGlobal = new Transformacao4D();
		this.vertices = new ArrayList<>();
		this.filhos = new ArrayList<>();
		this.bBox = new BoundingBox();
		this.matrizObjeto = new Transformacao4D();
		this.primitiva = GL.GL_LINE_STRIP;
	}

	// desenha os vértices do polígno e dos polígnos filhos
	public void desenha() {
		this.desenhaCor();
		this.gl.glLineWidth(2);
		this.gl.glPointSize(2);

		this.gl.glPushMatrix();
			this.gl.glMultMatrixd(this.matrizGlobal.GetDate(), 0);
			this.gl.glBegin(this.primitiva);
				for (short i = 0; i < this.vertices.size(); i++) {
					Ponto4D vertice = this.vertices.get(i);
					this.gl.glVertex2d(vertice.obterX(), vertice.obterY());
				}
			this.gl.glEnd();
			this.desenhaVerticeSelecionado();
			for (short i = 0; i < this.filhos.size(); i++) {
				this.filhos.get(i).desenha();
			}
		this.gl.glPopMatrix();
	}

	public void atribuirGL(GL gl) {
		this.gl = gl;
	}

	// translada as coordenadas x e y, z não influencia
	public void translacaoXYZ(double x, double y, double z) {
		Transformacao4D matrizTranslate = new Transformacao4D();
		matrizTranslate.atribuirTranslacao(x, y, z);
		this.matrizGlobal = matrizTranslate.transformMatrix(matrizGlobal);
	}

	// escala as coordenadas do ponto selecionado em relação ao centro da bbox
	public void escalaXYZPtoFixo(double escala, Ponto4D ptoFixo) {
		this.matrizObjeto.atribuirIdentidade();
		
		ptoFixo.inverterSinal();
		this.matrizTmpTranslacao.atribuirTranslacao(ptoFixo.obterX(), ptoFixo.obterY(), ptoFixo.obterZ());
		this.matrizObjeto = matrizTmpTranslacao.transformMatrix(matrizObjeto);

		this.matrizTmpEscala.atribuirEscala(escala, escala, 1.0);
		this.matrizObjeto = matrizTmpEscala.transformMatrix(matrizObjeto);

		ptoFixo.inverterSinal();
		this.matrizTmpTranslacaoInversa.atribuirTranslacao(ptoFixo.obterX(), ptoFixo.obterY(), ptoFixo.obterZ());
		this.matrizObjeto = matrizTmpTranslacaoInversa.transformMatrix(matrizObjeto);

		this.matrizGlobal = matrizGlobal.transformMatrix(matrizObjeto);
	}

	// rotaciona as coordenadas do ponto selecionado em relação ao centro da bbox
	public void rotacaoZPtoFixo(double angulo, Ponto4D ptoFixo) {
		this.matrizObjeto.atribuirIdentidade();

		ptoFixo.inverterSinal();
		this.matrizTmpTranslacao.atribuirTranslacao(ptoFixo.obterX(), ptoFixo.obterY(), ptoFixo.obterZ());
		this.matrizObjeto = matrizTmpTranslacao.transformMatrix(matrizObjeto);

		this.matrizTmpEscala.atribuirRotacaoZ(Transformacao4D.DEG_TO_RAD * angulo);
		this.matrizObjeto = matrizTmpEscala.transformMatrix(matrizObjeto);

		ptoFixo.inverterSinal();
		this.matrizTmpTranslacaoInversa.atribuirTranslacao(ptoFixo.obterX(), ptoFixo.obterY(), ptoFixo.obterZ());
		this.matrizObjeto = matrizTmpTranslacaoInversa.transformMatrix(matrizObjeto);

		this.matrizGlobal = matrizGlobal.transformMatrix(matrizObjeto);
	}

	// insere vértice e atribue este como vértice selecionado
	public void insereVertice(double x, double y) {
		this.verticeSelecionado = new Ponto4D(x, y, 0, 1);
		this.vertices.add(this.verticeSelecionado);
	}
	
	// desenha um cubo para o vértice selecionado
	private void desenhaVerticeSelecionado() {
		gl.glColor3d(1, 0, 0);
		gl.glPointSize(8);
		gl.glBegin(GL.GL_POINTS);
			gl.glVertex2d(this.verticeSelecionado.obterX(), this.verticeSelecionado.obterY());
		gl.glEnd();
	}
	
	// desenha a bbox
	public void desenhaBbox() {
		this.gl.glPushMatrix();
		this.gl.glMultMatrixd(this.matrizGlobal.GetDate(), 0);
			this.bBox.atribuirBoundingBox(this.verticeSelecionado); 
			for (short i = 0; i < this.vertices.size(); i++)
				this.bBox.atualizarBBox(this.vertices.get(i));
			
			this.bBox.processarCentroBBox();
			this.bBox.desenharOpenGLBBox(this.gl);
		this.gl.glPopMatrix();
	}

	// retorna se ainda contém vértices
	public boolean contemVertices() {
		return !this.vertices.isEmpty();
	}
	
	// seleciona um vértice de acordo com as coordenadas x e y
	public void selecionaVertice(double x, double y) {
		double menorRaio = this.calculaRaio(this.matrizGlobal.transformPoint(this.verticeSelecionado), x, y);
		for (short i = 0; i < this.vertices.size(); i++) {
			Ponto4D vertice = this.vertices.get(i);
			double raio = this.calculaRaio(this.matrizGlobal.transformPoint(vertice), x, y);
			if (raio < menorRaio) {
				menorRaio = raio;
				this.verticeSelecionado = vertice;
			}
		}
	}
	
	// calcula a distância de um vértice até as coordenadas x e y
	public double calculaRaio(Ponto4D vertice, double x, double y) {
		double dx = x - vertice.obterX();
		double dy = y - vertice.obterY();
		return Math.sqrt(dx * dx + dy * dy);
	}
	
	// remove o vértice selecionado atribuindo o último como o selecionado
	public void removeVerticeSelecionado() {
		this.vertices.remove(this.verticeSelecionado);
		this.verticeSelecionado = null;
		if (this.contemVertices())
			this.verticeSelecionado = this.vertices.get(this.vertices.size()-1);
	}
	
	// move o vértice selecionado de acordo com as coordenadas x e y
	public void moveVerticeSelecionado(double x, double y) {
		this.verticeSelecionado.atribuirX(x);
		this.verticeSelecionado.atribuirY(y);
	}
	
	// fecha o desenho
	public void fecha() {
		this.primitiva = GL.GL_LINE_LOOP;
	}
	
	// analisa se as coordenadas x e y pertencem ao polígno
	public boolean analisaSelecao(double x, double y) {
		if (this.analisaBbox(x, y))
			System.out.println("dentro da bbox");
		else
			System.err.println("fora da bbox");
		return this.analisaBbox(x, y) && this.analisaScanLine(x, y);
	}
	
	// analisa se as coordenadas x e y pertencem à bbox do polígno
	public boolean analisaBbox(double x, double y) {
		Ponto4D pontoMaior = this.matrizGlobal.transformPoint(new Ponto4D(this.bBox.obterMaiorX(), this.bBox.obterMaiorY(), this.bBox.obterMaiorZ(), 1));
		Ponto4D pontoMenor = this.matrizGlobal.transformPoint(new Ponto4D(this.bBox.obterMenorX(), this.bBox.obterMenorY(), this.bBox.obterMenorZ(), 1));
//		System.out.println("Menor: " + "[" + pontoMenor.obterX() + " , " +  pontoMenor.obterY() + "]");
//		System.out.println("Maior: " + "[" + pontoMaior.obterX() + " , " +  pontoMaior.obterY() + "]");
		return (x >= pontoMenor.obterX() &&
				x <= pontoMaior.obterX() &&
				y >= pontoMenor.obterY() &&
				y <= pontoMaior.obterY());
	}
	
	// analisa se as coordenadas x e y pertencem à scanline do polígno	
	public boolean analisaScanLine(double x, double y) {
		short totalInterseccoes = 0;
		List<Double> pontosInterseccao = new ArrayList<>();
		this.vertices.add(this.vertices.get(0));
		for (short i = 1; i < vertices.size(); i++) {
			Ponto4D verticeOrigem = this.matrizGlobal.transformPoint(vertices.get(i-1));
			Ponto4D verticeDestino = this.matrizGlobal.transformPoint(vertices.get(i));

			if (this.percenceReta(x, y, verticeOrigem, verticeDestino))
				break;

			if (!pontosInterseccao.contains(y))
				if (this.existeIntersecao(x, y, verticeOrigem, verticeDestino)) {
					if (y == verticeOrigem.obterY() || y == verticeDestino.obterY()) 
						pontosInterseccao.add(y);

					totalInterseccoes++;
				}
		}
		this.vertices.remove(this.vertices.size()-1);
		return totalInterseccoes % 2 > 0;
	}
	
	// analisa se as coordenadas x e y pertencem a reta do ponto de origem e destino
	private boolean percenceReta(double x, double y, Ponto4D origem, Ponto4D destino) {
		if ((origem.obterX() == destino.obterX()) ||
		    (origem.obterY() == destino.obterY()))
			return (origem.obterX() == x || origem.obterY() == y);

		double ladoEsquerdo = y - origem.obterY();
		double ladoDireto = (destino.obterY() - origem.obterY())/(destino.obterX() - origem.obterX()) * (x - origem.obterX());
		return ladoEsquerdo <= ladoDireto+0.3 && ladoEsquerdo >= ladoDireto-0.3; 
	}
	
	// analisa se as coordenads x e y fazem intersecção com o ponto de origem e destino
	private boolean existeIntersecao(double x, double y, Ponto4D origem, Ponto4D destino) {
		double equacao = (y - origem.obterY())/(destino.obterY() - origem.obterY());
		double interseccaoX = origem.obterX() + (destino.obterX() - origem.obterX()) * equacao;
		return interseccaoX >= x ? Math.abs(equacao - 0.5) <= 0.5 : false; 
	}
	
	// desenha a cor
	public void desenhaCor() {
		switch (this.cor) {
			case 0:
				this.gl.glColor3d(0, 0, 0);
				break;
				
			case 1:
				this.gl.glColor3d(1, 1, 0);
				break;
	
			case 2:
				gl.glColor3d(0, 0, 1);
				break;
				
			case 3:
				this.gl.glColor3d(1, 0, 0);
				break;
		}
	}
	
	// altera a cor
	public void alteraCor() {
		this.cor++;
		if (this.cor == 3)
			this.cor = 0;
	}
	
	// obtém o ponto do centro da bbox
	public Ponto4D obtemCentro() {
		return this.bBox.obterCentro();
	}
	
	// adiciona um polígno como filho
	public void adicionaFilho(ObjetoGrafico filho) {
		this.filhos.add(filho);
	}
	
	// retorna a lista de polígnos filhos
	public List<ObjetoGrafico> obterFilhos() {
		return this.filhos;
	}
	
	public void atualizaRastro(double x, double y) {
		this.rastroX = x;
		this.rastroY = y;
	}
	
	public void desenhaRastro() {
		if (!this.fechado()) {
			this.desenhaCor();
			this.gl.glLineWidth(2);
			this.gl.glBegin(this.primitiva);
				this.gl.glVertex2d(this.verticeSelecionado.obterX(), this.verticeSelecionado.obterY());
				this.gl.glVertex2d(this.rastroX, this.rastroY);
			this.gl.glEnd();
		}
	}
	
	public boolean fechado() {
		return this.primitiva == GL.GL_LINE_LOOP;
	}
}
