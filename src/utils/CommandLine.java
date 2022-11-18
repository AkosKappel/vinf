package utils;

import documents.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class CommandLine {

    private final InvertedIndex invertedIndex;
    private final String[] exitArgs = {"exit", "quit", "q"};

    public CommandLine(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public void run() {
        Scanner scanner = new Scanner(System.in);

        boolean running = true;
        while (running) {
            System.out.print("> ");
            String[] input = scanner.nextLine().split(" ");

            String command = input[0];
            String[] args = Arrays.copyOfRange(input, 1, input.length);

            switch (command) {
                case "-h", "help" -> help();
                case "-s", "search" -> search(args);
                case "-l", "list" -> list(args);
                case "-d", "display", "show" -> display(args);
                case "-t", "teammates" -> teammates(args);
                case "-o", "opponents" -> opponents(args);
                case "-c", "clubs" -> clubs(args);
                case "-q", "quit", "exit" -> running = false;
                default -> System.out.println("Unknown command: " + command);
            }
        }
    }

    public void help() {
        System.out.println("Commands:");
        System.out.println("  help - show this message");
        System.out.println("  search [query...] - search between all the documents");
        System.out.println("  list [players | clubs] - list all the documents containing the given type");
        System.out.println("  display [index | documents] - print inverted index or parsed documents");
        System.out.println("  teammates [player1], [player2] - print whether two players played together");
        System.out.println("  opponents [player1], [player2] - print whether two players played against each other");
        System.out.println("  clubs [club1] [club2] - print whether two clubs played against each other");
        System.out.println("  quit - exit the application");
    }

    public void search(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: search [names...]");
            return;
        }

        String query = String.join(" ", args);
        ArrayList<Page> results = invertedIndex.search(query);

        if (results.size() == 0) {
            System.out.println("No results found.");
        } else {
            System.out.println("Found " + results.size() + " results:");
            for (Page page : results) {
                System.out.println(page);
            }
        }
    }

    public void list(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: list [players | clubs]");
            return;
        }

        String type = args[0];
        switch (type) {
            case "p", "players" -> {
                invertedIndex.printPlayers();
                ArrayList<Player> players = invertedIndex.getPlayers();
                System.out.println("Found " + players.size() + " players.");
            }
            case "c", "clubs" -> {
                invertedIndex.printClubs();
                ArrayList<Club> clubs = invertedIndex.getClubs();
                System.out.println("Found " + clubs.size() + " clubs.");
            }
            default -> System.out.println("Unknown type: " + type);
        }
    }

    public void display(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: show [index | documents]");
            return;
        }

        for (String arg : args) {
            switch (arg) {
                case "index" -> invertedIndex.print();
                case "docs", "documents" -> invertedIndex.printDocuments();
                default -> System.out.println("Unknown argument: " + arg);
            }
        }
    }

    public void teammates(String[] args) {
        ArrayList<Page> selectedPlayers = getSelectedPlayers(args, "teammates");
        if (selectedPlayers == null) return;

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
    }

    public void opponents(String[] args) {
        ArrayList<Page> selectedPlayers = getSelectedPlayers(args, "opponents");
        if (selectedPlayers == null) return;

        Player player1 = (Player) selectedPlayers.get(0);
        Player player2 = (Player) selectedPlayers.get(1);

        boolean playedAgainst = hasPlayedAgainst(player1, player2);

        // display final result to user
        System.out.println(player1.getName() + " and " + player2.getName() +
                (playedAgainst ?
                        " played against each other in " + getPlayedAtAgainst(player1, player2) :
                        " never played against each other") + ".\n");
    }

    public void clubs(String[] args) {
        ArrayList<Page> selectedClubs = getSelectedClubs(args);
        if (selectedClubs == null) return;

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
    }

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
                for (int j = 0; j < foundPlayers.size(); j++) {
                    System.out.println(j + 1 + " - " + foundPlayers.get(j).getName());
                }

                // ask user for input until a valid number is entered
                Scanner choiceScanner = new Scanner(System.in);
                int choice;
                while (true) {
                    System.out.print("> ");
                    try {
                        String line = choiceScanner.nextLine();
                        for (String exitArg : exitArgs) {
                            if (line.toLowerCase().equals(exitArg)) {
                                return null;
                            }
                        }
                        choice = Integer.parseInt(line) - 1;
                    } catch (NumberFormatException e) {
                        System.out.println("Unknown choice! Please enter a number between 1 and " + foundPlayers.size() + ".");
                        continue;
                    }
                    if (choice < 0 || choice > foundPlayers.size() - 1) {
                        System.out.println("Invalid choice! Try again.");
                        continue;
                    }
                    break;
                }

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
                for (int j = 0; j < foundClubs.size(); j++) {
                    System.out.println(j + 1 + " - " + foundClubs.get(j).getName());
                }

                // ask user for input until a valid number is entered
                Scanner choiceScanner = new Scanner(System.in);
                int choice;
                while (true) {
                    System.out.print("> ");
                    try {
                        String line = choiceScanner.nextLine();
                        for (String exitArg : exitArgs) {
                            if (line.toLowerCase().equals(exitArg)) {
                                return null;
                            }
                        }
                        choice = Integer.parseInt(line) - 1;
                    } catch (NumberFormatException e) {
                        System.out.println("Unknown choice! Please enter a number between 1 and " + foundClubs.size() + ".");
                        continue;
                    }
                    if (choice < 0 || choice > foundClubs.size() - 1) {
                        System.out.println("Invalid choice! Try again.");
                        continue;
                    }
                    break;
                }

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
                if (name1.equals(name2) || !Player.yearsOverlap(ch1, ch2)) continue;

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
