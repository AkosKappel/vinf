import org.junit.jupiter.api.Test;
import org.vinf.documents.ClubHistory;
import org.vinf.documents.Player;
import org.vinf.utils.CommandLine;
import org.vinf.utils.InvertedIndex;
import org.vinf.utils.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestInvertedIndex {

    @Test
    public void testPostingListIntersection() {
        InvertedIndex invertedIndex = new InvertedIndex();

        ArrayList<Integer> list1 = new ArrayList<>(Arrays.asList(1, 3, 12, 23));
        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 10, 23));
        ArrayList<Integer> list3 = new ArrayList<>(Arrays.asList(1, 2));
        ArrayList<Integer> list4 = new ArrayList<>(Arrays.asList(10, 20, 30, 40, 50));

        ArrayList<ArrayList<Integer>> listGroup1 = new ArrayList<>();
        listGroup1.add(list1);
        listGroup1.add(list2);
        listGroup1.add(list3);

        ArrayList<Integer> intersection1 = invertedIndex.intersect(list1, list2);
        ArrayList<Integer> solution1 = new ArrayList<>(Arrays.asList(1, 3, 23));

        ArrayList<Integer> intersection2 = invertedIndex.intersect(list2, list3);
        ArrayList<Integer> solution2 = new ArrayList<>(Arrays.asList(1, 2));

        ArrayList<Integer> intersection3 = invertedIndex.intersect(list3, list4);
        ArrayList<Integer> solution3 = new ArrayList<>();

        ArrayList<Integer> intersection4 = invertedIndex.intersect(listGroup1);
        ArrayList<Integer> solution4 = new ArrayList<>(Collections.singletonList(1));

        assertEquals(intersection1, solution1);
        assertEquals(intersection2, solution2);
        assertEquals(intersection3, solution3);
        assertEquals(intersection4, solution4);
    }

    @Test
    public void testXMLFileParsing() {
        InvertedIndex invertedIndex = new InvertedIndex();
        invertedIndex.index(Settings.SOCCER_PLAYERS_XML_FILE, false);

        ArrayList<Player> players = invertedIndex.getPlayers();

        Player p1 = players.get(10); // Míchel
        Player p2 = players.get(11); // Emilio Butragueño
        Player p3 = players.get(0); // Bobby Charlton

        ClubHistory c1 = p1.getProfessionalClubs().get(0);
        ClubHistory c2 = p2.getProfessionalClubs().get(0);
        ClubHistory c3 = p3.getProfessionalClubs().get(0);

        boolean answer1 = Player.yearsOverlap(c1, c2);
        boolean solution1 = true;

        boolean answer2 = Player.yearsOverlap(c1, c3);
        boolean solution2 = false;

        assertEquals(answer1, solution1);
        assertEquals(answer2, solution2);

        CommandLine cli = new CommandLine(invertedIndex);

//        cli.teammates(new String[]{p1.getName(), ",", p2.getName()});
//        cli.teammates(new String[]{p1.getName(), ",", p3.getName()});
//
//        Player p4 = players.get(67); // Brad Friedel
//        Player p5 = players.get(90); // Bruce Grobbelaar
//
//        cli.opponents(new String[]{p4.getName(), ",", p5.getName()});
//
//        Player p6 = players.get(68); // DaMarcus Beasley
//        Player p7 = players.get(91); // Colin Bell
//
//        cli.opponents(new String[]{p6.getName(), ",", p7.getName()});
//
//        Player p8 = players.get(2); // David Beckham
//        Player p9 = players.get(27); // Thierry Henry
//
//        cli.opponents(new String[]{p8.getName(), ",", p9.getName()});
    }

}
