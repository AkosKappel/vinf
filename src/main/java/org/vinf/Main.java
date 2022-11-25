package org.vinf;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.vinf.documents.*;
import org.vinf.utils.*;
import scala.Tuple2;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class Main {

    // Spark configuration
    private static final SparkConf config = new SparkConf()
            .setMaster("local[*]")
            .setAppName("SoccerParser");
    private static final JavaSparkContext sc = new JavaSparkContext(config);
    private static final SQLContext sqlContext = new SQLContext(sc);
    private static final StructType wikipediaXMLSchema = getSchema();

    // Index and UI
    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine cli = new CommandLine(invertedIndex);

    public static void main(String[] args) {
        // measure execution start time
        long startTime = System.nanoTime();

        // read all XML files
//        invertedIndex.index(Settings.XML_FILES);
        runSpark(Settings.XML_FILES);

        // measure execution end time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Found " + invertedIndex.size() + " documents in " + duration / 1_000_000 + " ms");

        // start command line interface
        cli.help();
        cli.run();

        // stop Spark
        sc.close();
    }

    public static void runSpark(String fileName) {
        // check if file exists
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            System.err.println("File " + fileName + " does not exist");
            return;
        }

        // read XML file
        JavaRDD<Row> df = sqlContext.read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "page")
                .schema(wikipediaXMLSchema)
                .load(fileName)
                .javaRDD();

        // iterate over all pages and filter out relevant articles
        JavaRDD<Tuple2<Page, DocumentType>> pages = df.map(row -> {
            // get page title
            String title = row.getAs("title");

            // get wiki text
            GenericRowWithSchema revision = row.getAs("revision");
            GenericRowWithSchema text = revision.getAs("text");
            String wikiText = text.getAs("_VALUE");

            // parse wikipedia page
            return Parser.parsePage(title, wikiText);
        }).filter(Objects::nonNull);

        // index all pages
        pages.foreach(tuple -> invertedIndex.addDocument(tuple._1, tuple._2));
    }

    public static void runSpark(String[] fileNames) {
        Arrays.stream(fileNames).forEach(Main::runSpark);
    }

    private static StructType getSchema() {
        // Creates schema for the wikipedia dump XML file

        // root
        //   |-- id: long (nullable = true)
        //   |-- ns: long (nullable = true)
        //   |-- redirect: struct (nullable = true)
        //   |    |-- _VALUE: string (nullable = true)
        //   |    |-- _title: string (nullable = true)
        //   |-- revision: struct (nullable = true)
        //   |    |-- comment: string (nullable = true)
        //   |    |-- contributor: struct (nullable = true)
        //   |    |    |-- id: long (nullable = true)
        //   |    |    |-- ip: string (nullable = true)
        //   |    |    |-- username: string (nullable = true)
        //   |    |-- format: string (nullable = true)
        //   |    |-- id: long (nullable = true)
        //   |    |-- minor: string (nullable = true)
        //   |    |-- model: string (nullable = true)
        //   |    |-- parentid: long (nullable = true)
        //   |    |-- sha1: string (nullable = true)
        //   |    |-- text: struct (nullable = true)
        //   |    |    |-- _VALUE: string (nullable = true)
        //   |    |    |-- _bytes: long (nullable = true)
        //   |    |    |-- _xml:space: string (nullable = true)
        //   |    |-- timestamp: timestamp (nullable = true)
        //   |-- title: string (nullable = true)

        return DataTypes.createStructType(new StructField[]{
                DataTypes.createStructField("id", DataTypes.LongType, true),
                DataTypes.createStructField("ns", DataTypes.LongType, true),
                DataTypes.createStructField("redirect", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("_VALUE", DataTypes.StringType, true),
                        DataTypes.createStructField("_title", DataTypes.StringType, true)
                }), true),
                DataTypes.createStructField("revision", DataTypes.createStructType(new StructField[]{
                        DataTypes.createStructField("comment", DataTypes.StringType, true),
                        DataTypes.createStructField("contributor", DataTypes.createStructType(new StructField[]{
                                DataTypes.createStructField("id", DataTypes.LongType, true),
                                DataTypes.createStructField("ip", DataTypes.StringType, true),
                                DataTypes.createStructField("username", DataTypes.StringType, true)
                        }), true),
                        DataTypes.createStructField("format", DataTypes.StringType, true),
                        DataTypes.createStructField("id", DataTypes.LongType, true),
                        DataTypes.createStructField("minor", DataTypes.StringType, true),
                        DataTypes.createStructField("model", DataTypes.StringType, true),
                        DataTypes.createStructField("parentid", DataTypes.LongType, true),
                        DataTypes.createStructField("sha1", DataTypes.StringType, true),
                        DataTypes.createStructField("text", DataTypes.createStructType(new StructField[]{
                                DataTypes.createStructField("_VALUE", DataTypes.StringType, true),
                                DataTypes.createStructField("_bytes", DataTypes.LongType, true),
                                DataTypes.createStructField("_xml:space", DataTypes.StringType, true)
                        }), true),
                        DataTypes.createStructField("timestamp", DataTypes.TimestampType, true)
                }), true),
                DataTypes.createStructField("title", DataTypes.StringType, true)
        });
    }

    private static void tests() {
        testsInvertedIndex();
        testPlayers();
        testClubs();
    }

    private static void testsInvertedIndex() {
        ArrayList<Integer> list1 = new ArrayList<>(Arrays.asList(1, 3, 12, 23));
        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(1, 2, 3, 5, 10, 23));
        ArrayList<Integer> list3 = new ArrayList<>(Arrays.asList(1, 2));

        ArrayList<ArrayList<Integer>> lists1 = new ArrayList<>();
        lists1.add(list1);
        lists1.add(list2);
        lists1.add(list3);

        System.out.println(invertedIndex.intersect(list1, list2));
        System.out.println(invertedIndex.intersect(lists1));
    }

    private static void testPlayers() {
        ArrayList<Player> players = invertedIndex.getPlayers();

        Player p1 = players.get(10); // Míchel
        Player p2 = players.get(11); // Emilio Butragueño
        Player p3 = players.get(0); // Bobby Charlton

        ClubHistory c1 = p1.getProfessionalClubs().get(0);
        ClubHistory c2 = p2.getProfessionalClubs().get(0);
        ClubHistory c3 = p3.getProfessionalClubs().get(0);

        String ans = Player.yearsOverlap(c1, c2) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c2.getYearStart() + "-" + c2.getYearEnd());

        ans = Player.yearsOverlap(c1, c3) ? "" : " don't";
        System.out.println("Years " + c1.getYearStart() + "-" + c1.getYearEnd() + ans + " overlap with " + c3.getYearStart() + "-" + c3.getYearEnd());

        cli.teammates(new String[]{p1.getName(), ",", p2.getName()});
        cli.teammates(new String[]{p1.getName(), ",", p3.getName()});

