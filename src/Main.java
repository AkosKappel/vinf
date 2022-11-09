import documents.*;
import utils.*;

import java.util.ArrayList;
import java.util.Arrays;


public class Main {
    private static final String dataFolder = "./data/";
    private static final String[] xmlFiles = {
            "soccer-players.xml",
            "soccer-clubs.xml",
//            "enwiki-latest-pages-articles1.xml",
//            "enwiki-latest-pages-articles2.xml",
//            "enwiki-latest-pages-articles3.xml",
//            "enwiki-latest-pages-articles4.xml",
//            "enwiki-latest-pages-articles5.xml",
    };

    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine commandLine = new CommandLine(invertedIndex);

    public static void main(String[] args) {
        // measure execution start time
        long startTime = System.nanoTime();

        // read all XML files
//        indexClubs();
        indexPlayers();

        // measure execution end time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

//        invertedIndex.print();
//        invertedIndex.printDocuments();
//        tests(players);
        System.out.println("Found " + invertedIndex.size() + " documents in " + duration / 1_000_000 + " ms");

//        commandLine.help();
//        commandLine.run();
    }

    private static void indexClubs() {
        for (String xmlFile : xmlFiles) {
            String filePath = dataFolder + xmlFile;
            System.out.println("Parsing " + filePath + " ...");

            // parse XML file
            ArrayList<Club> clubs = Parser.parseClubs(filePath);

            // build inverted index
            for (Page club : clubs) {
                System.out.println("  " + club.getTitle());
//                invertedIndex.addDocument(club);
            }
        }
    }

    private static void indexPlayers() {
        for (String xmlFile : xmlFiles) {
            String filePath = dataFolder + xmlFile;
            System.out.println("Parsing " + filePath + " ...");

            // parse XML file
            ArrayList<Player> players = Parser.parsePlayers(filePath);

            // build inverted index
            for (Player player : players) {
                invertedIndex.addDocument(player);
            }
        }
    }

    private static void tests(ArrayList<Player> players) {
        Player p1 = players.get(10);
        Player p2 = players.get(11);
        Player p3 = players.get(0);

        ClubHistory c1 = p1.getProfessionalClubs().get(0);
        ClubHistory c2 = p2.getProfessionalClubs().get(0);
        ClubHistory c3 = p3.getProfessionalClubs().get(0);

        String ans = p1.yearsOverlap(c1, c2) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c2.getYearStart() + "-" + c2.getYearEnd());

        ans = p1.yearsOverlap(c1, c3) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c3.getYearStart() + "-" + c3.getYearEnd());

        commandLine.teammates(new String[]{p1.getName(), ",", p2.getName()});
        commandLine.teammates(new String[]{p1.getName(), ",", p3.getName()});

        ArrayList<Integer> list1 = new ArrayList<>(Arrays.asList(1, 3, 12, 23));
        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 10, 23));
        ArrayList<Integer> list3 = new ArrayList<>(Arrays.asList(1, 2));

        ArrayList<ArrayList<Integer>> lists1 = new ArrayList<>();
        lists1.add(list1);
        lists1.add(list2);
        lists1.add(list3);

        System.out.println(invertedIndex.intersect(list1, list2));
        System.out.println(invertedIndex.intersect(lists1));
    }

}