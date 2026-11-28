# trace les courbes precision/rappel a partir des csv generes par la commande pr
# usage : python scripts/tracer_pr.py resultats/pr_knn_6classes.csv
import sys
import csv
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

for chemin in sys.argv[1:]:
    with open(chemin) as f:
        lignes = list(csv.reader(f, delimiter=";"))
    noms = lignes[0][1:]
    rappel = [float(l[0]) for l in lignes[1:]]

    plt.figure(figsize=(6, 4.5))
    for j, nom in enumerate(noms):
        precision = [float(l[j + 1]) for l in lignes[1:]]
        plt.plot(rappel, precision, marker="o", markersize=3, label=nom)
    plt.xlabel("rappel")
    plt.ylabel("precision")
    plt.ylim(0, 1.05)
    plt.grid(alpha=0.3)
    plt.legend()
    plt.title(chemin.split("/")[-1].replace(".csv", "").replace("_", " "))
    plt.tight_layout()
    sortie = chemin.replace(".csv", ".png")
    plt.savefig(sortie, dpi=150)
    print("ecrit", sortie)
