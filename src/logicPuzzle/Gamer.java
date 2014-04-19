package logicPuzzle;

//classe che identifica il giocatore 
public class Gamer {

	private String Name;
	private int punteggio;

	public Gamer(String n, int p) {
		this.punteggio = p;
		this.Name = n;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((Name == null) ? 0 : Name.hashCode());
		result = prime * result + punteggio;
		return result;
	}

	public String getName() {
		return Name;
	}

	public int getPunteggio() {
		return punteggio;
	}

	public void setPunteggio(int punteggio) {
		this.punteggio = punteggio;
	}

	@Override
	public String toString() {
		return "Gamer [Name=" + Name + ", punteggio=" + punteggio + "]";
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Gamer other = (Gamer) obj;
		if (Name == null) {
			if (other.Name != null)
				return false;
		} else if (!Name.equals(other.Name))
			return false;
		if (punteggio != other.punteggio)
			return false;
		return true;
	}

}
