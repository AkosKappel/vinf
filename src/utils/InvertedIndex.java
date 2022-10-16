package utils;

import documents.Player;
import utils.Regex;

import java.text.Normalizer;
import java.util.*;

public class InvertedIndex {
    private final HashMap<String, ArrayList<Integer>> index;
    private final HashMap<Integer, Player> documents;

    private static int DOCUMENT_ID = 0;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.documents = new HashMap<>();
    }

    private String[] tokenize(String text) {
        return text
                .replaceAll(Regex.specialCharacter, "")
                .split(" ");
    }

    private String normalize(String text) {
        return Normalizer
                .normalize(text, Normalizer.Form.NFD)
                .replaceAll(Regex.nonAscii, "")
                .toLowerCase();
    }

    public void addDocument(Player player) {
        int docId = DOCUMENT_ID++;
        documents.put(docId, player);

        String[] words = tokenize(normalize(player.getName()));
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

    // TODO: remake this method
    public ArrayList<Player> search(String query) {
        ArrayList<Integer> docIds = new ArrayList<>();
        String[] words = tokenize(normalize(query));

        for (String word : words) {
            if (index.containsKey(word)) {
                ArrayList<Integer> ids = index.get(word);
                for (Integer id : ids) {
                    if (!docIds.contains(id)) {
                        docIds.add(id);
                    }
                }
            }
        }

        ArrayList<Player> results = new ArrayList<>();
        for (Integer docId : docIds) {
            results.add(documents.get(docId));
        }

        return results;
    }

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

    public ArrayList<Integer> intersect(ArrayList<Integer>... postingLists) {
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
        for (int id : documents.keySet()) {
            System.out.println(id + "\n" + documents.get(id));
        }
    }

}
