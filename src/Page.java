import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                    // get club years
                    findClubYears(line, Regex.youthYearsPattern, player, ClubType.YOUTH);
                    findClubYears(line, Regex.collegeYearsPattern, player, ClubType.COLLEGE);
                    findClubYears(line, Regex.yearsPattern, player, ClubType.PROFESSIONAL);
                    findClubYears(line, Regex.nationalYearsPattern, player, ClubType.NATIONAL);

                    // get club names
                    findClubNames(line, Regex.youthClubsPattern, player, ClubType.YOUTH);
                    findClubNames(line, Regex.collegeClubsPattern, player, ClubType.COLLEGE);
                    findClubNames(line, Regex.clubsPattern, player, ClubType.PROFESSIONAL);
                    findClubNames(line, Regex.nationalTeamPattern, player, ClubType.NATIONAL);

                    // get national team names
                    Matcher nationalTeamsMatcher = Regex.nationalTeamPattern.matcher(line);
                    if (nationalTeamsMatcher.find()) {
                        int index = Integer.parseInt(nationalTeamsMatcher.group(1)) - 1;
                        String teamName = nationalTeamsMatcher.group(2);
                        player.updateClubName(index, teamName, ClubType.NATIONAL);
                    }

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

    private static void findClubYears(String line, Pattern pattern, Player player, ClubType clubType) {
        Matcher yearsMatcher = pattern.matcher(line);
        if (yearsMatcher.find()) {
            int index = Integer.parseInt(yearsMatcher.group(1)) - 1;
            String yearJoined = yearsMatcher.group(2);
            String yearsLeft = yearsMatcher.group(3);
            player.updateYearJoined(index, yearJoined, clubType);
            player.updateYearLeft(index, yearsLeft, clubType);
        }
    }

    private static void findClubNames(String line, Pattern pattern, Player player, ClubType clubType) {
        Matcher clubsMatcher = pattern.matcher(line);
        if (clubsMatcher.find()) {
            int index = Integer.parseInt(clubsMatcher.group(1)) - 1;
            String clubName = clubsMatcher.group(2);
            player.updateClubName(index, clubName, clubType);
        }
    }

}
