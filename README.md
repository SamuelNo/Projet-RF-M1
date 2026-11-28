# Projet Reconnaissance des Formes - M1 VMI

Implémentation en Java des méthodes de classification (k-NN, k-means, vote majoritaire) et évaluation sur la base SharvitB2 (Université Paris Cité).

## Données

Télécharger `SharvitB2.zip` sur la page du projet et le dézipper dans `data/`.
Le lecteur parcourt tout le dossier (sous-dossiers compris) et récupère les fichiers `SxxNyyy.ART`, `.ZRK`, `.GFD`, `.YNG`, `.SA`.

## Compilation

```
javac -d bin $(find src -name "*.java")
```

Sous Windows (PowerShell) :

```
javac -d bin (Get-ChildItem -Recurse src -Filter *.java).FullName
```

## Utilisation

```
java -cp bin rf.Main data lire 18
java -cp bin rf.Main data knn GFD 3 l2 loo --norm
java -cp bin rf.Main data etude-k kfold:4 6 --norm
java -cp bin rf.Main data pr 6
python scripts/tracer_pr.py resultats/pr_knn_6classes.csv
```

La commande `pr` calcule les courbes précision/rappel du k-NN avec k = 12 (leave one out, distance L1), affiche l'AUC par classe et écrit la courbe moyenne de chaque méthode dans `resultats/`. Le script Python (matplotlib) en fait une image.

- distances : `l1`, `l2`, `l3`, `linf`
- protocoles : `loo` (leave one out), `holdout:0.5`, `kfold:4`
- nombre de classes : 6 par défaut
- `--norm` : centrage-réduction calculé sur l'apprentissage

## Organisation

```
src/rf/
  Main.java
  donnees/   Echantillon, LecteurBase
  outils/    Distance, Normalisation
  classif/   Knn, (KMeans, VoteMajoritaire)
  eval/      MatriceConfusion, Decoupage, CourbePR
scripts/     tracer_pr.py
```
