package utils;

import documents.Club;
import documents.ClubHistory;
import documents.Page;
import documents.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

@SuppressWarnings("DuplicatedCode")
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

    public void display(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: show [index|documents]");
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
        if (args.length == 0) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: teammates [player1], [player2]");
            return;
        }

        String[] playersQuery = String.join(" ", args).split(",");
        if (playersQuery.length != 2) {
            System.out.println("Invalid arguments!");
            System.out.println("  Usage: teammates [player1], [player2]");
            return;
        }

        ArrayList<ArrayList<Page>> allResults = new ArrayList<>();

        for (int i = 0; i < playersQuery.length; i++) {
            playersQuery[i] = playersQuery[i].trim();
            ArrayList<Page> results = invertedIndex.searchPlayers(playersQuery[i]);

            if (results.size() == 0) {
                System.out.println("No results found for '" + playersQuery[i] + "'.");
                return;
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
                                return;
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

        Player player1 = (Player) selectedPlayers.get(0);
        Player player2 = (Player) selectedPlayers.get(1);

        if (player1.equals(player2)) {
            System.out.println("You entered the same player twice! Probably this is a mistake.");
            System.out.println("Please try again and input two different players.");
            return;
        }

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
            sb.append(" were never teammates\n");
        }
        System.out.println(sb);
    }

    private void opponents(String[] args) {
        // TODO
    }

    private void clubs(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: clubs [club1], [club2]");
            return;
        }

        String[] clubsQuery = String.join(" ", args).split(",");
        if (clubsQuery.length != 2) {
            System.out.println("Invalid arguments!");
            System.out.println("  Usage: clubs [club1], [club2]");
            return;
        }

        ArrayList<ArrayList<Page>> allResults = new ArrayList<>();

        for (int i = 0; i < clubsQuery.length; i++) {
            clubsQuery[i] = clubsQuery[i].trim();
            ArrayList<Page> results = invertedIndex.searchClubs(clubsQuery[i]);

            if (results.size() == 0) {
                System.out.println("No results found for '" + clubsQuery[i] + "'.");
                return;
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
                                return;
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

        Club club1 = (Club) selectedClubs.get(0);
        Club club2 = (Club) selectedClubs.get(1);

        if (club1.equals(club2)) {
            System.out.println("You entered the same club twice! Probably this is a mistake.");
            System.out.println("Please try again and input two different clubs.");
            return;
        }

        boolean wereOpponents = club1.hasPlayedAgainst(club2);

        // TODO
//        // display final result to user
//        StringBuilder sb = new StringBuilder();
//        sb.append(club1.getName()).append(" and ").append(club2.getName());
//
//        if (wereOpponents) {
//            ArrayList<ClubHistory> opponentsHistory = club1.getPlayedAtAgainst(club2);
//
//            sb.append(" were opponents at:\n");
//            for (ClubHistory history : opponentsHistory) {
//                sb.append("  ").append(history.getClubName()).append(" (")
//                        .append(history.getYearStart()).append(" - ")
//                        .append(history.getYearEnd()).append(")\n");
//            }
//        } else {
//            sb.append(" were never opponents\n");
//        }
//        System.out.println(sb);
    }

}
