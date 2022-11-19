package org.vinf.documents;

import org.vinf.utils.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.regex.Matcher;

public class Club extends Page {

    private String league;

    public Club(String title) {
        super(title);
        this.league = "";
    }

    public Club(String title, String text) {
        this(title);

        // parse wiki text
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // find start of infobox
                Matcher infoboxMatcher = Regex.infoboxFootballClubPattern.matcher(line);
                if (infoboxMatcher.find()) {
                    String league = "";

                    Matcher stackMatcher = Regex.bracketsStartPattern.matcher(line);
                    long stack = 0;
                    while (stackMatcher.find()) stack++;
                    while (stack > 0) {
                        // get league from infobox
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

                    // continue to read wiki text and search for league
                    while ((line = reader.readLine()) != null) {
                        // find out from the text, in what league does the club play
                        Matcher leagueMatcher = Regex.playsInPattern.matcher(line);
                        if (leagueMatcher.find()) {
                            league = leagueMatcher.group(1);
                            break;
                        }
                    }

                    this.league = league;
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isValid() {
        return !name.isEmpty() && !name.matches(Regex.digits);
    }

    public boolean playedInSameLeague(Club club) {
        return !league.isEmpty() && !club.getLeague().isEmpty() && league.equals(club.getLeague());
    }

    @Override
    public String toString() {
        return name + (league.isEmpty() ? "" : " (" + league + ")");
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        league = league.replaceAll(Regex.bracketedText, "").trim();
        if (!league.isEmpty() && !title.equals(league)) {
            this.league = league;
        }
    }

}
