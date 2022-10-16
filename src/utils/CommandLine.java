package utils;

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

    private void help() {
        System.out.println("Commands:");
        System.out.println("  help - show this message");
        System.out.println("  search [names...] - search for players");
        System.out.println("  display [index|documents] - print inverted index or parsed documents");
        System.out.println("  teammates [player1], [player2] - print teammates of a player");
        System.out.println("  exit - exit the application");
    }

    private void search(String[] args) {
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

    private void display(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: show [index|documents]");
            return;
        }

        for (String arg : args) {
            switch (arg) {
                case "index" -> invertedIndex.print();
                case "documents" -> invertedIndex.printDocuments();
                default -> System.out.println("Unknown argument: " + arg);
            }
        }
    }

    private void teammates(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing argument!");
            System.out.println("  Usage: teammates [player1], [player2]");
            return;
        }

        String[] players = String.join(" ", args).split(",");
        if (players.length != 2) {
            System.out.println("Invalid argument!");
            System.out.println("  Usage: teammates [player1], [player2]");
            return;
        }

        Player player1 = (Player) invertedIndex.search(players[0].trim()).get(0);
        Player player2 = (Player) invertedIndex.search(players[1].trim()).get(0);

        boolean wereTeammates = player1.hasPlayedWith(player2);

        if (wereTeammates) {
            System.out.println(player1.getName() + " and " + player2.getName() + " were teammates at " + player1.getPlayedAtWith(player2));
        } else {
            System.out.println(player1.getName() + " and " + player2.getName() + " were never teammates");
        }
    }

}
