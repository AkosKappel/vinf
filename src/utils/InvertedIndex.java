package utils;

import documents.Person;

import java.text.Normalizer;
import java.util.*;

public class InvertedIndex {
    private final HashMap<String, ArrayList<Integer>> index;
    private final HashMap<Integer, Person> documents;

    private static int DOCUMENT_ID = 0;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.documents = new HashMap<>();
    }

    public int size() {
        return documents.size();
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

    public void addDocuments(ArrayList<Person> documents) {
        for (Person document : documents) {
            addDocument(document);
        }
    }

    public void addDocument(Person document) {
        int docId = DOCUMENT_ID++;
        documents.put(docId, document);

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

    public ArrayList<Person> search(String query) {
        String[] words = tokenize(normalize(query));

        ArrayList<ArrayList<Integer>> postingLists = new ArrayList<>();
        for (String word : words) {
            if (index.containsKey(word)) {
                ArrayList<Integer> postingList = index.get(word);
                postingLists.add(postingList);
            }
        }

        ArrayList<Integer> intersection = intersect(postingLists);
        ArrayList<Person> results = new ArrayList<>();

        for (Integer docId : intersection) {
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
        for (int id : documents.keySet()) {
            System.out.println(id + "\n" + documents.get(id));
        }
    }

}
