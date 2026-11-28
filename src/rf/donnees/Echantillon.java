package rf.donnees;

// une forme = sa classe, son numero et le vecteur lu dans le fichier
public class Echantillon {

    private final int classe;
    private final int numero;
    private final double[] valeurs;

    public Echantillon(int classe, int numero, double[] valeurs) {
        this.classe = classe;
        this.numero = numero;
        this.valeurs = valeurs;
    }

    public int getClasse() {
        return classe;
    }

    public int getNumero() {
        return numero;
    }

    public double[] getValeurs() {
        return valeurs;
    }

    public int dimension() {
        return valeurs.length;
    }

    public Echantillon avecValeurs(double[] v) {
        return new Echantillon(classe, numero, v);
    }

    @Override
    public String toString() {
        return String.format("S%02dN%03d", classe, numero);
    }
}
