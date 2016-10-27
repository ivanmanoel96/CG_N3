import java.awt.*;
import javax.media.opengl.*;
import javax.swing.*;

public class Frame extends JFrame{

	private static final long serialVersionUID = 1L;
	private Main renderer = new Main();
	
	private int janelaLargura  = 416, janelaAltura = 439;
	
	public Frame() {		
		super("CG-N3");   
		setBounds(300, 250, janelaLargura, janelaAltura);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());

		GLCapabilities glCaps = new GLCapabilities();
		glCaps.setRedBits(8);
		glCaps.setBlueBits(8);
		glCaps.setGreenBits(8);
		glCaps.setAlphaBits(8); 

		GLCanvas canvas = new GLCanvas(glCaps);
		add(canvas,BorderLayout.CENTER);
		canvas.addGLEventListener(renderer);        
		canvas.addKeyListener(renderer);
		canvas.addMouseListener(renderer);
		canvas.addMouseMotionListener(renderer);
		canvas.requestFocus();			
	}		
	
	public static void main(String[] args) {
		new Frame().setVisible(true);
	}
}
