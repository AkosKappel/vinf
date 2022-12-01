package org.vinf.utils;

import org.vinf.documents.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class CommandLine {

    private final InvertedIndex invertedIndex;
    private final String[] exitArgs = {"abort", "exit", "quit"};

    public CommandLine(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    /**
     * Starts the command line interface.
     */
    public void run() {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String[] input = scanner.nextLine().split(" ");

            String command = input[0].trim();
            String[] args = Arrays.copyOfRange(input, 1, input.length);

            switch (command) {
                case "-h":
                case "help":
                    help();
                    break;
                case "-s":
                case "search":
                    search(args);
                    break;
                case "-l":
                case "list":
                    list(args);
                    break;
                case "-d":
                case "display":
                case "show":
                    display(args);
                    break;
                case "count":
                    showCounts();
                    break;
                case "-t":
                case "teammates":
                    teammates(args);
                    break;
                case "-o":
                case "opponents":
                    opponents(args);
                    break;
                case "-c":
                case "clubs":
                    clubs(args);
                    break;
                case "save":
                    save(args);
                    break;
                case "load":
                    load(args);
                    break;
                case "-i":
                case "parse":
                    parse(args);
                    break;
                case "clear":
                    clear();
                    break;
                case "-q":
                case "quit":
                case "exit":
                    running = false;
                    break;
                case "":
                    break;
                default:
                    System.out.println("Unknown command: " + command);
                    break;
            }
        }
    }

    /**
     * Prints a list of all available commands.
     */
    public void help() {
        System.out.println("Commands:");
        System.out.println("  help - show this message");
        System.out.println("  search [query...] - search between all the documents");
        System.out.println("  list [players | clubs] - list all the documents containing the given type");
        System.out.println("  display [index | documents] - print inverted index or parsed documents");
        System.out.println("  count - show the number of documents");
        System.out.println("  teammates [player1], [player2] - print whether two players played together");
        System.out.println("  opponents [player1], [player2] - print whether two players played against each other");
        System.out.println("  clubs [club1], [club2] - print whether two clubs played against each other");
        System.out.println("  save [filename] - save the inverted index to a file");
        System.out.println("  load [filename] - load the inverted index from a file");
        System.out.println("  parse [filename...] - parse & index XML files");
        System.out.println("  clear - clear the inverted index");
        System.out.println("  quit - exit the application");
    }

    /**
     * Searches for the given query in the inverted index.
     *
     * @param args query to search for
     * @return a list of all pages containing the query
     */
    public ArrayList<Page> search(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: search [names...]");
            return null;
        }

        boolean verbose = false;
        if (args[0].equals("-v") || args[0].equals("--verbose")) {
            verbose = true;
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        String query = String.join(" ", args);
        ArrayList<Page> results = invertedIndex.search(query);

        if (results.size() == 0) {
            System.out.println("No results found.");
        } else {
            System.out.println("Found " + results.size() + " results:");
            for (Page page : results) {
                System.out.println(verbose ? page : page.getTitle());
            }
        }

        return results;
    }

    /**
     * Lists all the documents in the inverted index.
     *
     * @param args type of documents to list
     */
    public void list(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: list [players | clubs]");
            return;
        }

        boolean verbose = false;
        if (args[0].equals("-v") || args[0].equals("--verbose")) {
            verbose = true;
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        for (String type : args) {
            switch (type) {
                case "p":
                case "players":
                    invertedIndex.printPlayers(verbose);
                    System.out.println("Found " + invertedIndex.playersSize() + " players.");
                    break;
                case "c":
                case "clubs":
                    invertedIndex.printClubs(verbose);
                    System.out.println("Found " + invertedIndex.clubsSize() + " clubs.");
                    break;
                default:
                    System.out.println("Unknown type: " + type);
                    break;
            }
        }
    }

    /**
     * Displays the inverted index or the parsed documents.
     *
     * @param args type of data to display
     */
    public void display(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: display [index | documents]");
            return;
        }

        for (String arg : args) {
            switch (arg) {
                case "index":
                    invertedIndex.print();
                    break;
                case "docs":
                case "documents":
                    invertedIndex.printDocuments();
                    break;
                default:
                    System.out.println("Unknown argument: " + arg);
                    break;
            }
        }
    }

    /**
     * Shows the number of documents in the inverted index.
     */
    public void showCounts() {
        int nDocs = invertedIndex.size();
        int nPlayers = invertedIndex.playersSize();
        int nClubs = invertedIndex.clubsSize();

        System.out.println("Currently indexed " + nDocs + " documents (" + nPlayers + " players, " + nClubs + " clubs).");
    }

    /**
     * Prints whether two players played together.
     *
     * @param args names of the players separated by a comma
     */
    public boolean teammates(String[] args) {
        ArrayList<Page> selectedPlayers = getSelectedPlayers(args, "teammates");
        if (selectedPlayers == null) return false;

        Player player1 = (Player) selectedPlayers.get(0);
        Player player2 = (Player) selectedPlayers.get(1);

        boolean wereTeammates = player1.hasPlayedWith(player2);

        // display final result to user
        StringBuilder sb = new StringBuilder();
        sb.append(player1.getName()).append(" and ").append(player2.getName());

        if (wereTeammates) {
            ArrayList<ClubHistory> teammatesHistory = player1.getPlayedAtWith(player2);

            sb.append(" were teammates at:\n");
            for (ClubHistory history : teammatesHistory) {
                sb.append("  ").append(history.getClubName()).append(" (")
                        .append(history.getYearStart()).append(" - ")
                        .append(history.getYearEnd()).append(")\n");
            }
        } else {
            sb.append(" were never teammates.\n");
        }
        System.out.println(sb);

        return wereTeammates;
    }

    /**
     * Prints whether two players played against each other.
     *
     * @param args names of the players separated by a comma
     */
    public boolean opponents(String[] args) {
        ArrayList<Page> selectedPlayers = getSelectedPlayers(args, "opponents");
        if (selectedPlayers == null) return false;

        Player player1 = (Player) selectedPlayers.get(0);
        Player player2 = (Player) selectedPlayers.get(1);

        boolean playedAgainst = hasPlayedAgainst(player1, player2);

        // display final result to user
        System.out.println(player1.getName() + " and " + player2.getName() +
                (playedAgainst ?
                        " played against each other in " + getPlayedAtAgainst(player1, player2) :
                        " never played against each other") + ".\n");

        return playedAgainst;
    }

    /**
     * Prints whether two clubs played against each other.
     *
     * @param args names of the clubs separated by a comma
     */
    public boolean clubs(String[] args) {
        ArrayList<Page> selectedClubs = getSelectedClubs(args);
        if (selectedClubs == null) return false;

        Club club1 = (Club) selectedClubs.get(0);
        Club club2 = (Club) selectedClubs.get(1);

        boolean wereOpponents = club1.playedInSameLeague(club2);

        // display final result to user
        StringBuilder sb = new StringBuilder();
        if (wereOpponents) {
            sb.append("Both ").append(club1.getName()).append(" and ")
                    .append(club2.getName()).append(" play in ")
                    .append(club1.getLeague()).append(".");
        } else {
            sb.append(club1.getName()).append(" plays in ")
                    .append(club1.getLeague()).append(" and ")
                    .append(club2.getName()).append(" plays in ")
                    .append(club2.getLeague()).append(".");
        }
        System.out.println(sb);

        return wereOpponents;
    }

    /**
     * Saves the inverted index to a file.
     */
    public void save(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: save [filename]");
            return;
        }

        String filename = args[0];
        try {
            invertedIndex.save(filename);
            System.out.println("Saved " + invertedIndex.size() + " documents (" + invertedIndex.playersSize() +
                    " players, " + invertedIndex.clubsSize() + " clubs) into " + filename + ".");
        } catch (IOException e) {
            System.err.println("Error saving to file: " + e.getMessage());
        }
    }

    /**
     * Loads the inverted index from a file.
     */
    public void load(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: load [filename]");
            return;
        }

        String filename = args[0];
        try {
            invertedIndex.clear();
            invertedIndex.load(filename);
            System.out.println("Loaded " + invertedIndex.size() + " documents (" + invertedIndex.playersSize() +
                    " players, " + invertedIndex.clubsSize() + " clubs) from " + filename + ".");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading from file: " + e.getMessage());
        }
    }

    /**
     * Parses the given XML files and adds them to the inverted index.
     */
    public void parse(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: parse [filename...]");
            return;
        }

        for (String filename : args) {
            invertedIndex.index(filename);
            System.out.println("Indexed documents: " + invertedIndex.size() + " (" + invertedIndex.playersSize() +
                    " players, " + invertedIndex.clubsSize() + " clubs)");
        }
    }

    /**
     * Tells the inverted index to clear its data.
     */
    public void clear() {
        invertedIndex.clear();
        System.out.println("Cleared index.");
    }

    /**
     * Gets a list of soccer players based on a query.
     */
    private ArrayList<Page> getSelectedPlayers(String[] args, String command) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: " + command + " [player1], [player2]");
            return null;
        }

        String[] playersQuery = String.join(" ", args).split(",");
        if (playersQuery.length != 2) {
            System.out.println("Invalid arguments!");
            System.out.println("  Usage: " + command + " [player1], [player2]");
            return null;
        }

        ArrayList<ArrayList<Page>> allResults = new ArrayList<>();

        for (int i = 0; i < playersQuery.length; i++) {
            playersQuery[i] = playersQuery[i].trim();
            ArrayList<Page> results = invertedIndex.searchPlayers(playersQuery[i]);

            if (results.size() == 0) {
                System.out.println("No results found for '" + playersQuery[i] + "'.");
                return null;
            }

            allResults.add(results);
        }

        ArrayList<Page> selectedPlayers = new ArrayList<>();

        for (int i = 0; i < allResults.size(); i++) {
            ArrayList<Page> foundPlayers = allResults.get(i);

            if (foundPlayers.size() == 1) {
                selectedPlayers.add(foundPlayers.get(0));
            } else {
                // if multiple players are found, ask the user to select one
                System.out.println("Multiple players found with name '" + playersQuery[i] + "'.");
                System.out.println("Please select a player by typing in his number:");
                int choice = getChoiceFromList(foundPlayers);
                if (choice == -1) return null;
                selectedPlayers.add(foundPlayers.get(choice));
            }
        }

        Page player1 = selectedPlayers.get(0);
        Page player2 = selectedPlayers.get(1);

        if (player1.equals(player2)) {
            System.out.println("You entered the same player twice! Probably this is a mistake.");
            System.out.println("Please try again and input two different players.");
            return null;
        }

        return selectedPlayers;
    }

    /**
     * Gets a list of soccer clubs based on a query.
     */
    private ArrayList<Page> getSelectedClubs(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: clubs [club1], [club2]");
            return null;
        }

        String[] clubsQuery = String.join(" ", args).split(",");
        if (clubsQuery.length != 2) {
            System.out.println("Invalid arguments!");
            System.out.println("  Usage: clubs [club1], [club2]");
            return null;
        }

        ArrayList<ArrayList<Page>> allResults = new ArrayList<>();

        for (int i = 0; i < clubsQuery.length; i++) {
            clubsQuery[i] = clubsQuery[i].trim();
            ArrayList<Page> results = invertedIndex.searchClubs(clubsQuery[i]);

            if (results.size() == 0) {
                System.out.println("No results found for '" + clubsQuery[i] + "'.");
                return null;
            }

            allResults.add(results);
        }

        ArrayList<Page> selectedClubs = new ArrayList<>();

        for (int i = 0; i < allResults.size(); i++) {
            ArrayList<Page> foundClubs = allResults.get(i);

            if (foundClubs.size() == 1) {
                selectedClubs.add(foundClubs.get(0));
            } else {
                // if multiple clubs are found, ask the user to select one
                System.out.println("Multiple clubs found with name '" + clubsQuery[i] + "'.");
                System.out.println("Please select a club by typing in its number:");
                int choice = getChoiceFromList(foundClubs);
                if (choice == -1) return null;
                selectedClubs.add(foundClubs.get(choice));
            }
        }

        Page club1 = selectedClubs.get(0);
        Page club2 = selectedClubs.get(1);

        if (club1.equals(club2)) {
            System.out.println("You entered the same club twice! Probably this is a mistake.");
            System.out.println("Please try again and input two different clubs.");
            return null;
        }

        return selectedClubs;
    }

    /**
     * Asks the user to select a number from a list of options.
     */
    private int getChoiceFromList(ArrayList<Page> options) {
        for (int j = 0; j < options.size(); j++) {
            System.out.println(j + 1 + " - " + options.get(j).getTitle());
        }

        // ask user for input until a valid number is entered
        Scanner choiceScanner = new Scanner(System.in);
        while (true) {
            int choice;
            System.out.print("> ");
            try {
                String line = choiceScanner.nextLine();
                for (String exitArg : exitArgs) {
                    if (line.toLowerCase().equals(exitArg)) {
                        System.out.println("Aborted.");
                        return -1;
                    }
                }
                choice = Integer.parseInt(line) - 1;
            } catch (NumberFormatException e) {
                System.out.println("Unknown choice! Please enter a number between 1 and " + options.size() + ".");
                continue;
            }
            if (choice < 0 || choice > options.size() - 1) {
                System.out.println("Invalid choice! Try again.");
                continue;
            }
            return choice;
        }
    }

    private boolean hasPlayedAgainst(Player player1, Player player2) {
        return hasPlayedAgainst(player1, player2, ClubType.PROFESSIONAL) ||
                hasPlayedAgainst(player1, player2, ClubType.NATIONAL) ||
                hasPlayedAgainst(player1, player2, ClubType.COLLEGE) ||
                hasPlayedAgainst(player1, player2, ClubType.YOUTH);
    }

    private boolean hasPlayedAgainst(Player player1, Player player2, ClubType type) {
        ArrayList<ClubHistory> clubs1 = player1.getClubsByType(type);
        ArrayList<ClubHistory> clubs2 = player2.getClubsByType(type);

        for (ClubHistory ch1 : clubs1) {
            for (ClubHistory ch2 : clubs2) {
                String name1 = ch1.getClubName();
                String name2 = ch2.getClubName();
                if (name1 == null || name2 == null || name1.equals(name2) || !Player.yearsOverlap(ch1, ch2)) continue;

                ArrayList<Page> matches1 = invertedIndex.searchClubs(name1);
                if (matches1.size() < 1) continue;
                ArrayList<Page> matches2 = invertedIndex.searchClubs(name2);
                if (matches2.size() < 1) continue;

                Club club1 = (Club) matches1.get(0);
                Club club2 = (Club) matches2.get(0);
                if (club1.playedInSameLeague(club2)) return true;
            }
        }

        return false;
    }

    private String getPlayedAtAgainst(Player player1, Player player2) {
        String playedAt = getPlayedAtAgainst(player1, player2, ClubType.PROFESSIONAL);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtAgainst(player1, player2, ClubType.NATIONAL);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtAgainst(player1, player2, ClubType.COLLEGE);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtAgainst(player1, player2, ClubType.YOUTH);
        return playedAt;
    }

    private String getPlayedAtAgainst(Player player1, Player player2, ClubType type) {
        ArrayList<ClubHistory> clubs1 = player1.getClubsByType(type);
        ArrayList<ClubHistory> clubs2 = player2.getClubsByType(type);

        for (ClubHistory ch1 : clubs1) {
            for (ClubHistory ch2 : clubs2) {
                String name1 = ch1.getClubName();
                String name2 = ch2.getClubName();
                if (name1.equals(name2)) continue;

                ArrayList<Page> matches1 = invertedIndex.searchClubs(name1);
                if (matches1.size() < 1) continue;
                ArrayList<Page> matches2 = invertedIndex.searchClubs(name2);
                if (matches2.size() < 1) continue;

                Club club1 = (Club) matches1.get(0);
                Club club2 = (Club) matches2.get(0);
                if (club1.playedInSameLeague(club2)) return club1.getLeague();
            }
        }

        return null;
    }

}
