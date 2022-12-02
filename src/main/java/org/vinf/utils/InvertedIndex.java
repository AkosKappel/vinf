package org.vinf.utils;

import org.vinf.Main;
import org.vinf.documents.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.*;

public class InvertedIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    private HashMap<String, ArrayList<Integer>> index;
    private HashMap<Integer, Page> playerDocuments;
    private HashMap<Integer, Page> clubDocuments;

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

    /**
     * Tokenizes a string by removing all special characters and splitting
     * the string into words.
     *
     * @param text string to tokenize
     */
    private String[] tokenize(String text) {
        return text
                .replaceAll(Regex.specialCharacters, "")
                .trim()
                .split(" ");
    }

    /**
     * Normalizes a string by replacing non english characters with their
     * english equivalent. For example, "á" is replaced with "a".
     *
     * @param text string to normalize
     */
    private String normalize(String text) {
        return Normalizer
                .normalize(text, Normalizer.Form.NFD)
                .replaceAll(Regex.nonAscii, "")
                .toLowerCase()
                .trim();
    }

    /**
     * Adds parsed wikipedia pages into the inverted index.
     */
    public void addDocuments(ArrayList<? extends Page> documents, DocumentType type) {
        for (Page document : documents) {
            addDocument(document, type);
        }
    }

    /**
     * Adds a parsed wikipedia page into the inverted index based on its type.
     */
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

    /**
     * Adds a parsed wikipedia pages into the inverted index.
     */
    public void addDocument(Page document) {
        if (document instanceof Player) {
            addDocument(document, DocumentType.PLAYER);
        } else if (document instanceof Club) {
            addDocument(document, DocumentType.CLUB);
        } else {
            throw new IllegalArgumentException("Invalid document type: " + document.getClass());
        }
    }

    /**
     * Returns a list of pages that contain a given query.
     *
     * @param query query to search for
     * @return list of pages from the inverted index that contain the given words
     */
    public ArrayList<Page> search(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        if (intersection == null) return results;

        for (Integer docId : intersection) {
            if (playerDocuments.containsKey(docId)) {
                results.add(playerDocuments.get(docId));
            } else if (clubDocuments.containsKey(docId)) {
                results.add(clubDocuments.get(docId));
            }
        }

        return results;
    }

    /**
     * Returns a list of soccer players that contain a given query.
     *
     * @param query query to search for
     * @return list of players from the inverted index whose name contains the given words
     */
    public ArrayList<Page> searchPlayers(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        if (intersection == null) return results;

        for (Integer docId : intersection) {
            if (playerDocuments.containsKey(docId)) {
                results.add(playerDocuments.get(docId));
            }
        }

        return results;
    }

    /**
     * Returns a list of soccer clubs that contain a given query.
     *
     * @param query query to search for
     * @return list of clubs from the inverted index whose title contains the given words
     */
    public ArrayList<Page> searchClubs(String query) {
        ArrayList<Page> results = new ArrayList<>();
        ArrayList<Integer> intersection = processQuery(query);

        if (intersection == null) return results;

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
            } else {
                return null; // one of the words is not in the index (no results, because AND operator)
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

    public void printDocuments(boolean verbose) {
        printPlayers(verbose);
        printClubs(verbose);
    }

    public void printPlayers(boolean verbose) {
        for (int id : playerDocuments.keySet()) {
            if (verbose) {
                System.out.println(id + "\n" + playerDocuments.get(id));
            } else {
                System.out.println(id + " - " + playerDocuments.get(id).getTitle());
            }
        }
    }

    public void printClubs(boolean verbose) {
        for (int id : clubDocuments.keySet()) {
            if (verbose) {
                System.out.println(id + "\n" + clubDocuments.get(id));
            } else {
                System.out.println(id + " - " + clubDocuments.get(id).getTitle());
            }
        }
    }

    public ArrayList<Page> getDocuments() {
        ArrayList<Page> documents = new ArrayList<>();
        documents.addAll(clubDocuments.values());
        documents.addAll(playerDocuments.values());
        return documents;
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

    /**
     * Serializes the inverted index to a file.
     *
     * @param filename The name of the file to serialize to.
     * @throws IOException If an I/O error occurs.
     */
    public void save(String filename) throws IOException {
        if (!filename.endsWith(".dat")) filename += ".dat";

        // create index folder if it doesn't exist
        File indexFolder = new File(Settings.INDEX_FOLDER);
        if (!indexFolder.exists()) {
            boolean success = indexFolder.mkdir();
            if (!success) {
                throw new IOException("Failed to create folder: " + Settings.INDEX_FOLDER);
            }
        }

        // serialize index
        try (ObjectOutputStream out = new ObjectOutputStream(Files.newOutputStream(Paths.get(Settings.INDEX_FOLDER + filename)))) {
            out.writeObject(this);
        }
    }

    /**
     * Deserializes an inverted index from a file.
     *
     * @param filename The name of the file to deserialize from.
     * @throws IOException            If an I/O error occurs.
     * @throws ClassNotFoundException If the class of a serialized object cannot be found.
     */
    public void load(String filename) throws IOException, ClassNotFoundException {
        if (!filename.endsWith(".dat")) filename += ".dat";
        // deserialize index
        try (ObjectInputStream in = new ObjectInputStream(Files.newInputStream(Paths.get(Settings.INDEX_FOLDER + filename)))) {
            InvertedIndex obj = (InvertedIndex) in.readObject();
            this.index = obj.index;
            this.clubDocuments = obj.clubDocuments;
            this.playerDocuments = obj.playerDocuments;
        }
    }

    /**
     * Parse and index a XML file.
     *
     * @param xmlFile  The XML file to parse and index.
     * @param useSpark Whether to use Spark's distributed computing.
     */
    public void index(String xmlFile, boolean useSpark) {
        if (!xmlFile.endsWith(".xml")) xmlFile += ".xml";

        // parse XML file
        if (useSpark) {
            Main.runSpark(xmlFile);
        } else {
            Map<String, ArrayList<Page>> docs = Parser.parseXML(xmlFile);
            if (docs == null) return;
            ArrayList<Page> players = docs.get("players");
            ArrayList<Page> clubs = docs.get("clubs");

            // build inverted index
            addDocuments(players, DocumentType.PLAYER);
            addDocuments(clubs, DocumentType.CLUB);
        }
    }

    /**
     * Calls 'index' with useSpark from the settings.
     *
     * @param xmlFile The XML filename to parse and index.
     */
    public void index(String xmlFile) {
        index(xmlFile, Settings.USE_DISTRIBUTED);
    }

    /**
     * Calls 'index' for every given XML file.
     *
     * @param xmlFiles Array of XML filenames to parse and index.
     */
    public void index(String[] xmlFiles) {
        for (String xmlFile : xmlFiles) {
            index(xmlFile);
        }
    }

    /**
     * Empties the inverted index.
     */
    public void clear() {
        index.clear();
        clubDocuments.clear();
        playerDocuments.clear();
    }
}
