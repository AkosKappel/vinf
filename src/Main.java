import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {
    public static void main(String[] args) {
        String wikipediaFolder = "C:\\Users\\kappe\\Downloads\\wikipedia\\";
        String[] xmlFiles = {
                "wiki-data-partial.xml",
                "DavidBeckham.xml",
                "enwiki-latest-pages-articles1.xml",
                "enwiki-latest-pages-articles2.xml",
                "enwiki-latest-pages-articles3.xml",
                "enwiki-latest-pages-articles4.xml",
                "enwiki-latest-pages-articles5.xml",
        };
        String filePath = wikipediaFolder + xmlFiles[2];

        String[] infoboxLabels = {
                "person",
                "sport",
                "sportsperson",
                "football club",
                "sports league",
                "Australian football club",
                "national football team",
                "football biography",
                "football tournament",
                "football league",
                "football match"
        };

        // time execution
        long startTime = System.nanoTime();
        parseFile(filePath);
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        System.out.println("Time: " + duration / 1_000_000 + "ms");
    }

    private static void parseFile(String filePath) {
        // patterns for parsing
        Pattern pageStartPattern = Pattern.compile("<page>");
        Pattern pageEndPattern = Pattern.compile("</page>");
        Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
        Pattern infoboxStartPattern = Pattern.compile("\\{\\{Infobox ?([\\w. ]*)", Pattern.CASE_INSENSITIVE);
        Pattern startBracketsPattern = Pattern.compile("\\{\\{");
        Pattern endBracketsPattern = Pattern.compile("}}");

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));
        ) {
            Matcher titleMatcher;
            Matcher infoboxMatcher;

            StringBuilder pageBuilder;
            StringBuilder infoboxBuilder;

            int stack;
            int numPages = 0;

            boolean isRelevant;  // TODO: filter out relevant pages

            String title;
            String infoboxType;
            String line;

            while ((line = reader.readLine()) != null) {
                // check if page start
                if (pageStartPattern.matcher(line).find()) {
                    // reset page variables
                    pageBuilder = new StringBuilder();
                    infoboxBuilder = new StringBuilder();

                    title = "";
                    infoboxType = "";
                    isRelevant = false;

                    do {
                        // get title from page
                        titleMatcher = titlePattern.matcher(line);
                        if (title.isEmpty() && titleMatcher.find()) {
                            title = titleMatcher.group(1);
                        }

                        // get infobox from page
                        infoboxMatcher = infoboxStartPattern.matcher(line);
                        if (infoboxMatcher.find()) {
                            infoboxType = infoboxMatcher.group(1).toLowerCase();
                            stack = (int) startBracketsPattern.matcher(line).results().count();
                            while (stack > 0) {
                                pageBuilder.append(line);
                                pageBuilder.append("\n");
                                infoboxBuilder.append(line);
                                infoboxBuilder.append("\n");
                                line = reader.readLine();
                                stack += startBracketsPattern.matcher(line).results().count();
                                stack -= endBracketsPattern.matcher(line).results().count();
                            }
                        }

                        // read next line of page
                        pageBuilder.append(line);
                        pageBuilder.append("\n");
                        line = reader.readLine();
                    } while (!pageEndPattern.matcher(line).find());
                    pageBuilder.append(line);
                    pageBuilder.append("\n");
                    numPages++;

//                    System.out.println("----------------------------------------");
//                    System.out.println("Title: " + title);
//                    System.out.println("Infobox type: " + infoboxType);
//                    System.out.println("Infobox: " + infoboxBuilder);
//                    System.out.println("Page num: " + numPages);
//                    System.out.println("Page: " + pageBuilder);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}