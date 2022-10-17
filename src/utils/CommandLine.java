package utils;

import documents.ClubHistory;
import documents.Person;
import documents.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public final class CommandLine {

    private final InvertedIndex invertedIndex;

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
                case "-q", "quit", "exit" -> running = false;
                default -> System.out.println("Unknown command: " + command);
            }
        }
    }

    public void help() {
        System.out.println("Commands:");
        System.out.println("  help - show this message");
        System.out.println("  search [names...] - search for players");
        System.out.println("  display [index|documents] - print inverted index or parsed documents");
        System.out.println("  teammates [player1], [player2] - print teammates of a player");
        System.out.println("  exit - exit the application");
    }

    public void search(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: search [names...]");
            return;
        }

        String query = String.join(" ", args);
        ArrayList<Person> results = invertedIndex.search(query);

        if (results.size() == 0) {
            System.out.println("No results found");
        } else {
            System.out.println("Found " + results.size() + " results");
            for (Person person : results) {
                System.out.println(person);
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

        String[] players = String.join(" ", args).split(",");
        if (players.length != 2) {
            System.out.println("Invalid arguments!");
            System.out.println("  Usage: teammates [player1], [player2]");
            return;
        }

        for (int i = 0; i < players.length; i++) {
            players[i] = players[i].trim();
        }

        ArrayList<Person> results1 = invertedIndex.search(players[0]);
        if (results1.size() == 0) {
            System.out.println("No results found for " + players[0]);
            return;
        }

        ArrayList<Person> results2 = invertedIndex.search(players[1]);
        if (results2.size() == 0) {
            System.out.println("No results found for " + players[1]);
            return;
        }

        Player player1 = (Player) results1.get(0);
        Player player2 = (Player) results2.get(0);

        boolean wereTeammates = player1.hasPlayedWith(player2);

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

}
