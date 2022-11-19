package org.vinf.utils;

import org.vinf.documents.*;

import java.text.Normalizer;
import java.util.*;

public class InvertedIndex {

    private final HashMap<String, ArrayList<Integer>> index;
    private final HashMap<Integer, Page> playerDocuments;
    private final HashMap<Integer, Page> clubDocuments;

    private static int DOCUMENT_ID = 0;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.playerDocuments = new HashMap<>();
        this.clubDocuments = new HashMap<>();
    }

    public int size() {
        return playerDocuments.size() + clubDocuments.size();
    }

    public int playersSize() {
        return playerDocuments.size();
    }

    public int clubsSize() {
        return clubDocuments.size();
    }

    private String[] tokenize(String text) {
        return text
                .replaceAll(Regex.specialCharacters, "")
                .trim()
                .split(" ");
    }

    private String normalize(String text) {
        return Normalizer
                .normalize(text, Normalizer.Form.NFD)
                .replaceAll(Regex.nonAscii, "")
                .toLowerCase()
                .trim();
    }

    public void addDocuments(ArrayList<? extends Page> documents, DocumentType type) {
        for (Page document : documents) {
            addDocument(document, type);
        }
    }

    public void addDocument(Page document, DocumentType type) {
        int docId = DOCUMENT_ID++;

        switch (type) {
            case PLAYER:
                playerDocuments.put(docId, document);
                break;
            case CLUB:
                clubDocuments.put(docId, document);
                break;
            default:
                throw new IllegalArgumentException("Invalid document type: " + type);
        }

        String[] words = tokenize(normalize(document.getName()));
        for (String word : words) {
            if (index.containsKey(word)) {
                ArrayList<Integer> docIds = index.get(word);
                if (!docIds.contains(docId)) {
                    docIds.add(docId);
                }
            } else {
                ArrayList<Integer> docIds = new ArrayList<>();
                docIds.add(docId);
                index.put(word, docIds);
            }
        }
    }

    public ArrayList<Page> search(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        for (Integer docId : intersection) {
            if (playerDocuments.containsKey(docId)) {
                results.add(playerDocuments.get(docId));
            } else if (clubDocuments.containsKey(docId)) {
                results.add(clubDocuments.get(docId));
            }
        }

        return results;
    }

    public ArrayList<Page> searchPlayers(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        for (Integer docId : intersection) {
            if (playerDocuments.containsKey(docId)) {
                results.add(playerDocuments.get(docId));
            }
        }

        return results;
    }

    public ArrayList<Page> searchClubs(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        for (Integer docId : intersection) {
            if (clubDocuments.containsKey(docId)) {
                results.add(clubDocuments.get(docId));
            }
        }

        return results;
    }

    /**
     * Creates a posting list for all the words in the query
     * and finds their intersection.
     *
     * @param query The query to process.
     * @return The intersection of all the posting lists.
     */
    private ArrayList<Integer> processQuery(String query) {
        String[] words = tokenize(normalize(query));

        ArrayList<ArrayList<Integer>> postingLists = new ArrayList<>();
        for (String word : words) {
            if (index.containsKey(word)) {
                ArrayList<Integer> postingList = index.get(word);
                postingLists.add(postingList);
            }
        }

        return intersect(postingLists);
    }

    /**
     * Finds the intersection of exactly two posting lists.
     *
     * @param postingList1 The first posting list.
     * @param postingList2 The second posting list.
     * @return The intersection of the two posting lists.
     */
    public ArrayList<Integer> intersect(ArrayList<Integer> postingList1, ArrayList<Integer> postingList2) {
        ArrayList<Integer> intersection = new ArrayList<>();

        int i = 0, j = 0;
        int size1 = postingList1.size(), size2 = postingList2.size();
        Integer docId1 = postingList1.get(i), docId2 = postingList2.get(j);

        while (i < size1 && j < size2) {
            if (docId1.equals(docId2)) {
                intersection.add(docId1);
                if (++i < size1) docId1 = postingList1.get(i);
                if (++j < size2) docId2 = postingList2.get(j);
            } else if (docId1 < docId2) {
                if (++i < size1) docId1 = postingList1.get(i);
            } else {
                if (++j < size2) docId2 = postingList2.get(j);
            }
        }

        return intersection;
    }

    /**
     * Finds the intersection of all the posting lists.
     *
     * @param postingLists List of the posting lists to intersect.
     * @return The intersection of all the posting lists.
     */
    public ArrayList<Integer> intersect(ArrayList<ArrayList<Integer>> postingLists) {
        if (postingLists.size() == 0) {
            return new ArrayList<>();
        }

        // sort posting lists by size
        postingLists.sort(Comparator.comparingInt(ArrayList::size));

        // intersect the smallest posting list with the rest
        ArrayList<Integer> intersection = postingLists.get(0);

        int i = 1;
        while (i < postingLists.size() && !intersection.isEmpty()) {
            intersection = intersect(intersection, postingLists.get(i));
            i++;
        }

        return intersection;
    }

    @SafeVarargs
    public final ArrayList<Integer> intersect(ArrayList<Integer>... postingLists) {
        // sort posting lists by size
        Arrays.sort(postingLists, Comparator.comparingInt(ArrayList::size));

        // intersect the smallest posting list with the rest
        ArrayList<Integer> intersection = postingLists[0];

        int i = 1;
        while (i < postingLists.length && !intersection.isEmpty()) {
            intersection = intersect(intersection, postingLists[i]);
            i++;
        }

        return intersection;
    }

    public void print() {
        for (String word : index.keySet()) {
            System.out.println(word + ": " + index.get(word));
        }
    }

    public void printDocuments() {
        for (int id : clubDocuments.keySet()) {
            System.out.println(id + "\n" + clubDocuments.get(id));
        }
        for (int id : playerDocuments.keySet()) {
            System.out.println(id + "\n" + playerDocuments.get(id));
        }
    }

    public void printPlayers() {
        for (int id : playerDocuments.keySet()) {
            System.out.println(id + "\n" + playerDocuments.get(id));
        }
    }

    public void printClubs() {
        for (int id : clubDocuments.keySet()) {
            System.out.println(id + "\n" + clubDocuments.get(id));
        }
    }

    public ArrayList<Club> getClubs() {
        ArrayList<Club> clubs = new ArrayList<>();
        for (int id : clubDocuments.keySet()) {
            clubs.add((Club) clubDocuments.get(id));
        }
        return clubs;
    }

    public ArrayList<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        for (int id : playerDocuments.keySet()) {
            players.add((Player) playerDocuments.get(id));
        }
        return players;
    }

}
