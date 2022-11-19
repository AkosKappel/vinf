package org.vinf.utils;

import org.vinf.documents.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

public class Parser {

    private Parser() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Parser class");
    }

    public static Map<String, ArrayList<Page>> parseXML(String filePath) {
        ArrayList<Page> clubs = new ArrayList<>();
        ArrayList<Page> players = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // check if page starts with <page>
                if (Regex.pageStartPattern.matcher(line).find()) {
                    // reset page variables
                    StringBuilder pageBuilder = new StringBuilder();
                    boolean isSoccerPlayer = false;
                    boolean isSoccerClub = false;

                    do {
                        if (!isSoccerPlayer && Regex.infoboxFootballBiographyPattern.matcher(line).find()) {
                            isSoccerPlayer = true;
                        } else if (!isSoccerClub && Regex.infoboxFootballClubPattern.matcher(line).find()) {
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

                    // filter out soccer players and clubs
                    if (isSoccerPlayer) {
                        Page player = parsePlayer(pageBuilder);
                        if (player != null) players.add(player);
                    } else if (isSoccerClub) {
                        Page club = parseClub(pageBuilder);
                        if (club != null) clubs.add(club);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // return players and clubs
        Map<String, ArrayList<Page>> pages = new HashMap<>();
        pages.put("players", players);
        pages.put("clubs", clubs);
        return pages;
    }

    private static Player parsePlayer(StringBuilder page) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(page.toString()))) {
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

                    Matcher stackMatcher = Regex.bracketsStartPattern.matcher(line);
                    long stack = 0;
                    while (stackMatcher.find()) stack++;
                    while (stack > 0) {
                        // get club names and years
                        player.findClubYears(line);
                        player.findClubNames(line);

                        // read next line of infobox
                        line = reader.readLine();
                        if (line == null) break;

                        // count opening brackets
                        stackMatcher = Regex.bracketsStartPattern.matcher(line);
                        while (stackMatcher.find()) stack++;

                        // count closing brackets
                        stackMatcher = Regex.bracketsEndPattern.matcher(line);
                        while (stackMatcher.find()) stack--;
                    }

                    // return player if he has played for at least one club
                    return player.isValid() ? player : null;
                }
            }
        }

        // no player was found
        return null;
    }

    private static Club parseClub(StringBuilder page) throws IOException {
        try (BufferedReader reader = new BufferedReader(new StringReader(page.toString()))) {
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

                    Matcher stackMatcher = Regex.bracketsStartPattern.matcher(line);
                    long stack = 0;
                    while (stackMatcher.find()) stack++;
                    while (stack > 0) {
                        // extract attributes from infobox
                        Matcher leagueMatcher = Regex.leaguePattern.matcher(line);
                        if (league.isEmpty() && leagueMatcher.find()) {
                            league = leagueMatcher.group(1);
                        }

                        // read next line of infobox
                        line = reader.readLine();
                        if (line == null) break;

                        // count opening brackets
                        stackMatcher = Regex.bracketsStartPattern.matcher(line);
                        while (stackMatcher.find()) stack++;

                        // count closing brackets
                        stackMatcher = Regex.bracketsEndPattern.matcher(line);
                        while (stackMatcher.find()) stack--;
                    }

                    // read text inside page
                    while ((line = reader.readLine()) != null) {
                        // find out from the text, in what league does the club play
                        Matcher leagueMatcher = Regex.playsInPattern.matcher(line);
                        if (leagueMatcher.find()) {
                            league = leagueMatcher.group(1);
                            break;
                        }
                    }

                    club.setLeague(league);
                    return club.isValid() ? club : null;
                }
            }
        }

        // no club was found
        return null;
    }

}
