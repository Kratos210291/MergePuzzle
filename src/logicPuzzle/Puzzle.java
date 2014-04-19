package logicPuzzle;

import java.awt.*;

import javax.swing.*;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Puzzle extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static int r = 180;
	private static int g = 180;
	private static int b = 180;
	private static Color BG_COLOR = new Color(r, g, b, 50);
	private static final String FONT_NAME = "Comic Sans";
	private static final int Nodo_SIZE = 90;
	private static final int NodoS_MARGIN = 25;
	private static final int MOSSE_IN = 45;
	private PlayWave player;
	private Nodo[] nodi;
	boolean vittoria = false;
	boolean hoperso = false;
	private int bestPunteggio;
	private int punteggio = 0, punteggioParziale = 0;
	private int ourTarget = 64;
	private int livello = 2;
	private static String path;
	private boolean voglioSuoni;
	private boolean voglioInfo;
	private boolean voglioPausa;
	private boolean successivaInfo;
	private int contaMosse;
	private int mosse;
	private Timer timer;
	private int time = 0;
	private Gamer giocatore;

	/******** inner class che rappresenta la casella ************/
	private static class Nodo {

		int info;
		boolean bonus = false;

		public Nodo() {
			this(0);
		}

		public Nodo(int num) {
			info = num;
		}

		public boolean isEmpty() {
			return info == 0;
		}


		public Color getBackground() {
			if (!bonus)
				switch (info) {
				case 1:
					return Color.BLUE;
				case 2:
					return new Color(0xeee4da);
				case 4:
					return new Color(0xede0c8);
				case 8:
					return new Color(0xf2b179);
				case 16:
					return new Color(0xf59563);
				case 32:
					return new Color(0xf67c5f);
				case 64:
					return new Color(0xf65e3b);
				case 128:
					return new Color(0xedcf72);
				case 256:
					return new Color(0xedcc61);
				case 512:
					return new Color(0xedc850);
				case 1024:
					return new Color(0xedc53f);
				case 2048:
					return new Color(0xedc22e);
				}
			else {
				// colore del bonus
				return new Color(253, 224, 0);

			}
			return new Color(220, 220, 220, 80);

		}
	}

	/********* costruttore della classe Puzzle ******************/
	public Puzzle(String path, Gamer giocatore) {
		this.giocatore = giocatore;
		this.path = path;
		timer = new Timer(800, this);
		timer.start();
		voglioPausa = false;
		voglioSuoni = true;
		voglioInfo = false;
		successivaInfo = false;
		mosse=MOSSE_IN;
		contaMosse = 0;
		this.bestPunteggio=loadMigliorPunteggio();
		setFocusable(true);
		if (voglioSuoni) {
			player = new PlayWave(path + "/slide.wav");
			player.start();
		}

		// listener
		addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {

				if (e.getKeyCode() == KeyEvent.VK_ENTER
						|| e.getKeyCode() == KeyEvent.VK_P) {
					pausa();
				}
				if (e.getKeyCode() == KeyEvent.VK_ALT
						||e.getKeyCode() == KeyEvent.VK_I) {
					if (!voglioInfo) {
						voglioInfo = true;
					} else {
						voglioInfo = false;
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_SPACE) {
					if (voglioSuoni) {
						voglioSuoni = false;
					} else {
						voglioSuoni = true;
					}
				}
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					resetGame();
				}
				if (!possoMuovere()) {
					contaMosse = 0;
					ourTarget = 64;
					livello = 2;
					mosse = MOSSE_IN;
					hoperso = true;
				}
				if (voglioInfo) {
					if (e.getKeyCode() == KeyEvent.VK_LEFT) {
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/slide.wav");
							player.start();
						}
						successivaInfo = false;

					}
					if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/slide.wav");
							player.start();
						}
						successivaInfo = true;

					}
				}
				if (!vittoria && !hoperso && !voglioPausa && !voglioInfo) {
					switch (e.getKeyCode()) {
					case KeyEvent.VK_LEFT:
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/button1.wav");
							player.start();
						}
						sinistra();
						contaMosse++;
						break;
					case KeyEvent.VK_RIGHT:
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/button1.wav");
							player.start();
						}
						destra();
						contaMosse++;
						break;
					case KeyEvent.VK_DOWN:
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/button1.wav");
							player.start();
						}
						down();
						contaMosse++;
						break;
					case KeyEvent.VK_UP:
						if (voglioSuoni) {
							player = new PlayWave(Puzzle.path + "/button1.wav");
							player.start();
						}
						up();
						contaMosse++;
						break;
					}
				}
				// controllo numero di mosse
				if (mosse - contaMosse <= 0)
					punteggio -= 20;

				repaint();
			}
		});
		resetGame();
	}

	/*
	 * metodo che resetta le condizioni di gioco funziona
	 */
	public void resetGame() {
		if (voglioSuoni) {
			player = new PlayWave(path + "/slide.wav");
			player.start();
		}
		// nel caso in cui resetto a causa di partita fallita
		if (hoperso) {
			salvaPunteggio();
			punteggio = 0;
			punteggioParziale = punteggio;
			livello = 2;
			hoperso = false;
			timer.start();
			

		}
		// caso in cui resetto dal gioco
		if (!hoperso || !vittoria) {
			contaMosse = 0;
			punteggio = punteggioParziale;
			salvaPunteggio();

		}
		// caso in cui avanzo di livello
        salvaPunteggio();
		vittoria = false;
		hoperso = false;
		nodi = new Nodo[4 * 4];
		for (int i = 0; i < nodi.length; i++) {
			nodi[i] = new Nodo();
		}
		// quanti nodi inserire nella tabella vuota
		for (int i = 0; i < livello; i++)
			addNodo();

	}

	// programmo solo la mossa in una direzione
	public void sinistra() {
		boolean bisognaInserireNodo = false;
		for (int i = 0; i < 4; i++) {
			Nodo[] linea = getLine(i);
			Nodo[] nodiFusi = fondiLinea(muoviLinea(linea));
			setLine(i, nodiFusi);
			if (!bisognaInserireNodo && !compare(linea, nodiFusi)) {
				bisognaInserireNodo = true;
			}
		}

		if (bisognaInserireNodo) {
			addNodo();
		}
	}

	/*
	 * per fondere a sinistra i nodi mi mì basta ruotare la tabella e applicare
	 * la fusione a sinistra
	 */

	public void destra() {
		nodi = ruota(180);// ruoto di 180 gradi
		sinistra();// applico la fusione sinistra
		nodi = ruota(180);// riposiziono la tabella nel verso giusto
	}

	public void up() {
		nodi = ruota(270);
		sinistra();
		nodi = ruota(90);
	}

	public void down() {
		nodi = ruota(90);
		sinistra();
		nodi = ruota(270);
	}

	// pausa
	private void pausa() {
		if (!voglioPausa)
			voglioPausa = true;
		else
			voglioPausa = false;
	}

	private Nodo NodoAt(int x, int y) {
		return nodi[x + y * 4];
	}
    //aggiungo nodo in posizione casuale
	private void addNodo() {
		List<Nodo> list = availableSpace();
		Random rnd = new Random();
		if (!availableSpace().isEmpty()) {
			int index = (int) (Math.random() * list.size()) % list.size();
			Nodo emptyTime = list.get(index);

			if (rnd.nextInt(5)==1||rnd.nextInt(5)==5)
				emptyTime.info = 4;
			else
				emptyTime.info = 2;

			if (rnd.nextInt(livello + 5) == 0) {
				emptyTime.bonus = true;
				/*
				 * if (voglioSuoni) { player = new PlayWave(path +
				 * "/button.wav"); player.start(); }
				 */
			}
		}
	}

	private List<Nodo> availableSpace() {
		final List<Nodo> list = new ArrayList<Nodo>(16);
		for (Nodo t : nodi) {
			if (t.isEmpty()) {
				list.add(t);
			}
		}
		return list;
	}

	private boolean isFull() {
		return availableSpace().size() == 0;
	}

	// verifica il movimento
	private boolean possoMuovere() {
		if (!isFull()) {
			return true;
		}
		for (int x = 0; x < 3; x++) {
			for (int y = 0; y < 3; y++) {
				Nodo t = NodoAt(x, y);
				if (t.info == NodoAt(x + 1, y).info
						|| t.info == NodoAt(x, y + 1).info) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean compare(Nodo[] linea1, Nodo[] linea2) {
		if (linea1 == linea2) {
			return true;
		} else if (linea1.length != linea2.length) {
			return false;
		}

		for (int i = 0; i < linea1.length; i++) {
			if (!linea1[i].equals(linea2[i]))
				return false;
		}
		return true;
	}

	private Nodo[] ruota(int angolo) {
		Nodo[] nuovoNodo = new Nodo[4 * 4];
		int spostaX = 3, spostaY = 3;
		if (angolo == 90) {
			spostaY = 0;
		} else if (angolo == 270) {
			spostaX = 0;
		}

		double rad = Math.toRadians(angolo);
		int cos = (int) Math.cos(rad);
		int sin = (int) Math.sin(rad);
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				int newX = (x * cos) - (y * sin) + spostaX;
				int newY = (x * sin) + (y * cos) + spostaY;
				nuovoNodo[(newX) + (newY) * 4] = NodoAt(x, y);
			}
		}
		return nuovoNodo;
	}

	private Nodo[] muoviLinea(Nodo[] vecchiaLinea) {
		LinkedList<Nodo> l = new LinkedList<Nodo>();
		for (int i = 0; i < 4; i++) {
			if (!vecchiaLinea[i].isEmpty())
				l.addLast(vecchiaLinea[i]);
		}
		if (l.size() == 0) {
			return vecchiaLinea;
		} else {
			Nodo[] newLine = new Nodo[4];
			ensureSize(l, 4);
			for (int i = 0; i < 4; i++) {
				newLine[i] = l.removeFirst();
			}
			return newLine;
		}
	}

	private Nodo[] fondiLinea(Nodo[] oldLine) {
		LinkedList<Nodo> list = new LinkedList<Nodo>();
		for (int i = 0; i < 4 && !oldLine[i].isEmpty(); i++) {
			int num = oldLine[i].info;

			if (i < 3 && (oldLine[i].info == oldLine[i + 1].info)) {
				if (oldLine[i].bonus || oldLine[i + 1].bonus) {
					num *= 4;
				} else {
					if (voglioSuoni) {
						player = new PlayWave(path + "/monete.wav");
						player.start();
					}
					num *= 2;
				}
				if (num >= 64) {
					if (voglioSuoni) {
						player = new PlayWave(path + "/64.wav");
						player.start();
					}
				}
				punteggio += num;
				if (punteggio > bestPunteggio)
					bestPunteggio = punteggio;
				// vittoria
				if (num == ourTarget) {
					if (voglioSuoni) {
						player = new PlayWave(path + "/win.wav");
						player.start();
					}
					vittoria = true;
					contaMosse = 0;
					mosse += 20;
					livello++;
					punteggioParziale = punteggio;
					ourTarget *= 2;

				}
				i++;
			}
			list.add(new Nodo(num));
		}
		if (list.size() == 0) {
			return oldLine;
		} else {
			ensureSize(list, 4);
			return list.toArray(new Nodo[4]);
		}
	}

	private static void ensureSize(java.util.List<Nodo> l, int s) {
		while (l.size() != s) {
			l.add(new Nodo());
		}
	}

	private Nodo[] getLine(int index) {
		Nodo[] risultato = new Nodo[4];
		for (int i = 0; i < 4; i++) {
			risultato[i] = NodoAt(i, index);
		}
		return risultato;
	}

	private void setLine(int indice, Nodo[] re) {
		System.arraycopy(re, 0, nodi, indice * 4, 4);
	}

	/********** GRAFICA DI GIOCO *************/
	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = ((Graphics2D) g);

		disegnaSfondo(g2);
		disegnaElementiOpt(g2);

		// disegno nodi
		if (!voglioInfo)
			for (int y = 0; y < 4; y++) {
				for (int x = 0; x < 4; x++) {
					disegnaNodo(g2, nodi[x + y * 4], x, y);
				}
			}
	}

	private void disegnaElementiOpt(Graphics2D g2) {
		// icona muto
		URL muto = getClass().getResource("mute.png");
		Image imgMuto = new ImageIcon(muto).getImage();
		if (!voglioSuoni)
			g2.drawImage(imgMuto, 490, 10, 50, 50, this);
		// icona pausa
		URL pausa = getClass().getResource("pausa.png");
		Image imgPausa = new ImageIcon(pausa).getImage();
		if (voglioPausa)
			g2.drawImage(imgPausa, 490, 60, 50, 50, this);
		// info
		if (voglioInfo) {

			g2.setColor(new Color(0, 100, 225, 70));
			g2.fillRoundRect(15, 6, this.getSize().width - 70,
					this.getSize().height - 40, 75, 30);
			g2.setColor(Color.white);
			g2.setFont(new Font("Comic Sans", 22, 25));
			if (successivaInfo) {
				// iconaSinistra
				URL sin = getClass().getResource("sinistra.png");
				Image imgSin = new ImageIcon(sin).getImage();
				g2.drawImage(imgSin, 20, this.getSize().height / 2 - 20, 50,
						50, this);
				URL sch = getClass().getResource("Schermata2.png");
				Image imgSch = new ImageIcon(sch).getImage();
				g2.drawImage(imgSch, 70, this.getSize().height / 2 - 250, 420,
						400, this);

			} else {
				URL destro = getClass().getResource("destra.png");
				Image imgDestro = new ImageIcon(destro).getImage();
				URL sch = getClass().getResource("Schermata.png");
				Image imgSch = new ImageIcon(sch).getImage();
				g2.drawImage(imgSch, 20, this.getSize().height / 2 - 250, 520,
						350, this);
				g2.drawImage(imgDestro, 490, this.getSize().height / 2 - 10,
						50, 50, this);
				g2.setFont(new Font("Comic Sans", 20, 15));
				g2.drawString("premi SPAZIO per disatt/attivare audio ", 20,
						getHeight() - 180);
				g2.drawString("premi INVIO per fermare il gioco ", 20,
						getHeight() - 150);
			}
			g2.setFont(new Font("Comic Sans", 20, 12));
			g2.drawString("Realizzato da: De Vita Salvatore", 23, 495);
		}
		g2.setColor(Color.white);
		g2.setFont(new Font("Comic Sans", 15, 12));
		g2.drawString("Premi 'I' per Info  ", 20, getHeight() - 10);
		g2.drawString("Muovi con  ", this.getWidth()-250, getHeight() - 10);
		URL freccia = getClass().getResource("frecce.png");
		Image imgFreccia = new ImageIcon(freccia).getImage();
		g2.drawImage(imgFreccia, this.getWidth()-180, this.getSize().height- 25, 100,
				20, this);
	}

	private void disegnaSfondo(Graphics2D g2) {
		g2.setStroke(new BasicStroke(0.7f));
		// disegno sfondo
		URL sfondo = getClass().getResource("back.jpg");
		Image img = new ImageIcon(sfondo).getImage();
		g2.drawImage(img, 0, 0, this.getSize().width, this.getSize().height,
				this);
		// Colore ombra
		GradientPaint redtowhite = new GradientPaint(15, 6, new Color(250, 250,
				250, 150), this.getSize().width - 65,
				this.getSize().height - 39, new Color(150, 100, 90, 150), true);
		// disegno ombra
		g2.setPaint(redtowhite);
		g2.fillRoundRect(15, 6, this.getSize().width - 65,
				this.getSize().height - 39, 75, 30);
		g2.setColor(BG_COLOR);
		g2.fillRoundRect(15, 6, this.getSize().width - 70,
				this.getSize().height - 40, 75, 30);
		g2.setColor(new Color(0, 0, 255, 150));
		g2.fillRoundRect(20, 470, 505, 35, 20, 20);
		// icona allerta mosse Finite!!
		if ((mosse - contaMosse) < 0 && time % 2 == 0) {
			URL alert = getClass().getResource("alert.png");
			Image imgAlert = new ImageIcon(alert).getImage();
			g2.drawImage(imgAlert, 100, 505, 40, 40, this);
		}
	}

	private void disegnaNodo(Graphics g2, Nodo Nodo, int x, int y) {
		Graphics2D g = ((Graphics2D) g2);
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL,
				RenderingHints.VALUE_STROKE_NORMALIZE);

		g.setColor(Color.black);

		g.drawRoundRect(15, 6, this.getSize().width - 70,
				this.getSize().height - 40, 75, 30);
		g.drawRoundRect(15, 6, this.getSize().width - 65,
				this.getSize().height - 39, 75, 30);
		int value = Nodo.info;
		int xOffset = offsetCoors(x) + 20;
		int yOffset = offsetCoors(y);

		// imposto colore ombra
		g.setColor(Color.darkGray);
		// disegno ombra
		g.fillRoundRect(xOffset, yOffset, Nodo_SIZE + 5, Nodo_SIZE + 5, 80, 40);
		// coloro nodo
		g.setColor(Nodo.getBackground());
		g.fillRoundRect(xOffset, yOffset, Nodo_SIZE, Nodo_SIZE, 80, 40);

		g.setColor(Color.BLACK);
		g.drawRoundRect(xOffset, yOffset, Nodo_SIZE, Nodo_SIZE, 80, 40);
		g.drawRoundRect(xOffset, yOffset, Nodo_SIZE + 5, Nodo_SIZE + 5, 80, 40);
		final int size = value < 100 ? 36 : value < 1000 ? 32 : 24;
		final Font font = new Font(FONT_NAME, Font.BOLD, size);
		g.setFont(font);
		String s;
		if (!Nodo.bonus) {
			s = String.valueOf(value);
		} else {
			s = String.valueOf(value) + "$";
		}

		final FontMetrics fm = getFontMetrics(font);
		final int w = fm.stringWidth(s);
		final int h = -(int) fm.getLineMetrics(s, g).getBaselineOffsets()[2];

		if (value != 0) {
			g.drawString(s, xOffset + (Nodo_SIZE - w) / 2, yOffset + Nodo_SIZE
					- (Nodo_SIZE - h) / 2 - 2);

		}

		if (vittoria || hoperso) {
			g.setColor(new Color(0, 100, 225, 70));
			g.fillRoundRect(15, 6, this.getSize().width - 70,
					this.getSize().height - 40, 75, 30);
			g.setColor(Color.white);
			g.setFont(new Font(FONT_NAME, Font.BOLD, 55));
			if (vittoria) {
				g.drawString("Livello Superato!", 40, 150);
				g.setFont(new Font(FONT_NAME, Font.PLAIN, 28));
				// g.setColor(new Color(128, 128, 128, 128));
				g.drawString("Ottimo ora Prova a raggiungere " + ourTarget, 20,
						getHeight() - 320);
				g.drawString("Premi Esc per Continuare", 100, getHeight() - 250);
			}
			if (hoperso) {
				if (voglioSuoni) {
					player = new PlayWave(path + "/perso.wav");
					player.start();
				}
				g.drawString("Game over!", 105, 130);
				g.drawString("Hai perso!", 120, 200);
				g.setFont(new Font(FONT_NAME, Font.PLAIN, 30));
				// g.setColor(new Color(128, 128, 128, 128));
				g.drawString("Premi Esc per uscire", 120, getHeight() - 300);
			}
			/*
			 * if(stampa){ g.drawString("Attenzione", 145, 130);
			 * g.drawString("Non Puoi fondere gli Uno", 94, 200); g.setFont(new
			 * Font(FONT_NAME, Font.PLAIN, 30)); g.setColor(new Color(128, 128,
			 * 128, 128)); g.drawString("Premi Invio per Continuare", 180,
			 * getHeight() - 300); stampa=false; }
			 */
		}
		g.setFont(new Font(FONT_NAME, Font.PLAIN, 25));

		g.setColor(new Color(230, 230, 0));
		g.drawString("Punteggio: " + punteggio, 300, 495);

		g.drawString("Raggiungi " + ourTarget, 30, 495);
		g.setColor(new Color(230, 0, 0));
		g.drawString("Best " + bestPunteggio, 300, 535);
		g.setColor(Color.BLUE);
		if (mosse - contaMosse >= 0)
			g.drawString("Mosse " + (mosse - contaMosse), 30, 535);
		else {
			g.setColor(Color.RED);
			if (time % 2 == 0)
				g.drawString("-10", 30, 535);
			else {
				g.setColor(Color.blue);
				g.drawString("Punteggio", 30, 535);
			}
		}
	}
	//salvo il miglior punteggio
	private void salvaPunteggio(){
		if(punteggio>loadMigliorPunteggio()){
			try {
				DataOutputStream dos=new DataOutputStream(new FileOutputStream("punteggio.dat"));
				dos.writeInt(punteggio);
				dos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	// questo metodo carica il miglior punteggio conseguito
	private int loadMigliorPunteggio() {
		int puntMom=0;
		File f=new File("punteggio.dat");
		// se ho gia salvato il miglior punteggio
		if(f.exists()){
			try{
		DataInputStream dis=new DataInputStream(new FileInputStream(f));
		
			puntMom=dis.readInt();
			dis.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		}else{
		 //prima volta che gioco
			try {
				DataOutputStream dos=new DataOutputStream(new FileOutputStream("punteggio.dat"));
				dos.writeInt(0);
				dos.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		return puntMom;
	}

	// calcola la distanza tra un nodo e il successivo
	private static int offsetCoors(int arg) {
		return arg * (NodoS_MARGIN + Nodo_SIZE) + NodoS_MARGIN;
	}

	// main di prova
	public static void main(String[] args) {
		JFrame game = new JFrame();
		game.setTitle("MergePuzzle");
		game.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		game.setSize(600, 600);
		game.setResizable(false);

		game.add(new Puzzle("src/sound", null));

		game.setLocationRelativeTo(null);
		game.setVisible(true);
	}

	// ascoltatore del timer
	@Override
	public void actionPerformed(ActionEvent e) {
		time++;
		if (mosse - contaMosse <= 0)
			repaint();

	}

}
