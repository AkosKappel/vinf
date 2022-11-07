package utils;

import documents.Page;
import documents.Player;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

public class Parser {

    private Parser() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Parser class");
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

    // TODO: parse clubs
    public static void parseClubs(String filePath) {
    }

}
