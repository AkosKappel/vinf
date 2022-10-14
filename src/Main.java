import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Main {
    private static final String wikipediaFolder = "C:\\Users\\kappe\\Downloads\\wikipedia\\";
    private static final String[] xmlFiles = {
            "wiki-data-partial.xml",
            "DavidBeckham.xml",
            "soccer-players.xml",
            "enwiki-latest-pages-articles1.xml",
            "enwiki-latest-pages-articles2.xml",
            "enwiki-latest-pages-articles3.xml",
            "enwiki-latest-pages-articles4.xml",
            "enwiki-latest-pages-articles5.xml",
    };

    public static void main(String[] args) {
        String filePath = wikipediaFolder + xmlFiles[2];

        // measure execution time
        long startTime = System.nanoTime();
        ArrayList<Player> players = parseFile(filePath);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        // print results
        for (Player player : players) {
            System.out.println(player);
        }
        System.out.println("Found " + players.size() + " players");
        System.out.println("Time: " + duration / 1_000_000 + "ms");

        tests(players);
    }

    private static void tests(ArrayList<Player> players) {
        Player p1 = players.get(10);
        Player p2 = players.get(11);
        Player p3 = players.get(0);

        ClubHistory c1 = p1.getClubs().get(0);
        ClubHistory c2 = p2.getClubs().get(0);
        ClubHistory c3 = p3.getClubs().get(0);

        String ans = p1.yearsOverlap(c1, c2) ? "" : " don't";
        System.out.println("Years " + c1.getYearJoined() + "-" + c1.getYearLeft() + ans + " overlap with " + c2.getYearJoined() + "-" + c2.getYearLeft());

        ans = p1.yearsOverlap(c1, c3) ? "" : " don't";
        System.out.println("Years " + c1.getYearJoined() + "-" + c1.getYearLeft() + ans + " overlap with " + c3.getYearJoined() + "-" + c3.getYearLeft());

        if (p1.hasPlayedWith(p2)) {
            System.out.println(p1.getName() + " played with " + p2.getName() + " at " + p1.getPlayedAtWith(p2));
        } else {
            System.out.println(p1.getName() + " did not play with " + p2.getName());
        }

        if (p1.hasPlayedWith(p3)) {
            System.out.println(p1.getName() + " played with " + p3.getName() + " at " + p1.getPlayedAtWith(p3));
        } else {
            System.out.println(p1.getName() + " did not play with " + p3.getName());
        }
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
                        Matcher categoryMatcher = Regex.categorySoccerPlayerPattern.matcher(line);
                        if (!isSoccerPlayer && categoryMatcher.find()) {
                            isSoccerPlayer = true;
                        }

                        // read next line of page
                        pageBuilder.append(line);
                        pageBuilder.append("\n");
                        line = reader.readLine();
                    } while (!Regex.pageEndPattern.matcher(line).find());
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