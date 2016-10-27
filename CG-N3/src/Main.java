import java.awt.event.*;
import java.util.*;
import javax.media.opengl.*;
import javax.media.opengl.glu.*;
import javax.swing.*;

public class Main implements GLEventListener, KeyListener, MouseListener, MouseMotionListener  {
	private GL gl;
	private GLU glu;
	private GLAutoDrawable glDrawable;

	private enum Estados {edicao, manutencao};
	private enum Objetos {poligno, vertice};
	private ObjetoGrafico raiz;
	private ObjetoGrafico polignoSelecionado;
	private Estados estado;
	private Objetos objeto;
	
	public void init(GLAutoDrawable drawable) {
		this.gl = drawable.getGL();
		this.gl.glClearColor(1, 1, 1, 1);
		this.glDrawable = drawable;
		this.glDrawable.setGL(new DebugGL(gl));
		this.glu = new GLU();
		this.estado = Estados.edicao;
		this.objeto = Objetos.poligno;
	}

	public void display(GLAutoDrawable arg0) {
		this.gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		this.glu.gluOrtho2D(0, 400, 400, 0);

		this.gl.glMatrixMode(GL.GL_MODELVIEW);
		this.gl.glLoadIdentity();

		if (this.raiz != null) {
			this.raiz.desenha();
			if (this.polignoSelecionado != null) {
				this.polignoSelecionado.desenhaBbox();
				if (this.estado == Estados.edicao)
					this.polignoSelecionado.desenhaRastro();
			}
		}
		this.gl.glFlush();
	}

	public void keyPressed(KeyEvent e) {
		switch (e.getKeyCode()) {
			case KeyEvent.VK_P:
				this.objeto = Objetos.poligno;
				System.out.println("objeto: polígno");
				break;
			case KeyEvent.VK_V:
				this.objeto = Objetos.vertice;
				System.out.println("objeto: vértice");
				break;
			case KeyEvent.VK_E:
				this.estado = Estados.edicao;
				System.out.println("status: edição");
				break;
			case KeyEvent.VK_M:
				this.estado = Estados.manutencao;
				System.out.println("status: manutenção");
				break;
		}
		
		if (this.polignoSelecionado != null)
			switch (e.getKeyCode()) {
				case KeyEvent.VK_DELETE:
					if (this.estado == Estados.edicao)
						if (this.objeto == Objetos.poligno)
							this.removePolignoSelecionado();
						else {
							this.polignoSelecionado.removeVerticeSelecionado();
							if (!this.polignoSelecionado.contemVertices())
								this.removePolignoSelecionado();
						}
					break;
				case KeyEvent.VK_RIGHT:
					this.polignoSelecionado.translacaoXYZ(2, 0, 0);
					break;
				case KeyEvent.VK_LEFT:
					this.polignoSelecionado.translacaoXYZ(-2, 0, 0);
					break;
				case KeyEvent.VK_UP:
					this.polignoSelecionado.translacaoXYZ(0, -2, 0);
					break;
				case KeyEvent.VK_DOWN:
					this.polignoSelecionado.translacaoXYZ(0, 2, 0);
					break;
				case KeyEvent.VK_1:
					this.polignoSelecionado.escalaXYZPtoFixo(0.5, this.polignoSelecionado.obtemCentro());
					break;
				case KeyEvent.VK_2:
					this.polignoSelecionado.escalaXYZPtoFixo(2.0, this.polignoSelecionado.obtemCentro());
					break;
				case KeyEvent.VK_3:
					this.polignoSelecionado.rotacaoZPtoFixo(10.0, this.polignoSelecionado.obtemCentro());
					break;
				case KeyEvent.VK_4:
					this.polignoSelecionado.rotacaoZPtoFixo(-10.0, this.polignoSelecionado.obtemCentro());
					break;
				case KeyEvent.VK_SPACE:
					this.polignoSelecionado.alteraCor();
					break;
			}
		this.glDrawable.display();
	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		this.gl.glViewport(0, 0, width, height);
		this.gl.glMatrixMode(GL.GL_PROJECTION);
		this.gl.glLoadIdentity();
	}
	
	// remove o polígno selecionado, se este tiver polígnos filhos, também são removidos da tela
	private void removePolignoSelecionado() {
		if (this.polignoSelecionado.equals(this.raiz)) {
			this.polignoSelecionado = null;
			this.raiz = null;
		}
		else {
			List<ObjetoGrafico> fila = new LinkedList<>();
			fila.add(this.raiz);
			while (!fila.isEmpty()) {
				ObjetoGrafico poligno = fila.remove(0);
				List<ObjetoGrafico> filhos = poligno.obterFilhos();
				if (filhos.contains(this.polignoSelecionado)) {
					filhos.remove(this.polignoSelecionado);
					this.polignoSelecionado = poligno;
					break;
				}
				for (short i = 0; i < filhos.size(); i++)
					fila.add(filhos.get(i));
			}
		}
	}
	
	// insere polígno como filho do polígno selecionado e atribue este como o polígno selecionado
	private void inserePoligno(double x, double y) {
		ObjetoGrafico poligno = new ObjetoGrafico();
		poligno.insereVertice(x, y);
		poligno.atribuirGL(gl);
		
		if (this.raiz == null)
			this.raiz = poligno;
		else
			this.polignoSelecionado.adicionaFilho(poligno);
		
		this.polignoSelecionado = poligno;
	}

	// selecionado polígno conforme as coordenadas x e y do ponto selecionado
	private void selecionaPoligno(double x, double y) {
		if (this.raiz != null) {
			this.polignoSelecionado = null;
			List<ObjetoGrafico> fila = new LinkedList<>();
			fila.add(this.raiz);
			while (!fila.isEmpty()) {
				ObjetoGrafico poligno = fila.remove(0);
				if (poligno.analisaSelecao(x, y)) {
					this.polignoSelecionado = poligno;
					break;
				}
				List<ObjetoGrafico> filhos = poligno.obterFilhos();
				for (short i = 0; i < filhos.size(); i++)
					fila.add(filhos.get(i));
			}
		}
	}
	
	public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
        	if (this.estado == Estados.manutencao) {
        		this.selecionaPoligno(e.getX(), e.getY());
        		if (this.polignoSelecionado != null)
        			this.polignoSelecionado.selecionaVertice(e.getX(), e.getY());
	        }
        	else {
        		if (this.polignoSelecionado != null && !this.polignoSelecionado.fechado())
    				this.polignoSelecionado.insereVertice(e.getX(), e.getY());
        		else
        			this.inserePoligno(e.getX(), e.getY());
        		
        		this.polignoSelecionado.atualizaRastro(e.getX(), e.getY());
        	}
        }
        else
        	if (this.polignoSelecionado != null)
        		this.polignoSelecionado.fecha();
        
        this.glDrawable.display();
	}
	
	public void mouseDragged(MouseEvent e) {
		if (this.polignoSelecionado != null)
			this.polignoSelecionado.moveVerticeSelecionado(e.getX(), e.getY());
        
		this.glDrawable.display();
	}
	
	public void mouseMoved(MouseEvent e) {
		if (this.polignoSelecionado != null) {
			this.polignoSelecionado.atualizaRastro(e.getX(), e.getY());
			this.glDrawable.display();
		}
	}
	
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}
	public void keyReleased(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}
	public void mouseClicked(MouseEvent arg0) {}
	public void mouseEntered(MouseEvent arg0) {}
	public void mouseExited(MouseEvent arg0) {}
	public void mouseReleased(MouseEvent arg0) {}
}