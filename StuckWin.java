/******************************************************************************
 * 
 * Groupe 25:
 * 
 * BERTOMEU Tom
 * DUFILS Nathan
 * 
 * Ce code comporte 4 modes de jeu différents sélectionnables lors du lancement 
 * du jeu dans un terminal:
 * - java StuckWin : jeu en joueur contre joueur
 * - java Stuckwin 1 : jeu contre un robot jouant le premier mouvement jouable
 * - java Stuckwin 2 : jeu contre un robot jouant un coup bloquant l'un de ses 
 * pions si cela est possible, sinon il joue un mouvement aléatoire
 * - java Stuckwin ? : peu importe quel charactère se trouve à la place du '?'
 * cela lance un jeu du robot n°1 (B) contre le robot n°2 (R)
 * 
 * Le squelette original de la classe StuckWin a pu être légèrement modifié 
 * afin de correspondre aux exigeances de l'analyseur sonarQube
 * 
 ******************************************************************************/

import java.util.Scanner;
import java.security.SecureRandom;

public class StuckWin {
    static final Scanner input = new Scanner(System.in);
    private static final double BOARD_SIZE = 7;

    enum Result {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT}
    enum ModeMvt {REAL, SIMU}
    final char[] joueurs = {'B', 'R'};
    static final int SIZE = 8;
    static final char VIDE = '.';
    // 'B'=bleu 'R'=rouge '.'=vide '-'=n'existe pas
    char[][] state = {
            {'-', '-', '-', '-', 'R', 'R', 'R', 'R'},
            {'-', '-', '-', '.', 'R', 'R', 'R', 'R'},
            {'-', '-', '.', '.', '.', 'R', 'R', 'R'},
            {'-', 'B', 'B', '.', '.', '.', 'R', 'R'},
            {'-', 'B', 'B', 'B', '.', '.', '.', '-'},
            {'-', 'B', 'B', 'B', 'B', '.', '-', '-'},
            {'-', 'B', 'B', 'B', 'B', '-', '-', '-'},
    };
    SecureRandom random = new SecureRandom();

    /**
     * Déplace un pion ou simule son déplacement
     * @param couleur couleur du pion à déplacer
     * @param lcSource case source Lc
     * @param lcDest case destination Lc
     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.
     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement
     */
    Result deplace(char couleur, String lcSource, String lcDest,  ModeMvt mode) {
        int idLettreSource = (lcSource.charAt(0)-65);
        int idColSource = (lcSource.charAt(1)-48);

        int idLettreDest = (lcDest.charAt(0)-65);
        int idColDest = (lcDest.charAt(1)-48);

        String[] dests = possibleDests(couleur,idLettreSource,idColSource);
        int cpt = 0;
        for (int i = 0 ; i < dests.length ; i++) {
            if (lcDest.equals(dests[i])) {
                cpt++;
            }
        }

        if (state[idLettreSource][idColSource] == '.') {
            return Result.EMPTY_SRC;
        } else if (state[idLettreSource][idColSource] != couleur){
            return Result.BAD_COLOR;
        } else if (state[idLettreDest][idColDest] == '-') {
            return Result.EXT_BOARD;
        } else if (cpt == 0) {
                return Result.TOO_FAR;
        } else if (state[idLettreDest][idColDest] != '.') {
            return Result.DEST_NOT_FREE;
        } else {
            if (mode == ModeMvt.REAL) {
                char temp = state[idLettreDest][idColDest];
                state[idLettreDest][idColDest] = state[idLettreSource][idColSource];
                state[idLettreSource][idColSource] = temp;
            }
            return Result.OK;
        }
    }

