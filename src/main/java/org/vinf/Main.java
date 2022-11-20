package org.vinf;

import org.vinf.documents.*;
import org.vinf.utils.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;


public class Main {
    private static final String dataFolder = "./data/";
    private static final String[] xmlFiles = {
            "soccer-clubs.xml",
            "soccer-players.xml",
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
        invertedIndex.index(xmlFiles);

        // measure execution end time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

//        for (Page p : invertedIndex.getDocuments()) {
//            System.out.println(p.getTitle());
//        }

//        invertedIndex.print();
//        invertedIndex.printDocuments();
//        invertedIndex.printPlayers();
//        invertedIndex.printClubs();
        System.out.println("Found " + invertedIndex.size() + " documents in " + duration / 1_000_000 + " ms");

        ArrayList<Club> clubs = invertedIndex.getClubs();
        ArrayList<Player> players = invertedIndex.getPlayers();

//        tests();
//        testClubs(clubs);
//        testPlayers(players);

        commandLine.help();
        commandLine.run();
    }

    private static void tests() {
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

    private static void testPlayers(ArrayList<Player> players) {
        Player p1 = players.get(10); // Míchel
        Player p2 = players.get(11); // Emilio Butragueño
        Player p3 = players.get(0); // Bobby Charlton

        ClubHistory c1 = p1.getProfessionalClubs().get(0);
        ClubHistory c2 = p2.getProfessionalClubs().get(0);
        ClubHistory c3 = p3.getProfessionalClubs().get(0);

        String ans = Player.yearsOverlap(c1, c2) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c2.getYearStart() + "-" + c2.getYearEnd());

        ans = Player.yearsOverlap(c1, c3) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c3.getYearStart() + "-" + c3.getYearEnd());

        commandLine.teammates(new String[]{p1.getName(), ",", p2.getName()});
        commandLine.teammates(new String[]{p1.getName(), ",", p3.getName()});

//        for (int i = 0; i < players.size(); i++) {
//            System.out.println(players.get(i).getName() + " - " + i);
//        }

        Player p4 = players.get(67); // Brad Friedel
        Player p5 = players.get(90); // Bruce Grobbelaar

        commandLine.opponents(new String[]{p4.getName(), ",", p5.getName()});

        Player p6 = players.get(68); // DaMarcus Beasley
        Player p7 = players.get(91); // Colin Bell

        commandLine.opponents(new String[]{p6.getName(), ",", p7.getName()});

        Player p8 = players.get(2); // David Beckham
        Player p9 = players.get(27); // Thierry Henry

        commandLine.opponents(new String[]{p8.getName(), ",", p9.getName()});
    }

    private static void testClubs(ArrayList<Club> clubs) {
        Club c1 = clubs.get(0); // Arsenal F.C. (Premier League)
        Club c2 = clubs.get(1); // AFC Ajax (Eredivisie)
        Club c3 = clubs.get(2); // AZ Alkmaar (Eredivisie)
        Club c4 = clubs.get(8); // Chelsea F.C. (Premier League)
        Club c5 = clubs.get(56); // Rosenborg BK (Eliteserien (football))
        Club c6 = clubs.get(57); // Tromsø IL (Eliteserien)

        System.out.println(c1.playedInSameLeague(c2)); // false
        System.out.println(c2.playedInSameLeague(c3)); // true
        System.out.println(c3.playedInSameLeague(c4)); // false
        System.out.println(c4.playedInSameLeague(c1)); // true
        System.out.println(c5.playedInSameLeague(c6)); // true
    }

}
