package utils;

import documents.Club;
import documents.ClubType;
import documents.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Parser {

    private Parser() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Parser class");
    }

    public static ArrayList<Club> parseClubs(String filePath) {
        ArrayList<Club> clubs = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // check if page starts with <page>
                if (Regex.pageStartPattern.matcher(line).find()) {
                    // reset page variables
                    StringBuilder pageBuilder = new StringBuilder();
                    boolean isSoccerClub = false;

                    do {
                        if (!isSoccerClub && Regex.infoboxFootballClubPattern.matcher(line).find()) {
                            isSoccerClub = true;
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

                    // filter out soccer clubs
                    if (isSoccerClub) {
                        Club c = parseClub(pageBuilder.toString());
                        if (c != null) clubs.add(c);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return clubs;
    }

    public static ArrayList<Player> parsePlayers(String filePath) {
        ArrayList<Player> players = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
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
                        Player p = parsePlayer(pageBuilder.toString());
                        if (p != null) players.add(p);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return players;
    }

    private static Club parseClub(String page) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(page))) {
            String title = "";

            String line;
            while ((line = reader.readLine()) != null) {
                // get title from page
                Matcher titleMatcher = Regex.titlePattern.matcher(line);
                if (title.isEmpty() && titleMatcher.find()) {
                    title = titleMatcher.group(1);
                }

                Matcher infoboxMatcher = Regex.infoboxFootballClubPattern.matcher(line);
                if (infoboxMatcher.find()) {
                    Club club = new Club(title);
                    String league = "";

                    long stack = Regex.bracketsStartPattern.matcher(line).results().count();
                    while (stack > 0) {
                        // extract attributes from infobox
                        Matcher leagueMatcher = Regex.leaguePattern.matcher(line);
                        if (league.isEmpty() && leagueMatcher.find()) {
                            league = leagueMatcher.group(1);
                        }

                        // read next line of infobox
                        line = reader.readLine();
                        if (line == null) break;
                        stack += Regex.bracketsStartPattern.matcher(line).results().count();
                        stack -= Regex.bracketsEndPattern.matcher(line).results().count();
                    }

                    // read text
                    while (league.isEmpty()) {
                        line = reader.readLine();
                        if (line == null) break;

                        // find out from the text, in what league does the club play
                        Matcher leagueMatcher = Regex.clubPattern.matcher(line);
                        if (leagueMatcher.find()) {
                            league = leagueMatcher.group(1);
                            break;
                        }
                    }

                    club.setLeague(league);
                    if (club.isValid()) return club;
                }
            }
        }

        // no club was found
        return null;
    }

    private static Player parsePlayer(String page) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(page))) {
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
                    Player player = new Player(title);

                    long stack = Regex.bracketsStartPattern.matcher(line).results().count();
                    while (stack > 0) {
                        // get club names and years
                        findClubYears(line, player);
                        findClubNames(line, player);

                        // read next line of infobox
                        line = reader.readLine();
                        if (line == null) break;
                        stack += Regex.bracketsStartPattern.matcher(line).results().count();
                        stack -= Regex.bracketsEndPattern.matcher(line).results().count();
                    }

                    // return player if he has played for at least one club
                    if (player.isValid()) return player;
                }
            }
        }

        // no player was found
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

}
