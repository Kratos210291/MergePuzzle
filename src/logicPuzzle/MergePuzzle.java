package logicPuzzle;
import java.awt.EventQueue;

import javax.swing.*;


public class MergePuzzle extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MergePuzzle() {
		this.setTitle("MergePuzzle");
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setSize(590, 620);
		this.setResizable(false);

		this.add(new Puzzle("src/sound",null));

		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}
	public static void main(String[]args){
		EventQueue.invokeLater( new Runnable(){
			public void run(){
				new MergePuzzle();
			}
		});
	}

}
