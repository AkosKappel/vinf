import documents.*;
import utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

public class Main {
    private static final String wikipediaFolder = "C:\\Users\\kappe\\Downloads\\wikipedia\\";
    private static final String[] xmlFiles = {
            "soccer-players.xml",
            "enwiki-latest-pages-articles1.xml",
            "enwiki-latest-pages-articles2.xml",
            "enwiki-latest-pages-articles3.xml",
            "enwiki-latest-pages-articles4.xml",
            "enwiki-latest-pages-articles5.xml",
    };
    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine commandLine = new CommandLine(invertedIndex);

    public static void main(String[] args) {
        String filePath = wikipediaFolder + xmlFiles[0];

        // measure execution time
        long startTime = System.nanoTime();
        ArrayList<Player> players = parseFile(filePath);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // build inverted index
        for (Player player : players) {
            invertedIndex.addDocument(player);
        }

        // print inverted index
//        invertedIndex.print();

        // print players
//        invertedIndex.printDocuments();

//        tests(players);

        System.out.println("Found " + players.size() + " players");
        System.out.println("Time: " + duration / 1_000_000 + "ms");

        commandLine.run();
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

    private static ArrayList<Player> parseFile(String filePath) {
        ArrayList<Player> players = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // check if page starts with <page>
                if (Regex.pageStartPattern.matcher(line).find()) {
                    // reset page variables
                    StringBuilder pageBuilder = new StringBuilder();
                    boolean isSoccerPlayer = false;

                    do {
                        if (!isSoccerPlayer && Regex.infoboxFootballBiographyPattern.matcher(line).find()) {
                            isSoccerPlayer = true;
                        }

                        // add line to page
                        pageBuilder.append(line);
                        pageBuilder.append("\n");

                        // read next line of page
                        line = reader.readLine();

                        // read until </page> is found
                    } while (!Regex.pageEndPattern.matcher(line).find());

                    // add last line of page
                    pageBuilder.append(line);
                    pageBuilder.append("\n");

                    // filter out soccer players
                    if (isSoccerPlayer) {
                        Player p = Page.parse(pageBuilder.toString());
                        if (p != null) players.add(p);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return players;
    }

}