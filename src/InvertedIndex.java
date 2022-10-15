import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;

public class InvertedIndex {
    private final HashMap<String, ArrayList<Integer>> index;
    private final HashMap<Integer, Player> documents;

    private static int documentID = 0;

    public InvertedIndex() {
        this.index = new HashMap<>();
        this.documents = new HashMap<>();
    }

    public void addDocument(Player player) {
        int docID = documentID++;
        documents.put(docID, player);

        String[] words = player.getName().split(" ");
        for (String word : words) {
            String key = Normalizer
                    .normalize(word, Normalizer.Form.NFD)
                    .replaceAll(Regex.nonAscii, "")
                    .toLowerCase();

            if (index.containsKey(key)) {
                ArrayList<Integer> docIds = index.get(key);
                if (!docIds.contains(docID)) {
                    docIds.add(docID);
                }
            } else {
                ArrayList<Integer> docIds = new ArrayList<>();
                docIds.add(docID);
                index.put(key, docIds);
            }
        }
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
