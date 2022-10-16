package utils;

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
                case "help" -> help();
                case "search" -> search(args);
                case "show" -> show(args);
                case "exit" -> running = false;
                default -> System.out.println("Unknown command: " + command);
            }
        }

    }

    private void help() {
        System.out.println("Commands:");
        System.out.println("  help - show this message");
        System.out.println("  search [names...] - search for players");
        System.out.println("  show [index|documents] - print inverted index or parsed documents");
        System.out.println("  exit - exit the application");
    }

    private void search(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument");
            help();
            return;
        }

        String query = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        ArrayList<Player> results = invertedIndex.search(query);

        if (results.size() == 0) {
            System.out.println("No results found");
        } else {
            System.out.println("Found " + results.size() + " results");
            for (Player player : results) {
                System.out.println(player);
            }
        }
    }

    private void show(String[] args) {
        if (args.length < 1) {
            System.out.println("Missing argument");
            help();
            return;
        }

        switch (args[0]) {
            case "index" -> invertedIndex.print();
            case "documents" -> invertedIndex.printDocuments();
            default -> System.out.println("Unknown argument: " + args[0]);
        }
    }

}
