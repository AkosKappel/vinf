import java.io.*;
import java.util.regex.Matcher;

public class Main {
    public static void main(String[] args) {
        String wikipediaFolder = "C:\\Users\\kappe\\Downloads\\wikipedia\\";
        String[] xmlFiles = {
                "wiki-data-partial.xml",
                "DavidBeckham.xml",
                "soccer-players-1.xml",
                "enwiki-latest-pages-articles1.xml",
                "enwiki-latest-pages-articles2.xml",
                "enwiki-latest-pages-articles3.xml",
                "enwiki-latest-pages-articles4.xml",
                "enwiki-latest-pages-articles5.xml",
        };
        String filePath = wikipediaFolder + xmlFiles[2];

        // time execution
        long startTime = System.nanoTime();
        parseFile(filePath);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time: " + duration / 1_000_000 + "ms");
    }

    private static void parseFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                // check if page starts with <page>
                if (Regex.pageStartPattern.matcher(line).find()) {
                    // reset page variables
                    StringBuilder pageBuilder = new StringBuilder();
                    String title = "";
                    boolean isSoccerPlayer = false;

                    do {
                        // get title from page
                        Matcher titleMatcher = Regex.titlePattern.matcher(line);
                        if (title.isEmpty() && titleMatcher.find()) {
                            title = titleMatcher.group(1);
                        }

                        Matcher categoryMatcher = Regex.playerCategoryPattern.matcher(line);
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
                        // TODO: parse pages about soccer players
                        Page.parse(pageBuilder.toString());
                        System.out.println("Title: " + title);
//                        StringBuilder infobox = parseInfobox(pageBuilder.toString());
//                        System.out.println("Infobox: " + infobox);
                        System.out.println("Page: " + pageBuilder);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}