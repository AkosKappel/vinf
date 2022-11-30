package org.vinf;

import org.apache.commons.io.FileUtils;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import org.vinf.documents.*;
import org.vinf.utils.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;


public class Main {

    // Spark variables
    private static JavaSparkContext sc;
    private static SQLContext sqlContext;
    private static StructType wikipediaXMLSchema;

    // Index and UI
    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine cli = new CommandLine(invertedIndex);

    public static void main(String[] args) {
        // read all XML files
        invertedIndex.index(Settings.XML_FILES);
        // stop Spark before starting the UI
        exitSpark();

        // start command line interface
        cli.help();
        cli.run();

        // stop Spark if it was used in the UI
        exitSpark();
    }

    public static void saveSpark(JavaRDD<Tuple2<Page, DocumentType>> pages) {
        // delete output folder if exists
        File outputFolder = new File(Settings.OUTPUT_FOLDER);
        if (outputFolder.exists()) {
            try {
                FileUtils.deleteDirectory(outputFolder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // save spark output
        pages.saveAsTextFile(Settings.OUTPUT_FOLDER);
    }

    public static void loadSpark() {
        // load spark output
        JavaRDD<String> output = sc.textFile(Settings.OUTPUT_FOLDER);
        System.out.println("### Found " + output.count() + " documents");
        output.foreach(str -> {
            String[] parts = str.split(",");
            if (parts.length < 1) return;
            String title = parts[0];
            System.out.println("### " + title);
        });
    }

    public static void runSpark(String fileName) {
        if (fileName == null) return;

        // check if file exists
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            System.err.println("File " + fileName + " does not exist");
            return;
        }

        if (sc == null || sqlContext == null) initSpark();

        // measure execution start time
        long startTime = System.nanoTime();

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
            if (revision == null) return null;
            GenericRowWithSchema text = revision.getAs("text");
            if (text == null) return null;
            String wikiText = text.getAs("_VALUE");

            // parse wikipedia page
            return Parser.parsePage(title, wikiText);
        }).filter(Objects::nonNull);

        if (pages == null || pages.isEmpty()) {
            System.out.println("No relevant pages found in file " + fileName);
            return;
        }

        // index all pages
        pages.foreach(tuple -> invertedIndex.addDocument(tuple._1, tuple._2));

        // measure execution end time
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        System.out.println("Found " + invertedIndex.size() + " documents in " + duration / 1_000_000 + " ms");

//        saveSpark(pages);
//        loadSpark();
    }

    public static void runSpark(String[] fileNames) {
        Arrays.stream(fileNames).forEach(Main::runSpark);
    }

    private static void initSpark() {
        if (sc == null) {
            System.out.println("Initializing spark...");
            SparkConf config = new SparkConf().setMaster(Settings.SPARK_MASTER).setAppName(Settings.APP_NAME);
            sc = new JavaSparkContext(config);
            sqlContext = new SQLContext(sc);
            wikipediaXMLSchema = getSchema();
        }
    }

    private static void exitSpark() {
        if (sc != null) {
            System.out.println("Stopping spark...");
            sc.close();
            sc = null;
            sqlContext = null;
            wikipediaXMLSchema = null;
        }
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
