import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
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
                    // get youth club years
                    Matcher youthYearsMatcher = Regex.youthYearsPattern.matcher(line);
                    if (youthYearsMatcher.find()) {
                        int index = Integer.parseInt(youthYearsMatcher.group(1)) - 1;
                        String yearJoined = youthYearsMatcher.group(2);
                        String yearLeft = youthYearsMatcher.group(3);
                        player.updateYearJoined(index, yearJoined, ClubType.YOUTH);
                        player.updateYearLeft(index, yearLeft, ClubType.YOUTH);
                    }

                    // get youth club names
                    Matcher youthClubMatcher = Regex.youthClubsPattern.matcher(line);
                    if (youthClubMatcher.find()) {
                        int index = Integer.parseInt(youthClubMatcher.group(1)) - 1;
                        String clubName = youthClubMatcher.group(2);
                        player.updateClubName(index, clubName, ClubType.YOUTH);
                    }

                    // get college club years
                    Matcher collegeYearsMatcher = Regex.collegeYearsPattern.matcher(line);
                    if (collegeYearsMatcher.find()) {
                        int index = Integer.parseInt(collegeYearsMatcher.group(1)) - 1;
                        String yearJoined = collegeYearsMatcher.group(2);
                        String yearLeft = collegeYearsMatcher.group(3);
                        player.updateYearJoined(index, yearJoined, ClubType.COLLEGE);
                        player.updateYearLeft(index, yearLeft, ClubType.COLLEGE);
                    }

                    // get college club names
                    Matcher collegeClubMatcher = Regex.collegeClubsPattern.matcher(line);
                    if (collegeClubMatcher.find()) {
                        int index = Integer.parseInt(collegeClubMatcher.group(1)) - 1;
                        String clubName = collegeClubMatcher.group(2);
                        player.updateClubName(index, clubName, ClubType.COLLEGE);
                    }

                    // get default club years
                    Matcher yearsMatcher = Regex.yearsPattern.matcher(line);
                    if (yearsMatcher.find()) {
                        int yearsIndex = Integer.parseInt(yearsMatcher.group(1)) - 1;
                        String yearJoined = yearsMatcher.group(2);
                        String yearsLeft = yearsMatcher.group(3);
                        player.updateYearJoined(yearsIndex, yearJoined, ClubType.DEFAULT);
                        player.updateYearLeft(yearsIndex, yearsLeft, ClubType.DEFAULT);
                    }

                    // get default club names
                    Matcher clubsMatcher = Regex.clubsPattern.matcher(line);
                    if (clubsMatcher.find()) {
                        int index = Integer.parseInt(clubsMatcher.group(1)) - 1;
                        String clubName = clubsMatcher.group(2);
                        player.updateClubName(index, clubName, ClubType.DEFAULT);
                    }

                    // get national team years
                    Matcher nationalTeamsYearsMatcher = Regex.nationalYearsPattern.matcher(line);
                    if (nationalTeamsYearsMatcher.find()) {
                        int index = Integer.parseInt(nationalTeamsYearsMatcher.group(1)) - 1;
                        String yearJoined = nationalTeamsYearsMatcher.group(2);
                        String yearLeft = nationalTeamsYearsMatcher.group(3);
                        player.updateYearJoined(index, yearJoined, ClubType.NATIONAL);
                        player.updateYearLeft(index, yearLeft, ClubType.NATIONAL);
                    }

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

        if (player != null) {
            System.out.println(player);
            return player;
        }

        return null;
    }

}
