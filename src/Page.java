import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

public class Page {

    long id;
    String title;
    String content;

    private static long pageID = 0;

    private Page(String title) {
        this(title, "");
    }

    private Page(String title, String content) {
        this.id = pageID++;
        this.title = title;
        this.content = content;
    }

    public static Page parse(String page) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(page));

        String title = "";

        String line;
        // read until end of infobox
        while ((line = reader.readLine()) != null) {
            // get title from page
            Matcher titleMatcher = Regex.titlePattern.matcher(line);
            if (title.isEmpty() && titleMatcher.find()) {
                title = titleMatcher.group(1);
            }

            Matcher infoboxMatcher = Regex.infoboxFootballBiographyPattern.matcher(line);
            if (infoboxMatcher.find()) {
                long stack = Regex.bracketsStartPattern.matcher(line).results().count();
                while (stack > 0) {
                    // get years from infobox
                    Matcher yearsMatcher = Regex.yearsPattern.matcher(line);
                    if (yearsMatcher.find()) {
                        String yearsNum = yearsMatcher.group(1);
                        String yearsStart = yearsMatcher.group(2);
                        String yearsEnd = yearsMatcher.group(3);
                        if (yearsEnd == null) yearsEnd = yearsStart;
                        System.out.println("Years " + yearsNum + ": " + yearsStart + " - " + yearsEnd);
                    }

                    // get clubs from infobox
                    Matcher clubsMatcher = Regex.clubsPattern.matcher(line);
                    if (clubsMatcher.find()) {
                        String clubsNum = clubsMatcher.group(1);
                        String clubs = clubsMatcher.group(2);
                        System.out.println("Clubs " + clubsNum + ": " + clubs);
                    }

                    // read next line of infobox
                    line = reader.readLine();
                    stack += Regex.bracketsStartPattern.matcher(line).results().count();
                    stack -= Regex.bracketsEndPattern.matcher(line).results().count();
                }
                break;
            }
        }

        System.out.println("Title: " + title);
        System.out.println("########################################");

        return new Page(title);
    }

}