    /**
     * Déplace un pion ou simule son déplacement sans vérifier la nature de la source
     * ce qui permet entre autres de déterminer les coups qu'un pion peut faire à l'avance
     * @param couleur couleur du pion à déplacer
     * @param lcSource case source Lc
     * @param lcDest case destination Lc
     * @param mode ModeMVT.REAL/SIMU selon qu'on réalise effectivement le déplacement ou qu'on le simule seulement.
     * @return enum {OK, BAD_COLOR, DEST_NOT_FREE, EMPTY_SRC, TOO_FAR, EXT_BOARD, EXIT} selon le déplacement
     */
    Result deplace2(char couleur, String lcSource, String lcDest,  ModeMvt mode) {
        int idLettreSource = (lcSource.charAt(0)-65);
        int idColSource = (lcSource.charAt(1)-48);

        int idLettreDest = (lcDest.charAt(0)-65);
        int idColDest = (lcDest.charAt(1)-48);

        String[] dests = possibleDests(couleur,idLettreSource,idColSource);
        int cpt = 0;
        for (int i = 0 ; i < dests.length ; i++) {
            if (lcDest.equals(dests[i])) {
                cpt++;
            }
        }

        if (state[idLettreDest][idColDest] == '-') { // vérifie si la destination est à l'extérieur du jeu
            return Result.EXT_BOARD;
        } else if (cpt == 0) { // vérifie si la destination est une des destinations possibles
                return Result.TOO_FAR;
        } else if (state[idLettreDest][idColDest] != '.') { // vérifie si la destination est libre
            return Result.DEST_NOT_FREE;
        } else { // joue si toutes les conditions sont remplies
            if (mode == ModeMvt.REAL) {
                char temp = state[idLettreDest][idColDest];
                state[idLettreDest][idColDest] = state[idLettreSource][idColSource];
                state[idLettreSource][idColSource] = temp;
            }
            return Result.OK;
        }
    }

