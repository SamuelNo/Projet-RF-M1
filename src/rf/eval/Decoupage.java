package rf.eval;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import rf.donnees.Echantillon;

public class Decoupage {

    // un pli = un couple (apprentissage, test)
    // holdout donne 1 pli, kfold en donne k, loo en donne autant que d'images
    public static class Pli {
        public final List<Echantillon> app;
        public final List<Echantillon> test;

        Pli(List<Echantillon> app, List<Echantillon> test) {
            this.app = app;
            this.test = test;
        }
    }

    // on regroupe par classe pour garder la meme proportion partout (stratifie)
    // le melange est fait avec une graine fixe pour pouvoir refaire exactement les memes resultats
    private static Map<Integer, List<Echantillon>> parClasse(List<Echantillon> base, long graine) {
        Map<Integer, List<Echantillon>> res = new LinkedHashMap<>();
        for (Echantillon e : base) {
            res.computeIfAbsent(e.getClasse(), c -> new ArrayList<>()).add(e);
        }
        Random r = new Random(graine);
        for (List<Echantillon> l : res.values()) Collections.shuffle(l, r);
        return res;
    }

    // holdout : ratio de chaque classe en apprentissage, le reste en test
    public static List<Pli> holdout(List<Echantillon> base, double ratio, long graine) {
        List<Echantillon> app = new ArrayList<>();
        List<Echantillon> test = new ArrayList<>();
        for (List<Echantillon> l : parClasse(base, graine).values()) {
            int nApp = (int) Math.round(l.size() * ratio);
            app.addAll(l.subList(0, nApp));
            test.addAll(l.subList(nApp, l.size()));
        }
        List<Pli> res = new ArrayList<>();
        res.add(new Pli(app, test));
        return res;
    }

    // validation croisee en k plis stratifies
    // chaque pli sert de test une fois, les k-1 autres servent d'apprentissage
    public static List<Pli> kPlis(List<Echantillon> base, int k, long graine) {
        List<List<Echantillon>> plis = new ArrayList<>();
        for (int i = 0; i < k; i++) plis.add(new ArrayList<>());
        // les images de chaque classe sont reparties a tour de role pour que chaque pli contienne toutes les classes
        for (List<Echantillon> l : parClasse(base, graine).values()) {
            for (int i = 0; i < l.size(); i++) plis.get(i % k).add(l.get(i));
        }
        List<Pli> res = new ArrayList<>();
        for (int i = 0; i < k; i++) {
            List<Echantillon> app = new ArrayList<>();
            for (int j = 0; j < k; j++) if (j != i) app.addAll(plis.get(j));
            res.add(new Pli(app, plis.get(i)));
        }
        return res;
    }

    // leave one out : chaque image est testee une fois, on apprend sur toutes les autres
    // 72 plis pour 6 classes, le temps de calcul reste faible vu la taille de la base
    // on garde 11 exemples par classe en apprentissage au lieu de 6 avec un holdout 50/50
    public static List<Pli> leaveOneOut(List<Echantillon> base) {
        List<Pli> res = new ArrayList<>();
        for (int i = 0; i < base.size(); i++) {
            List<Echantillon> app = new ArrayList<>(base);
            Echantillon e = app.remove(i);
            List<Echantillon> test = new ArrayList<>();
            test.add(e);
            res.add(new Pli(app, test));
        }
        return res;
    }

    public static List<Pli> depuis(String protocole, List<Echantillon> base, long graine) {
        String p = protocole.toLowerCase();
        if (p.equals("loo")) return leaveOneOut(base);
        if (p.startsWith("holdout")) {
            double ratio = p.contains(":") ? Double.parseDouble(p.split(":")[1]) : 0.5;
            return holdout(base, ratio, graine);
        }
        if (p.startsWith("kfold")) {
            int k = p.contains(":") ? Integer.parseInt(p.split(":")[1]) : 4;
            return kPlis(base, k, graine);
        }
        throw new IllegalArgumentException("protocole inconnu : " + protocole);
    }
}
