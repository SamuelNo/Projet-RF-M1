package rf.donnees;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LecteurBase {

    public static final String[] METHODES = {"ART", "ZRK", "GFD", "YNG", "SA"};

    // SxxNyyy.MET, les zernike sont en .zrk.txt dans l'archive
    private static final Pattern NOM = Pattern.compile("s(\\d+)n(\\d+)\\.(\\w+)(\\.txt)?", Pattern.CASE_INSENSITIVE);

    // parcourt tout le dossier (sous-dossiers compris) et garde les fichiers de la methode demandee
    public static List<Echantillon> charger(String dossier, String methode, int nbClasses) throws IOException {
        List<Path> fichiers;
        try (Stream<Path> s = Files.walk(Paths.get(dossier))) {
            fichiers = s.filter(Files::isRegularFile).collect(Collectors.toList());
        }

        List<Echantillon> base = new ArrayList<>();
        for (Path p : fichiers) {
            Matcher m = NOM.matcher(p.getFileName().toString());
            // startsWith a cause de s05n011.zrkt.txt (faute de frappe dans l'archive)
            if (!m.matches() || !m.group(3).toLowerCase().startsWith(methode.toLowerCase())) {
                continue;
            }
            int classe = Integer.parseInt(m.group(1));
            int num = Integer.parseInt(m.group(2));
            if (classe > nbClasses) {
                continue;
            }
            base.add(new Echantillon(classe, num, lireValeurs(p)));
        }

        if (base.isEmpty()) {
            throw new IOException("aucun fichier ." + methode + " trouve dans " + dossier);
        }

        base.sort(Comparator.comparingInt(Echantillon::getClasse).thenComparingInt(Echantillon::getNumero));

        int d = base.get(0).dimension();
        for (Echantillon e : base) {
            if (e.dimension() != d) {
                throw new IOException(e + "." + methode + " : " + e.dimension() + " valeurs au lieu de " + d);
            }
        }
        return base;
    }

    // on prend tous les nombres du fichier, peu importe les separateurs
    static double[] lireValeurs(Path p) throws IOException {
        String contenu = new String(Files.readAllBytes(p), StandardCharsets.UTF_8).trim();
        List<Double> vals = new ArrayList<>();
        for (String t : contenu.split("[\\s;]+")) {
            if (t.isEmpty()) continue;
            try {
                vals.add(Double.parseDouble(t));
            } catch (NumberFormatException ex) {
                // valeur non numerique (entete eventuelle), ignoree
            }
        }
        double[] v = new double[vals.size()];
        for (int i = 0; i < v.length; i++) v[i] = vals.get(i);
        return v;
    }
}
