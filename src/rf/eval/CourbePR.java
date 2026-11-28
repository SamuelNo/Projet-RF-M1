package rf.eval;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

// courbe precision/rappel a partir des scores par classe d'un classifieur
// pour chaque classe c on trie les images testees par score de c decroissant,
// puis on baisse le seuil petit a petit : a chaque seuil on calcule precision et rappel
// marche pour le knn (scores = proportions des k voisins) comme pour le vote (scores combines)
public class CourbePR {

    public static final int NB_NIVEAUX = 11;

    private final int nbClasses;
    private final List<Integer> vraies = new ArrayList<>();
    private final List<double[]> scores = new ArrayList<>();

    public CourbePR(int nbClasses) {
        this.nbClasses = nbClasses;
    }

    public void ajouter(int vraie, double[] s) {
        vraies.add(vraie);
        scores.add(s.clone());
    }

    // points (rappel, precision) pour la classe c, un point par valeur de score distincte
    // les ex aequo sont traites ensemble, sinon l'ordre arbitraire changerait la courbe
    public List<double[]> points(int c) {
        int n = vraies.size();
        List<Integer> ordre = new ArrayList<>();
        for (int i = 0; i < n; i++) ordre.add(i);
        ordre.sort(Comparator.comparingDouble((Integer i) -> scores.get(i)[c - 1]).reversed());

        int positifs = 0;
        for (int v : vraies) if (v == c) positifs++;

        List<double[]> pts = new ArrayList<>();
        int vp = 0, fp = 0, i = 0;
        while (i < n) {
            double s = scores.get(ordre.get(i))[c - 1];
            while (i < n && scores.get(ordre.get(i))[c - 1] == s) {
                if (vraies.get(ordre.get(i)) == c) vp++;
                else fp++;
                i++;
            }
            pts.add(new double[]{(double) vp / positifs, (double) vp / (vp + fp)});
        }
        return pts;
    }

    // aire sous la courbe de precision interpolee (meme definition que courbeMoyenne mais sur tous les points)
    // pas de trapezes : si le premier seuil ne contient que des erreurs la precision part de 0
    // et les trapezes ecrasent l'aire alors que la classe est bien retrouvee juste apres
    public double auc(int c) {
        List<double[]> pts = points(c);
        double[] interp = new double[pts.size()];
        double best = 0;
        for (int i = pts.size() - 1; i >= 0; i--) {
            best = Math.max(best, pts.get(i)[1]);
            interp[i] = best;
        }
        double aire = 0, r0 = 0;
        for (int i = 0; i < pts.size(); i++) {
            aire += (pts.get(i)[0] - r0) * interp[i];
            r0 = pts.get(i)[0];
        }
        return aire;
    }

    public double aucMoyenne() {
        double s = 0;
        for (int c = 1; c <= nbClasses; c++) s += auc(c);
        return s / nbClasses;
    }

    // precision interpolee sur 11 niveaux de rappel (0, 0.1, ..., 1) puis moyenne sur les classes
    // interpolee = meilleure precision obtenue pour un rappel au moins egal au niveau
    // ca donne une seule courbe par methode, plus facile a comparer
    public double[] courbeMoyenne() {
        double[] moy = new double[NB_NIVEAUX];
        for (int c = 1; c <= nbClasses; c++) {
            List<double[]> pts = points(c);
            for (int j = 0; j < NB_NIVEAUX; j++) {
                double niveau = j / (double) (NB_NIVEAUX - 1);
                double best = 0;
                for (double[] pt : pts) {
                    if (pt[0] >= niveau - 1e-9) best = Math.max(best, pt[1]);
                }
                moy[j] += best / nbClasses;
            }
        }
        return moy;
    }

    // ecrit plusieurs courbes moyennes cote a cote dans un csv (une colonne par methode)
    public static void ecrireCsv(String fichier, List<String> noms, List<CourbePR> courbes) throws IOException {
        Path p = Paths.get(fichier);
        if (p.getParent() != null) Files.createDirectories(p.getParent());
        List<double[]> moy = new ArrayList<>();
        for (CourbePR c : courbes) moy.add(c.courbeMoyenne());

        try (PrintWriter w = new PrintWriter(Files.newBufferedWriter(p))) {
            w.println("rappel;" + String.join(";", noms));
            for (int j = 0; j < NB_NIVEAUX; j++) {
                StringBuilder sb = new StringBuilder(String.format(java.util.Locale.US, "%.1f", j / 10.0));
                for (double[] m : moy) sb.append(String.format(java.util.Locale.US, ";%.4f", m[j]));
                w.println(sb);
            }
        }
    }
}
