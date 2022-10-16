package documents;

import utils.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

public abstract class Page {
    String title;

    protected Page(String title) {
        this.title = title;
    }

    public static Player parse(String page) throws IOException {
        BufferedReader reader = new BufferedReader(new StringReader(page));

        String title = "";
        Player player = null;

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
                player = new Player(title);

                long stack = Regex.bracketsStartPattern.matcher(line).results().count();
                while (stack > 0) {
                    // get club names and years
                    findClubYears(line, player);
                    findClubNames(line, player);

                    // read next line of infobox
                    line = reader.readLine();
                    stack += Regex.bracketsStartPattern.matcher(line).results().count();
                    stack -= Regex.bracketsEndPattern.matcher(line).results().count();
                }
                break;
            }
        }

        if (player != null && player.hasClubHistory()) return player;
        return null;
    }

    private static void findClubYears(String line, Player player) {
        Matcher yearsMatcher = Regex.clubYearsPattern.matcher(line);

        if (yearsMatcher.find()) {
            ClubType clubType = ClubType.getClubType(yearsMatcher.group(1));
            int index = Integer.parseInt(yearsMatcher.group(2)) - 1;
            String yearJoined = yearsMatcher.group(3);
            String yearsLeft = yearsMatcher.group(4);

            player.updateYearJoined(index, yearJoined, clubType);
            player.updateYearLeft(index, yearsLeft, clubType);
        }
    }

    private static void findClubNames(String line, Player player) {
        Matcher clubsMatcher = Regex.clubNamePattern.matcher(line);

        if (clubsMatcher.find()) {
            ClubType clubType = ClubType.getClubType(clubsMatcher.group(1));
            int index = Integer.parseInt(clubsMatcher.group(2)) - 1;
            String clubName = clubsMatcher.group(3);

            player.updateClubName(index, clubName, clubType);
        }
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
