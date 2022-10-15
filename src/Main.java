import java.io.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
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
    private static final InvertedIndex invertedIndex = new InvertedIndex();

    public static void main(String[] args) {
        String filePath = wikipediaFolder + xmlFiles[2];

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
        invertedIndex.print();

        // print players
//        invertedIndex.printDocuments();

//        tests(players);

        System.out.println("Found " + players.size() + " players");
        System.out.println("Time: " + duration / 1_000_000 + "ms");

        runApplication();
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

    // TODO: implement input for command line
    private static void runApplication() {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.print("Enter a search query: ");
            String[] args = scanner.nextLine().split(" ");

            switch (args[0]) {
                case "help" -> {
                    System.out.println("help - show this message");
                    System.out.println("search [names...] - search for players");
                    System.out.println("exit - exit the program");
                }
                case "search" -> {
                    if (args.length < 2) {
                        System.out.println("Please enter a search query");
                        continue;
                    }

                    String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                    ArrayList<Player> results = invertedIndex.search(query);

                    if (results.size() == 0) {
                        System.out.println("No results found");
                    } else {
                        System.out.println("Found " + results.size() + " results");
                        for (Player player : results) {
                            System.out.println(player);
                        }
                    }
                }
                case "player" -> {
                    System.out.println("Enter player name: ");
                }
                case "exit" -> running = false;
                default -> System.out.println("Unknown command: " + args[0]);
            }
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