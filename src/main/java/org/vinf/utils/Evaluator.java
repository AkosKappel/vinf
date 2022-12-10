package org.vinf.utils;

import org.vinf.documents.Page;

import java.util.ArrayList;

public class Evaluator {

    private final CommandLine cli;

    public Evaluator(CommandLine cli) {
        this.cli = cli;
    }

    public void evaluate() {
        cli.clearIndex();
        if (!cli.load(new String[]{"enwiki"})) return;

        int[] searchEval = evaluateSearch();
        int searchHits = searchEval[0];
        int numSearches = searchEval[1];

        int[] teammateEval = evaluateTeammates();
        int teammateTruePositives = teammateEval[0];
        int teammateFalsePositives = teammateEval[1];
        int teammateTrueNegatives = teammateEval[2];
        int teammateFalseNegatives = teammateEval[3];
        int numTeammateSearches = teammateEval[4];

        int[] opponentEval = evaluateOpponents();
        int opponentTruePositives = opponentEval[0];
        int opponentFalsePositives = opponentEval[1];
        int opponentTrueNegatives = opponentEval[2];
        int opponentFalseNegatives = opponentEval[3];
        int numOpponentSearches = opponentEval[4];

        int truePositives = teammateTruePositives + opponentTruePositives;
        int falsePositives = teammateFalsePositives + opponentFalsePositives;
        int trueNegatives = teammateTrueNegatives + opponentTrueNegatives;
        int falseNegatives = teammateFalseNegatives + opponentFalseNegatives;
        int total = numTeammateSearches + numOpponentSearches;

        int teammateAccuracy = (teammateTruePositives + teammateTrueNegatives) * 100 / numTeammateSearches;
        int teammatePrecision = teammateTruePositives * 100 / (teammateTruePositives + teammateFalsePositives);
        int teammateRecall = teammateTruePositives * 100 / (teammateTruePositives + teammateFalseNegatives);
        int teammateF1 = 2 * teammatePrecision * teammateRecall / (teammatePrecision + teammateRecall);

        int opponentAccuracy = (opponentTruePositives + opponentTrueNegatives) * 100 / numOpponentSearches;
        int opponentPrecision = opponentTruePositives * 100 / (opponentTruePositives + opponentFalsePositives);
        int opponentRecall = opponentTruePositives * 100 / (opponentTruePositives + opponentFalseNegatives);
        int opponentF1 = 2 * opponentPrecision * opponentRecall / (opponentPrecision + opponentRecall);

        int accuracy = (truePositives + trueNegatives) * 100 / total;
        int precision = truePositives * 100 / (truePositives + falsePositives);
        int recall = truePositives * 100 / (truePositives + falseNegatives);
        int f1 = 2 * precision * recall / (precision + recall);

        System.out.println("Search results: " + searchHits + "/" + numSearches);

        System.out.println("Teammates evaluation:");
        System.out.println("  Accuracy: " + teammateAccuracy + "%");
        System.out.println("  Precision: " + teammatePrecision + "%");
        System.out.println("  Recall: " + teammateRecall + "%");
        System.out.println("  F1: " + teammateF1 + "%");

        System.out.println("Opponents evaluation:");
        System.out.println("  Accuracy: " + opponentAccuracy + "%");
        System.out.println("  Precision: " + opponentPrecision + "%");
        System.out.println("  Recall: " + opponentRecall + "%");
        System.out.println("  F1: " + opponentF1 + "%");

        System.out.println("Overall evaluation:");
        System.out.println("  Accuracy: " + accuracy + "%");
        System.out.println("  Precision: " + precision + "%");
        System.out.println("  Recall: " + recall + "%");
        System.out.println("  F1: " + f1 + "%");
    }