    /**
     * Construit les trois chaînes représentant les positions accessibles
     * à partir de la position de départ [idLettre][idCol].
     * @param couleur couleur du pion à jouer
     * @param idLettre id de la ligne du pion à jouer
     * @param idCol id de la colonne du pion à jouer
     * @return tableau des trois positions jouables par le pion (redondance possible sur les bords)
     */
    String[] possibleDests(char couleur, int idLettre, int idCol){
        String[] possibleDests = new String[3];
        switch (couleur) {
            case 'B':
                if (idLettre+65-1 < 65) { // vérifie si la position est dans le jeu
                    possibleDests[0] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[0] = Character.toString((char)(idLettre+65-1)) + Character.toString(idCol+48);
                }
                if (idLettre+65-1 < 65 || idCol+48+1 > 55) { // vérifie si la position est dans le jeu
                    possibleDests[1] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[1] = Character.toString((char)(idLettre+65-1)) + Character.toString(idCol+48+1);
                }
                if (idCol+48+1 > 55) { // vérifie si la position est dans le jeu
                    possibleDests[2] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[2] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48+1);
                }
                break;
            case 'R':
                if (idLettre+65+1 > 71) { // vérifie si la position est dans le jeu
                    possibleDests[0] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[0] = Character.toString((char)(idLettre+65+1)) + Character.toString(idCol+48);
                }
                if (idLettre+65+1 > 71 || idCol+48-1 < 48) { // vérifie si la position est dans le jeu
                    possibleDests[1] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[1] = Character.toString((char)(idLettre+65+1)) + Character.toString(idCol+48-1);
                }    
                if (idCol+48-1 < 48) { // vérifie si la position est dans le jeu
                    possibleDests[2] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48);
                } else {
                    possibleDests[2] = Character.toString((char)(idLettre+65)) + Character.toString(idCol+48-1);
                }
                break;
            default:
                System.out.println("Aucune couleur selectionnée");
        }
        return possibleDests;
    }

    /**
     * Affiche la moitié haute du plateau de jeu dans la configuration portée par
     * l'attribut d'état "state"
     */
    void afficheDebut() {
        for (int i = (int)BOARD_SIZE; i>=1; i--){
            int line=0;
            int position = i;
            for (int j = i-4 ; j > 0 ; j--) {
                System.out.print("  ");
            }
            if (i%2 != 0 && i < 4) {
                System.out.print("  ");
            }
            while (position<SIZE) {
                switch (state[line][position]) {
                    case 'R':
                        System.out.print(ConsoleColors.RED_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    case 'B':
                        System.out.print(ConsoleColors.BLUE_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    case '.':
                        System.out.print(ConsoleColors.WHITE_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    default:
                        break;
                }
                line++;
                position++;
            }
            System.out.println();
        }
    }

    /**
     * Affiche la moitié basse du plateau de jeu dans la configuration portée par
     * l'attribut d'état "state"
     */
    void afficheFin() {
        for (int j = 0; j<BOARD_SIZE-1; j++){
            int line=j;
            int position = 0;
            for (int i = j-2 ; i > 0 ; i--) {
                System.out.print("  ");
            }
            if (j%2 != 0 && j < 2) {
                System.out.print("  ");
            }
            while (line<SIZE-1) {
                switch (state[line][position]) {
                    case 'R':
                        System.out.print(ConsoleColors.RED_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    case 'B':
                        System.out.print(ConsoleColors.BLUE_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    case '.':
                        System.out.print(ConsoleColors.WHITE_BACKGROUND_BRIGHT + (char) (line + 65) + position + ConsoleColors.RESET + "  ");
                        break;
                    default:
                        break;
                }
                line++;
                position++;
            }
            System.out.println();
        }
    }

    /**
     * Affiche le plateau de jeu dans la configuration portée par
     * l'attribut d'état "state"
     */
    void affiche() {
        afficheDebut();
        afficheFin();  
    }

    /**
     * Joue un tour
     * Joue le premier pion jouable à la première position jouable
     * @param couleur couleur du pion à jouer
     * @return tableau contenant la position de départ et la destination du pion à jouer.
     */
    String[] jouerIA(char couleur, String[] args) {
        String[] jeuIA = new String[2];
        switch (couleur) {
            case 'B':
                jeuIA = jouePremier(couleur);
                break;

            case 'R':
                if (args[0].equals("1")) {
                    jeuIA = jouePremier(couleur);
                } else {
                    jeuIA = debutant(couleur);
                }
                break;

            default:
                break;
        }
        return jeuIA;
    }

    /**
     * gère le jeu en fonction du joueur/couleur
     * @param couleur
     * @return tableau de deux chaînes {source,destination} du pion à jouer
     */
    String[] jouer(char couleur, String[] args){
        String src = "";
        String dst = "";
        String[] mvtIa;
        System.out.println("Mouvement " + couleur);
        switch(couleur) {
            case 'B':
                if (args.length == 0 || args[0].equals("1") || args[0].equals("2")) {
                    System.out.print("Source: ");
                    src = input.next();
                    while ((src.charAt(0) < 65 || src.charAt(0) > 71) || (src.charAt(1) < 49 || src.charAt(1) > 55 ) || src.length()!=2) {
                        System.out.print("Merci de rentrer des coordonnées valides pour la source: ");
                        src = input.next();
                    } // vérifie la valeur entrée pour la source pour éviter de faire planter le jeu
                    System.out.print("Destination: ");
                    dst = input.next();
                    while ((dst.charAt(0) < 65 || dst.charAt(0) > 71) || (dst.charAt(1) < 49 || dst.charAt(1) > 55 ) || dst.length()!=2) {
                        System.out.print("Merci de rentrer des coordonnées valides pour la destination: ");
                        dst = input.next();
                    } // vérifie la valeur entrée pour la destination pour éviter de faire planter le jeu
                    System.out.println(src + "->" + dst);
                    break;
                } else {
                    mvtIa = jouerIA(couleur, args);
                    src = mvtIa[0];
                    dst = mvtIa[1];
                    System.out.println(src + "->" + dst);
                    break;
                }
                
            case 'R':
                if (args.length == 0) {
                    System.out.print("Source: ");
                    src = input.next();
                    while ((src.charAt(0) < 65 || src.charAt(0) > 71) || (src.charAt(1) < 49 || src.charAt(1) > 55 ) || src.length()!=2) {
                        System.out.print("Merci de rentrer des coordonnées valides pour la source: ");
                        src = input.next();
                    } // vérifie la valeur entrée pour la source pour éviter de faire planter le jeu
                    System.out.print("Destination: ");
                    dst = input.next();
                    while ((dst.charAt(0) < 65 || dst.charAt(0) > 71) || (dst.charAt(1) < 49 || dst.charAt(1) > 55 ) || dst.length()!=2) {
                        System.out.print("Merci de rentrer des coordonnées valides pour la destination: ");
                        dst = input.next();
                    } // vérifie la valeur entrée pour la destination pour éviter de faire planter le jeu
                    System.out.println(src + "->" + dst);
                    break;
                } else {
                    mvtIa = jouerIA(couleur, args);
                    src = mvtIa[0];
                    dst = mvtIa[1];
                    System.out.println(src + "->" + dst);
                    break;
                }
            default:
                break;
        }
        return new String[]{src, dst};
    }

    /**
     * AI standard (naïve):
     * joue le premier pion pouvant être joué, à la première position qu'il peut jouer
     * @param couleur
     * @return
     */
    String[] jouePremier(char couleur) {
        String[] jeuPremier = new String[2];
        // parcours le jeu à la recherche du pion de la couleur à jouer
        for (int i = 0 ; i < (int)BOARD_SIZE ; i++) {
            for (int j = 0 ; j < SIZE ; j++) {
                if (state[i][j] == couleur) {
                    // joue le premier pion jouable
                    String source = Character.toString((char)(i+65)) + Character.toString(j+48);
                    String[] possibleDests = possibleDests(couleur,i,j);
                    for (int k = 0 ; k < 3 ; k++) {
                        if (deplace(couleur, source, possibleDests[k], ModeMvt.SIMU) == Result.OK) {
                            jeuPremier[0] = source;
                            jeuPremier[1] = possibleDests[k];
                            return jeuPremier;
                        }
                    }
                }
            }
        }
        return jeuPremier;
    }

    /**
     * IA plus avancée:
     * si elle peut bloquer l'un de ses pions, elle le fait
     * sinon elle joue un pion au hasard à une position au hasard
     * @param couleur
     * @return
     */
    String[] debutant(char couleur) {
        String[] debutant = new String[2];
        if (peutBloquer(couleur)) { // vérifie s'il peut jouer un pion pour le bloquer
            // joue le premier pion bloquable
            for (int i = 0 ; i < (int)BOARD_SIZE ; i++) {
                for (int j = 0 ; j < SIZE ; j++) {
                    if (state[i][j] == couleur && peutJouer(couleur, i, j)) {
                        String cell = Character.toString((char)(i+65)) + j;
                        String[] possDests = possibleDests(couleur, i, j);
                        for (int k = 0 ; k < possDests.length ; k++) {
                            int idLettreDest = (possDests[k].charAt(0)-65);
                            int idColDest = (possDests[k].charAt(1)-48);
                            if (!peutJouer(couleur, idLettreDest, idColDest) && state[idLettreDest][idColDest] == '.') {
                                debutant[0] = cell;
                                debutant[1] = possDests[k];
                                return debutant;
                            }
                        }
                    }
                }
            }
        } else { // si aucun pion ne peut se bloquer, jouer un mouvement au hasard
            String[] pions = listePions(couleur); // établit une liste des coordonnées de tous les pions
            String jouer = getRandomElement(pions); // tire un élément au hasard dans cette liste
            int idLettre = (jouer.charAt(0)-65);
            int idCol = (jouer.charAt(1)-48);
            while (!peutJouer(couleur, idLettre, idCol)) { // vérifie si ce pion peut jouer
                jouer = getRandomElement(pions);
                idLettre = (jouer.charAt(0)-65);
                idCol = (jouer.charAt(1)-48);
            }
            String[] possDests = possibleDests(couleur, idLettre, idCol); // liste les positions jouables par le pion tiré
            String jouer2 = getRandomElement(possDests); // tire une position au hasard dans cette liste
            while (deplace(couleur, jouer, jouer2, ModeMvt.SIMU) != Result.OK) { // vérifie si ce mouvement est faisable
                jouer2 = getRandomElement(possDests);
            }
            debutant[0] = jouer;
            debutant[1] = jouer2;
        }
        return debutant;
    }

    /**
     * tire un élément au hasard de la liste fournie
     * @param liste
     * @return
     */
    String getRandomElement(String[] liste) {
        int index = random.nextInt(liste.length);
        return liste[index];
    }

    /**
     * vérifie si le pion dont les coordonnées sont indiquées est jouable
     * @param couleur
     * @param ligne
     * @param colonne
     * @return
     */
    boolean peutJouer(char couleur, int ligne, int colonne) {
        String[] possDests=possibleDests(couleur,ligne,colonne); // liste les positions jouables par le pion
        String cell = Character.toString((char)(ligne+65)) + colonne;
        for (int k = 0; k<3; k++){ // vérifie si chaque destination est jouable
            if (deplace2(couleur, cell, possDests[k], ModeMvt.SIMU) == Result.OK) {
                return true;
            }
        }
        return false;
    }

    /**
     * détermine si le joueur peut jouer un coup pouvant bloquer un de ses pions
     * @param couleur
     * @return
     */
    boolean peutBloquer(char couleur) {
        // parcours le jeu à la recherche d'un pion jouable de la couleur à jouer
        for (int i = 0 ; i < (int)BOARD_SIZE ; i++) {
            for (int j = 0 ; j < SIZE ; j++) {
                if (state[i][j] == couleur && peutJouer(couleur, i, j)) {
                    String[] possDests = possibleDests(couleur, i, j);
                    for (int k = 0 ; k < possDests.length ; k++) { // vérifie pour chaque position jouable si elle permet au pion de se bloquer
                        int idLettreDest = (possDests[k].charAt(0)-65);
                        int idColDest = (possDests[k].charAt(1)-48);
                        if (!peutJouer(couleur, idLettreDest, idColDest) && state[idLettreDest][idColDest] == '.') {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * retourne une liste des positions de tous les pions du joueur
     * @param couleur
     * @return
     */
    String[] listePions(char couleur) {
        String[] listePions = new String[13];
        int cpt = 0;
        // parcours le jeu à la recherche du pion de la couleur à jouer
        for (int i = 0 ; i < (int)BOARD_SIZE ; i++) {
            for (int j = 0 ; j < SIZE ; j++) {
                if (state[i][j] == couleur) { // récupère les coordonnées de chaque pion
                    listePions[cpt] = Character.toString((char)(i+65)) + j; 
                    cpt++;
                }
            }
        }
        return listePions;
    }

    /**
     * retourne 'R' ou 'B' si vainqueur, 'N' si partie pas finie
     * @param couleur
     * @return
     */
    char finPartie(char couleur){
        int cpt=0;
        for (int i = 0; i<SIZE-1; i++){
            for (int j = 0; j<SIZE-1; j++){
                if (couleur == state[i][j] && peutJouer(couleur, i, j)){
                    cpt++;
                }
            }
        }
        if (cpt == 0)
            return couleur;
        else
            return 'N';
    }


    public static void main(String[] args) {
        StuckWin jeu = new StuckWin();
        String src = "";
        String dest;
        String[] reponse;
        Result status;
        char partie;
        char curCouleur = jeu.joueurs[0];
        char nextCouleur = jeu.joueurs[1];
        char tmp;
        int cpt = 0;

        // version console
        do {
            // séquence pour Bleu ou rouge
            jeu.affiche();
            do {
                reponse = jeu.jouer(curCouleur, args);
                src = reponse[0];
                dest = reponse[1];
                if("q".equals(src))
                    return;
                status = jeu.deplace(curCouleur, src, dest, ModeMvt.REAL);
                partie = jeu.finPartie(nextCouleur);
                System.out.println("status : "+status + " partie : " + partie);
            } while(status != Result.OK && partie=='N');
            tmp = curCouleur;
            curCouleur = nextCouleur;
            nextCouleur = tmp;
            cpt ++;
        } while(partie =='N');
        String finPartie = String.format("Victoire : %s (%s coups)",partie, (cpt/2));  
        System.out.printf(finPartie);
    }
}

