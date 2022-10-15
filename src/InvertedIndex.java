import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

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
