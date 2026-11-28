package rf;

import java.util.ArrayList;
import java.util.List;

import rf.classif.Knn;
import rf.donnees.Echantillon;
import rf.donnees.LecteurBase;
import rf.eval.CourbePR;
import rf.eval.Decoupage;
import rf.eval.MatriceConfusion;
import rf.outils.Distance;
import rf.outils.Normalisation;

public class Main {

    static final long GRAINE = 42;

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            usage();
            return;
        }
        String dossier = args[0];
        String cmd = args[1];

        switch (cmd) {
            case "lire":
                lire(dossier, nbClasses(args, 2));
                break;
            case "knn":
                if (args.length < 6) {
                    usage();
                    return;
                }
                MatriceConfusion mc = evaluerKnn(dossier, args[2], Integer.parseInt(args[3]),
                        Distance.depuis(args[4]), args[5], nbClasses(args, 6), aOption(args, "--norm"));
                mc.afficher();
                break;
            case "pr":
                courbesPR(dossier, nbClasses(args, 2), aOption(args, "--norm"));
                break;
            case "etude-k":
                etudeK(dossier, args.length > 2 ? args[2] : "loo", nbClasses(args, 3), aOption(args, "--norm"));
                break;
            default:
                usage();
        }
    }

    static void usage() {
        System.out.println("java -cp bin rf.Main <dossier> lire [nbClasses]");
        System.out.println("java -cp bin rf.Main <dossier> knn <ART|ZRK|GFD|YNG|SA> <k> <l1|l2|l3|linf> <loo|holdout:0.5|kfold:4> [nbClasses] [--norm]");
        System.out.println("java -cp bin rf.Main <dossier> etude-k [protocole] [nbClasses] [--norm]");
        System.out.println("java -cp bin rf.Main <dossier> pr [nbClasses] [--norm]");
    }

    static int nbClasses(String[] args, int i) {
        if (args.length > i && !args[i].startsWith("--")) return Integer.parseInt(args[i]);
        return 6;
    }

    static boolean aOption(String[] args, String opt) {
        for (String a : args) if (a.equals(opt)) return true;
        return false;
    }

    static void lire(String dossier, int nbClasses) throws Exception {
        for (String m : LecteurBase.METHODES) {
            try {
                List<Echantillon> base = LecteurBase.charger(dossier, m, nbClasses);
                System.out.printf("%-4s %3d echantillons, dimension %d, premier : %s%n",
                        m, base.size(), base.get(0).dimension(), base.get(0));
            } catch (Exception e) {
                System.out.printf("%-4s erreur : %s%n", m, e.getMessage());
            }
        }
    }

    public static MatriceConfusion evaluerKnn(String dossier, String methode, int k, Distance d,
                                              String protocole, int nbClasses, boolean norm) throws Exception {
        return evaluerKnn(dossier, methode, k, d, protocole, nbClasses, norm, null);
    }

    // si pr n'est pas null on y range aussi les scores de chaque image testee
    public static MatriceConfusion evaluerKnn(String dossier, String methode, int k, Distance d,
                                              String protocole, int nbClasses, boolean norm,
                                              CourbePR pr) throws Exception {
        List<Echantillon> base = LecteurBase.charger(dossier, methode, nbClasses);
        MatriceConfusion mc = new MatriceConfusion(nbClasses);

        // une seule matrice pour tous les plis, en loo cela revient a cumuler les 72 tests
        for (Decoupage.Pli pli : Decoupage.depuis(protocole, base, GRAINE)) {
            List<Echantillon> app = pli.app;
            List<Echantillon> test = pli.test;
            // normalisation refaite a chaque pli, sur l'apprentissage du pli
            if (norm) {
                Normalisation n = new Normalisation();
                n.ajuster(app);
                app = n.appliquer(app);
                test = n.appliquer(test);
            }
            Knn knn = new Knn(k, d);
            knn.entrainer(app);
            for (Echantillon e : test) {
                mc.ajouter(e.getClasse(), knn.predire(e, nbClasses));
                if (pr != null) pr.ajouter(e.getClasse(), knn.scores(e, nbClasses));
            }
        }
        return mc;
    }

    // courbes precision/rappel du knn avec k = 12 (taille d'une classe), en leave one out, distance l1
    // avec k = 12 les scores prennent 13 valeurs possibles (0/12 a 12/12), assez pour tracer une courbe
    static void courbesPR(String dossier, int nbClasses, boolean norm) throws Exception {
        List<String> noms = new ArrayList<>();
        List<CourbePR> courbes = new ArrayList<>();
        System.out.printf("knn k=12, l1, loo, %d classes%s%n", nbClasses, norm ? ", normalise" : "");
        System.out.println("meth  auc moyenne   auc par classe");
        for (String m : LecteurBase.METHODES) {
            CourbePR pr = new CourbePR(nbClasses);
            evaluerKnn(dossier, m, 12, Distance.MANHATTAN, "loo", nbClasses, norm, pr);
            StringBuilder sb = new StringBuilder();
            for (int c = 1; c <= nbClasses; c++) sb.append(String.format(" %.3f", pr.auc(c)));
            System.out.printf("%-4s  %.3f        %s%n", m, pr.aucMoyenne(), sb);
            noms.add(m);
            courbes.add(pr);
        }
        String fichier = "resultats/pr_knn_" + nbClasses + "classes" + (norm ? "_norm" : "") + ".csv";
        CourbePR.ecrireCsv(fichier, noms, courbes);
        System.out.println("courbes moyennes ecrites dans " + fichier);
    }

    // tableau du taux de reconnaissance en fonction de k, pour chaque methode et chaque distance
    static void etudeK(String dossier, String protocole, int nbClasses, boolean norm) throws Exception {
        int[] ks = {1, 3, 5, 7, 9, 11};
        System.out.printf("protocole %s, %d classes%s%n", protocole, nbClasses, norm ? ", normalise" : "");
        for (Distance d : new Distance[]{Distance.MANHATTAN, Distance.EUCLIDIENNE}) {
            System.out.println("\ndistance " + d.name().toLowerCase());
            StringBuilder entete = new StringBuilder("meth ");
            for (int k : ks) entete.append(String.format("  k=%-4d", k));
            System.out.println(entete);
            for (String m : LecteurBase.METHODES) {
                List<String> cases = new ArrayList<>();
                for (int k : ks) {
                    try {
                        double t = evaluerKnn(dossier, m, k, d, protocole, nbClasses, norm).tauxReconnaissance();
                        cases.add(String.format("%6.1f", 100 * t));
                    } catch (Exception e) {
                        cases.add("    --");
                    }
                }
                System.out.printf("%-4s %s%n", m, String.join("  ", cases));
            }
        }
    }
}
