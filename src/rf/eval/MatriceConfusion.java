package rf.eval;

// lignes = vraie classe, colonnes = classe predite
public class MatriceConfusion {

    private final int n;
    private final int[][] m;

    public MatriceConfusion(int nbClasses) {
        n = nbClasses;
        m = new int[n][n];
    }

    public void ajouter(int vraie, int predite) {
        m[vraie - 1][predite - 1]++;
    }

    public int total() {
        int t = 0;
        for (int[] l : m) for (int x : l) t += x;
        return t;
    }

    // bien classes (diagonale) / total
    public double tauxReconnaissance() {
        int ok = 0;
        for (int i = 0; i < n; i++) ok += m[i][i];
        return total() == 0 ? 0 : (double) ok / total();
    }

    // precision : parmi ceux predits c, combien sont vraiment c (colonne)
    public double precision(int c) {
        int col = 0;
        for (int i = 0; i < n; i++) col += m[i][c - 1];
        return col == 0 ? 0 : (double) m[c - 1][c - 1] / col;
    }

    // rappel : parmi les vrais c, combien on en a retrouve (ligne)
    public double rappel(int c) {
        int lig = 0;
        for (int j = 0; j < n; j++) lig += m[c - 1][j];
        return lig == 0 ? 0 : (double) m[c - 1][c - 1] / lig;
    }

    // moyenne harmonique des deux
    public double fMesure(int c) {
        double p = precision(c), r = rappel(c);
        return p + r == 0 ? 0 : 2 * p * r / (p + r);
    }

    public double fMesureMoyenne() {
        double s = 0;
        for (int c = 1; c <= n; c++) s += fMesure(c);
        return s / n;
    }

    public void afficher() {
        StringBuilder sb = new StringBuilder("      ");
        for (int j = 1; j <= n; j++) sb.append(String.format("%4d", j));
        sb.append("\n");
        for (int i = 0; i < n; i++) {
            sb.append(String.format("%4d |", i + 1));
            for (int j = 0; j < n; j++) sb.append(String.format("%4d", m[i][j]));
            sb.append("\n");
        }
        System.out.print(sb);
        System.out.printf("taux de reconnaissance : %.2f %%%n", 100 * tauxReconnaissance());
        System.out.printf("f-mesure moyenne       : %.3f%n", fMesureMoyenne());
        System.out.println("classe  precision  rappel  f-mesure");
        for (int c = 1; c <= n; c++) {
            System.out.printf("%6d  %9.3f  %6.3f  %8.3f%n", c, precision(c), rappel(c), fMesure(c));
        }
    }
}
