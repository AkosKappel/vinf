import org.apache.spark.api.java.JavaSparkContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vinf.Main;
import org.vinf.documents.Club;
import org.vinf.documents.ClubHistory;
import org.vinf.documents.Page;
import org.vinf.documents.Player;
import org.vinf.utils.CommandLine;
import org.vinf.utils.InvertedIndex;
import org.vinf.utils.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

public class TestApplication {

    InvertedIndex invertedIndex;
    CommandLine cli;
    private static final boolean USE_SPARK = false;

    @BeforeEach
    void setUp() {
        invertedIndex = new InvertedIndex();
        cli = new CommandLine(invertedIndex);
    }

    @Test
    public void testSpark() {
        Main.initSpark();
        JavaSparkContext sc = Main.getSparkContext();
        assertNotNull(sc);

        Main.exitSpark();
        sc = Main.getSparkContext();
        assertNull(sc);
    }

    @Test
    public void testPostingListIntersection() {
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
    public void testClearingInvertedIndex() {
        invertedIndex.clear();

        invertedIndex.index(Settings.DAVID_BECKHAM_XML_FILE, USE_SPARK);
        assertEquals(invertedIndex.size(), 1);

        invertedIndex.clear();
        assertEquals(invertedIndex.size(), 0);
    }

    @Test
    public void testXMLFileParsing() {
        invertedIndex.clear();
        invertedIndex.index(Settings.ENWIKI_1GB_XML_FILE, USE_SPARK);

        int nDocs = invertedIndex.size();
        int nPlayers = invertedIndex.playersSize();
        int nClubs = invertedIndex.clubsSize();

        assertEquals(nDocs, 41);
        assertEquals(nPlayers, 18);
        assertEquals(nClubs, 23);
    }

    @Test
    public void testOverlapYears() {
        invertedIndex.clear();
        invertedIndex.index(Settings.SOCCER_PLAYERS_XML_FILE, USE_SPARK);

        ArrayList<Player> players = invertedIndex.getPlayers();

        Player p1 = players.get(10); // Míchel
        Player p2 = players.get(11); // Emilio Butragueño
        Player p3 = players.get(0); // Bobby Charlton

        ClubHistory c1 = p1.getProfessionalClubs().get(0); // 1981 - 1982
        ClubHistory c2 = p2.getProfessionalClubs().get(0); // 1982 - 1984
        ClubHistory c3 = p3.getProfessionalClubs().get(0); // 1956 - 1973

        boolean answer1 = Player.yearsOverlap(c1, c2);
        assertTrue(answer1);
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() +
                " overlap with " + c2.getYearStart() + "-" + c2.getYearEnd() +
                " in " + Math.max(c1.getYearStart(), c2.getYearStart()) + "-" + Math.min(c1.getYearEnd(), c2.getYearEnd()));

        boolean answer2 = Player.yearsOverlap(c1, c3);
        assertFalse(answer2);
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() +
                " don't" + " overlap with " + c3.getYearStart() + "-" + c3.getYearEnd());
    }

    @Test
    public void testSearchingInvertedIndex() {
        invertedIndex.clear();
        invertedIndex.index(Settings.SOCCER_PLAYERS_XML_FILE, USE_SPARK);

        ArrayList<Page> results = cli.search(new String[]{"bobby"});
        assertNotNull(results);
        assertEquals(results.size(), 3);

        Page p1 = results.get(0);
        Page p2 = results.get(1);
        Page p3 = results.get(2);

        assertEquals(p1.getTitle(), "Bobby Charlton");
        assertEquals(p2.getTitle(), "Bobby Robson");
        assertEquals(p3.getTitle(), "Bobby Convey");

        results = cli.search(new String[]{"bobby", "charlton"});
        assertNotNull(results);
        assertEquals(results.size(), 1);

        p1 = results.get(0);
        assertEquals(p1.getTitle(), "Bobby Charlton");

        results = cli.search(new String[]{"messi", "ronaldo", "mbappe"});
        assertNotNull(results);
        System.out.println(results);
        assertEquals(results.size(), 0);

        results = cli.search(new String[]{"pele"});
        assertNotNull(results);
        assertEquals(results.size(), 1);

        p1 = results.get(0);
        assertEquals(p1.getTitle(), "Pelé");
    }

    @Test
    public void testSoccerPlayers() {
        invertedIndex.clear();
        invertedIndex.index(Settings.SOCCER_PLAYERS_XML_FILE, USE_SPARK);
        ArrayList<Player> players = invertedIndex.getPlayers();

        boolean answer;

        Player p1 = players.get(10); // Míchel
        Player p2 = players.get(11); // Emilio Butragueño
        Player p3 = players.get(0); // Bobby Charlton

        answer = cli.teammates(new String[]{p1.getName(), ",", p2.getName()});
        assertTrue(answer);

        answer = cli.teammates(new String[]{p1.getName(), ",", p3.getName()});
        assertFalse(answer);

        Player p4 = players.get(3); // Gordon Banks
        Player p5 = players.get(5); // Pelé

        answer = cli.opponents(new String[]{p4.getName(), ",", p5.getName()});
        assertFalse(answer); // true if using whole dataset

        Player p6 = players.get(68); // DaMarcus Beasley
        Player p7 = players.get(91); // Colin Bell

        answer = cli.opponents(new String[]{p6.getName(), ",", p7.getName()});
        assertFalse(answer);

        Player p8 = players.get(2); // David Beckham
        Player p9 = players.get(27); // Thierry Henry

        answer = cli.opponents(new String[]{p8.getName(), ",", p9.getName()});
        assertFalse(answer);
    }

    @Test
    public void testSoccerClubs() {
        invertedIndex.clear();
        invertedIndex.index(Settings.SOCCER_CLUBS_XML_FILE, USE_SPARK);
        ArrayList<Club> clubs = invertedIndex.getClubs();

        Club c1 = clubs.get(0); // Arsenal F.C. (Premier League)
        Club c2 = clubs.get(1); // AFC Ajax (Eredivisie)
        Club c3 = clubs.get(2); // AZ Alkmaar (Eredivisie)
        Club c4 = clubs.get(8); // Chelsea F.C. (Premier League)
        Club c5 = clubs.get(56); // Rosenborg BK (Eliteserien (football))
        Club c6 = clubs.get(57); // Tromsø IL (Eliteserien)

        assertFalse(c1.playedInSameLeague(c2));
        assertTrue(c2.playedInSameLeague(c3));
        assertFalse(c3.playedInSameLeague(c4));
        assertTrue(c4.playedInSameLeague(c1));
        assertTrue(c5.playedInSameLeague(c6));
    }

}