    private int[] evaluateTeammates() {
        String[][] teammateSearches = new String[][]{
                new String[]{"Lionel Messi, Luis Suarez", "3"}, // FC Barcelona
                new String[]{"Karim Benzema, Cristiano Ronaldo"}, // Real Madrid
                new String[]{"Neymar, Kylian Mbappé"}, // Paris Saint-Germain
                new String[]{"Sergio Aguero, Kevin De Bruyne"}, // Manchester City
                new String[]{"Luka Modric, Sergio Ramos"}, // Real Madrid
                new String[]{"Ryan Giggs, David Beckham"}, // Manchester United
                new String[]{"Robert Lewandowski, Joshua Kimmich"}, // Bayern Munich
                new String[]{"James Rodriguez, Radamel Falcao"}, // AS Monaco
                new String[]{"Virgil van Dijk, Sadio Mane"}, // Liverpool
                new String[]{"Thiago Silva, Zlatan Ibrahimovic", "1"}, // Paris Saint-Germain
                new String[]{"Radamel Falcao, James Rodriguez"}, // AS Monaco
                new String[]{"Paul Pogba, Antoine Griezmann"}, // France national team
                new String[]{"Mesut Özil, Per Mertesacker"}, // Arsenal
                new String[]{"Angel Di Maria, Edinson Cavani"}, // Paris Saint-Germain
        };
        String[][] noTeammateSearches = new String[][]{
                new String[]{"Cristiano Ronaldo, Lionel Messi"},
                new String[]{"Eden Hazard, Neymar"},
                new String[]{"Robert Lewandowski, Sergio Aguero"},
                new String[]{"David Beckham, Andres Iniesta"},
                new String[]{"Virgil van Dijk, Zlatan Ibrahimovic"},
                new String[]{"Lionel Messi, David Beckham"},
                new String[]{"Cristiano Ronaldo, Neymar"},
                new String[]{"Kylian Mbappe, Cristiano Ronaldo"},
                new String[]{"Cristiano Ronaldo, Zlatan Ibrahimovic"},
                new String[]{"Thiago Silva, Sergio Ramos", "1"},
        };

        int truePositives = 0;
        int falsePositives = 0;
        int trueNegatives = 0;
        int falseNegatives = 0;
        int numSearches = teammateSearches.length + noTeammateSearches.length;

        for (String[] search : teammateSearches) {
            boolean result = cli.teammates(search);
            if (result) {
                truePositives++;
                System.out.print("True positive: ");
            } else {
                falseNegatives++;
                System.out.print("False negative: ");
            }
            System.out.println(String.join(", ", search));
        }

        for (String[] search : noTeammateSearches) {
            boolean result = cli.teammates(search);
            if (result) {
                falsePositives++;
                System.out.print("False positive: ");
            } else {
                trueNegatives++;
                System.out.print("True negative: ");
            }
            System.out.println(String.join(", ", search));
        }

        return new int[]{truePositives, falsePositives, trueNegatives, falseNegatives, numSearches};
    }

    private int[] evaluateOpponents() {
        String[][] opponentSearches = new String[][]{
                new String[]{"Lionel Messi, Cristiano Ronaldo"}, // El Clásico
                new String[]{"Radamel Falcao, Eden Hazard"}, // UEFA Champions League
                new String[]{"Sergio Aguero, Robert Lewandowski"}, // Bundesliga
                new String[]{"Mesut Özil, Ryan Giggs"}, // Premier League
                new String[]{"Andres Iniesta, David Beckham"}, // UEFA Champions League
                new String[]{"Zlatan Ibrahimovic, Virgil van Dijk"}, // Premier League
                new String[]{"Andres Iniesta, Cristiano Ronaldo"}, // UEFA Champions League
                new String[]{"David Beckham, Neymar"}, // UEFA Champions League
                new String[]{"Angel Di Maria, James Rodriguez"}, // UEFA Champions League
                new String[]{"Robert Lewandowski, Sergio Aguero"}, // Bundesliga
        };
        String[][] noOpponentSearches = new String[][]{
                new String[]{"Sergio Ramos, Luka Modric"},
                new String[]{"David Beckham, Ryan Giggs"},
                new String[]{"Robert Lewandowski, Joshua Kimmich"},
                new String[]{"Radamel Falcao, James Rodriguez"},
                new String[]{"Virgil van Dijk, Sadio Mane"},
                new String[]{"Paul Pogba, Antoine Griezmann"},
                new String[]{"Mesut Özil, Per Mertesacker"},
                new String[]{"Zlatan Ibrahimovic, Thiago Silva", "1"},
                new String[]{"Cristiano Ronaldo, Karim Benzema"},
                new String[]{"Angel Di Maria, Edinson Cavani"},
        };

        int truePositives = 0;
        int falsePositives = 0;
        int trueNegatives = 0;
        int falseNegatives = 0;
        int numSearches = opponentSearches.length + noOpponentSearches.length;

        for (String[] search : opponentSearches) {
            boolean result = cli.opponents(search);
            if (result) {
                truePositives++;
                System.out.print("True positive: ");
            } else {
                falseNegatives++;
                System.out.print("False negative: ");
            }
            System.out.println(String.join(", ", search));
        }

        for (String[] search : noOpponentSearches) {
            boolean result = cli.opponents(search);
            if (result) {
                falsePositives++;
                System.out.print("False positive: ");
            } else {
                trueNegatives++;
                System.out.print("True negative: ");
            }
            System.out.println(String.join(", ", search));
        }

        return new int[]{truePositives, falsePositives, trueNegatives, falseNegatives, numSearches};
    }

    private int[] evaluateSearch() {
        String[][] searches = new String[][]{
                new String[]{"David Beckham"},
                new String[]{"Manchester United"},
                new String[]{"Cristiano Ronaldo"},
                new String[]{"Lionel Messi"},
                new String[]{"Real Madrid"}
        };

        int searchHits = 0;
        int numSearches = searches.length;

        for (String[] search : searches) {
            ArrayList<Page> results = cli.search(search);
            if (results != null && results.size() > 0) searchHits++;
        }

        return new int[]{searchHits, numSearches};
    }

}
