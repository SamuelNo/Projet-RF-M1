package rf.outils;

import java.util.ArrayList;
import java.util.List;

import rf.donnees.Echantillon;

// centrage-reduction, moyenne et ecart type calcules sur l'apprentissage seulement
// si on les calcule sur toute la base le test "fuit" dans l'apprentissage et les resultats sont faux
// utile surtout pour zernike ou les valeurs montent a plusieurs milliers alors que le reste est entre 0 et 1
public class Normalisation {

    private double[] moy;
    private double[] ecart;

    public void ajuster(List<Echantillon> app) {
        int d = app.get(0).dimension();
        moy = new double[d];
        ecart = new double[d];
        for (Echantillon e : app) {
            for (int j = 0; j < d; j++) moy[j] += e.getValeurs()[j];
        }
        for (int j = 0; j < d; j++) moy[j] /= app.size();
        for (Echantillon e : app) {
            for (int j = 0; j < d; j++) {
                double x = e.getValeurs()[j] - moy[j];
                ecart[j] += x * x;
            }
        }
        for (int j = 0; j < d; j++) {
            ecart[j] = Math.sqrt(ecart[j] / app.size());
            if (ecart[j] < 1e-12) ecart[j] = 1; // composante constante
        }
    }

    // x' = (x - moyenne) / ecart type, composante par composante
    public List<Echantillon> appliquer(List<Echantillon> liste) {
        List<Echantillon> res = new ArrayList<>();
        for (Echantillon e : liste) {
            double[] v = new double[e.dimension()];
            for (int j = 0; j < v.length; j++) {
                v[j] = (e.getValeurs()[j] - moy[j]) / ecart[j];
            }
            res.add(e.avecValeurs(v));
        }
        return res;
    }
}
