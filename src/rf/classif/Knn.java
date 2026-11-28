package rf.classif;

import java.util.ArrayList;
import java.util.List;

import rf.donnees.Echantillon;
import rf.outils.Distance;

// k plus proches voisins
// pas de vrai apprentissage, on garde juste la base et tout se fait au moment de predire
public class Knn {

    private final int k;
    private final Distance distance;
    private List<Echantillon> app;

    public Knn(int k, Distance distance) {
        this.k = k;
        this.distance = distance;
    }

    public void entrainer(List<Echantillon> app) {
        this.app = app;
    }

    // renvoie les k plus proches, du plus pres au plus loin
    // on garde une liste triee de taille k plutot que de trier toute la base a chaque fois
    public List<Echantillon> voisins(Echantillon x) {
        int n = Math.min(k, app.size());
        List<Echantillon> res = new ArrayList<>();
        List<Double> dists = new ArrayList<>();
        for (Echantillon e : app) {
            double d = distance.calculer(x.getValeurs(), e.getValeurs());
            // position ou inserer d pour que la liste reste triee
            int pos = 0;
            while (pos < dists.size() && dists.get(pos) <= d) pos++;
            // si d est plus grand que les k distances deja gardees, on ne garde pas e
            if (pos < n) {
                dists.add(pos, d);
                res.add(pos, e);
                if (res.size() > n) {
                    res.remove(n);
                    dists.remove(n);
                }
            }
        }
        return res;
    }

    // proportion de chaque classe parmi les k voisins (index 0 = classe 1)
    // sert aussi pour la courbe precision/rappel et pour le vote
    public double[] scores(Echantillon x, int nbClasses) {
        double[] s = new double[nbClasses];
        List<Echantillon> v = voisins(x);
        for (Echantillon e : v) s[e.getClasse() - 1]++;
        for (int c = 0; c < nbClasses; c++) s[c] /= v.size();
        return s;
    }

    // vote des k voisins, la classe la plus representee gagne
    public int predire(Echantillon x, int nbClasses) {
        List<Echantillon> v = voisins(x);
        int[] cpt = new int[nbClasses];
        for (Echantillon e : v) cpt[e.getClasse() - 1]++;

        int max = 0;
        for (int c : cpt) max = Math.max(max, c);

        // egalite : on garde la classe du voisin le plus proche parmi les ex aequo
        // v est trie donc le premier qui a le max est forcement le plus proche
        for (Echantillon e : v) {
            if (cpt[e.getClasse() - 1] == max) return e.getClasse();
        }
        return v.get(0).getClasse();
    }

    public int getK() {
        return k;
    }
}