//        for (int i = 0; i < players.size(); i++) {
//            System.out.println(players.get(i).getName() + " - " + i);
//        }

        Player p4 = players.get(67); // Brad Friedel
        Player p5 = players.get(90); // Bruce Grobbelaar

        cli.opponents(new String[]{p4.getName(), ",", p5.getName()});

        Player p6 = players.get(68); // DaMarcus Beasley
        Player p7 = players.get(91); // Colin Bell

        cli.opponents(new String[]{p6.getName(), ",", p7.getName()});

        Player p8 = players.get(2); // David Beckham
        Player p9 = players.get(27); // Thierry Henry

        cli.opponents(new String[]{p8.getName(), ",", p9.getName()});
    }

    private static void testClubs() {
        ArrayList<Club> clubs = invertedIndex.getClubs();

        Club c1 = clubs.get(0); // Arsenal F.C. (Premier League)
        Club c2 = clubs.get(1); // AFC Ajax (Eredivisie)
        Club c3 = clubs.get(2); // AZ Alkmaar (Eredivisie)
        Club c4 = clubs.get(8); // Chelsea F.C. (Premier League)
        Club c5 = clubs.get(56); // Rosenborg BK (Eliteserien (football))
        Club c6 = clubs.get(57); // Tromsø IL (Eliteserien)

        System.out.println(c1.playedInSameLeague(c2)); // false
        System.out.println(c2.playedInSameLeague(c3)); // true
        System.out.println(c3.playedInSameLeague(c4)); // false
        System.out.println(c4.playedInSameLeague(c1)); // true
        System.out.println(c5.playedInSameLeague(c6)); // true
    }

}
