import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.media.opengl.DebugGL;
import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

public class Main implements GLEventListener, KeyListener, MouseListener, MouseMotionListener {
	private GL gl;
	private GLU glu;
	private GLAutoDrawable glDrawable;

	private enum Estados {edicao, manutencao};
	private Estados estadoAtual;
	
	private double origemX = 10, origemY = 10;
	
	private ObjetoGrafico polignoSelecionado;
	
	private List<ObjetoGrafico> objetos;//	= { 
//			new ObjetoGrafico(),
//			new ObjetoGrafico() };
	
	// "render" feito logo apos a inicializacao do contexto OpenGL.
	public void init(GLAutoDrawable drawable) {
		glDrawable = drawable;
		gl = drawable.getGL();
		glu = new GLU();
		glDrawable.setGL(new DebugGL(gl));

		gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
		
		this.objetos = new ArrayList<ObjetoGrafico>();
		
		this.estadoAtual = Estados.edicao;

//		for (byte i=0; i < objetos.length; i++) {
//			objetos[i].atribuirGL(gl);
//		}
	}

	// metodo definido na interface GLEventListener.
	// "render" feito pelo cliente OpenGL.
	public void display(GLAutoDrawable arg0) {
		gl.glClear(GL.GL_COLOR_BUFFER_BIT);
		glu.gluOrtho2D(-30.0f, 30.0f, -30.0f, 30.0f);

		gl.glMatrixMode(GL.GL_MODELVIEW);
		gl.glLoadIdentity();

		gl.glLineWidth(1.0f);
		gl.glPointSize(1.0f);

//		sru();
		
//		for (int i = 0; i < objetos.length; i++) {
//			objetos[i].desenha();
//		}
		
		if (this.polignoSelecionado != null) {
			this.polignoSelecionado.atribuirGL(this.gl);
			this.polignoSelecionado.desenha();
		}
		
		gl.glFlush();
	}

	public void sru() {
		gl.glColor3f(1.0f, 0.0f, 0.0f);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(-20.0f, 0.0f);
			gl.glVertex2f(20.0f, 0.0f);
		gl.glEnd();
		gl.glColor3f(0.0f, 1.0f, 0.0f);
		gl.glBegin(GL.GL_LINES);
			gl.glVertex2f(0.0f, -20.0f);
			gl.glVertex2f(0.0f, 20.0f);
		gl.glEnd();
	}
	
	public void keyPressed(KeyEvent e) {

		switch (e.getKeyCode()) {
		case KeyEvent.VK_P:
//			objetos[0].exibeVertices();
			break;
		case KeyEvent.VK_M:
//			objetos[0].exibeMatriz();
			break;

		case KeyEvent.VK_R:
//			objetos[0].atribuirIdentidade();
			break;

		case KeyEvent.VK_RIGHT:
//			objetos[0].translacaoXYZ(2.0,0.0,0.0);
			break;
		case KeyEvent.VK_LEFT:
//			objetos[0].translacaoXYZ(-2.0,0.0,0.0);
			break;
		case KeyEvent.VK_UP:
//			objetos[0].translacaoXYZ(0.0,2.0,0.0);
			break;
		case KeyEvent.VK_DOWN:
//			objetos[0].translacaoXYZ(0.0,-2.0,0.0);
			break;

		case KeyEvent.VK_PAGE_UP:
//			objetos[0].escalaXYZ(2.0,2.0);
			break;
		case KeyEvent.VK_PAGE_DOWN:
//			objetos[0].escalaXYZ(0.5,0.5);
			break;

		case KeyEvent.VK_HOME:
//			objetos[0].rotacaoZ(-10);
			break;

		case KeyEvent.VK_1:
//			objetos[0].escalaXYZPtoFixo(0.5, new Ponto4D(-15.0,-15.0,0.0,0.0));
			this.estadoAtual = Estados.edicao;
			System.out.println("Estado de edição");
			break;
			
		case KeyEvent.VK_2:
//			objetos[0].escalaXYZPtoFixo(2.0, new Ponto4D(-15.0,-15.0,0.0,0.0));
			this.estadoAtual = Estados.manutencao;
			System.out.println("Estado de manutenção");
			break;
			
			case KeyEvent.VK_3:
//				objetos[0].rotacaoZPtoFixo(10.0, new Ponto4D(-15.0,-15.0,0.0,0.0));
				break;
		}

		glDrawable.display();
	}

	// metodo definido na interface GLEventListener.
	// "render" feito depois que a janela foi redimensionada.
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	    gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL.GL_PROJECTION);
        gl.glLoadIdentity();
		// System.out.println(" --- reshape ---");
	}

	// metodo definido na interface GLEventListener.
	// "render" feito quando o modo ou dispositivo de exibicao associado foi
	// alterado.

	public void mousePressed(MouseEvent e) {
		this.origemX = e.getX();
        this.origemY = e.getY();
        
        System.out.println(e.getX());
        System.out.println(e.getY());
        
        if (this.estadoAtual == Estados.edicao)
        	if (this.polignoSelecionado == null) {
        		ObjetoGrafico poligno = new ObjetoGrafico(this.obterCoordenada(this.origemX), 
        												  this.obterCoordenada(this.origemY));
        		this.polignoSelecionado = poligno;
        		System.out.println("Poligno criado");
        	}
        	else {
        		System.out.println("Vértice criado");
        		this.polignoSelecionado.adicionaVertice(this.obterCoordenada(this.origemX), 
        												this.obterCoordenada(this.origemY));
        	}	
        this.glDrawable.display();
	}
	
	public void mouseDragged(MouseEvent e) {
		if (this.estadoAtual == Estados.manutencao) {
			this.polignoSelecionado.atribuiXYVertice(this.obterCoordenada(e.getX() - this.origemX), 
													 this.obterCoordenada(e.getY() - this.origemY));
			this.glDrawable.display();
		}
		
		this.origemX = e.getX();
		this.origemY = e.getY();
	}
	
	private double obterCoordenada(double ponto) {
		return Math.round((ponto - 191) / 6.4);
	}
	
	public void displayChanged(GLAutoDrawable arg0, boolean arg1, boolean arg2) {}

	public void mouseReleased(MouseEvent e) {}

	public void mouseMoved(MouseEvent arg0) {}

	public void mouseClicked(MouseEvent arg0) {}

	public void mouseEntered(MouseEvent arg0) {}

	public void mouseExited(MouseEvent e) {}
	
	public void keyReleased(KeyEvent arg0) {}

	public void keyTyped(KeyEvent arg0) {}
}
