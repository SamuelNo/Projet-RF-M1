package rf.outils;

// distances de minkowski L_p = (somme |xi - yi|^p)^(1/p)
// p = 1 manhattan, p = 2 euclidienne, p infini = max des ecarts
public enum Distance {

    MANHATTAN(1),
    EUCLIDIENNE(2),
    MINKOWSKI3(3),
    CHEBYSHEV(Double.POSITIVE_INFINITY);

    private final double p;

    Distance(double p) {
        this.p = p;
    }

    public double calculer(double[] a, double[] b) {
        // cas limite p -> infini, la formule generale ne s'applique pas donc traite a part
        if (Double.isInfinite(p)) {
            double max = 0;
            for (int i = 0; i < a.length; i++) max = Math.max(max, Math.abs(a[i] - b[i]));
            return max;
        }
        double s = 0;
        for (int i = 0; i < a.length; i++) {
            s += Math.pow(Math.abs(a[i] - b[i]), p);
        }
        return Math.pow(s, 1.0 / p);
    }

    public static Distance depuis(String nom) {
        switch (nom.toLowerCase()) {
            case "l1": case "manhattan": return MANHATTAN;
            case "l2": case "euclidienne": return EUCLIDIENNE;
            case "l3": return MINKOWSKI3;
            case "linf": case "chebyshev": return CHEBYSHEV;
            default: throw new IllegalArgumentException("distance inconnue : " + nom);
        }
    }
}
