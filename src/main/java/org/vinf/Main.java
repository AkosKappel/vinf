package org.vinf;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import org.vinf.documents.*;
import org.vinf.utils.*;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;


public class Main {

    // Spark variables
    private static JavaSparkContext sc;
    private static SparkSession spark;
    private static StructType wikipediaXMLSchema;

    // Index and UI
    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine cli = new CommandLine(invertedIndex);

    /**
     * Main entry point of the application.
     *
     * @param args command line arguments
     */
    public static void main(String[] args) {
        // read all XML files
        invertedIndex.index(Settings.XML_FILES);
        // stop Spark before starting the UI
        exitSpark();

        // start command line interface
        cli.help();
        cli.run();
        cli.clearIndex();

        // stop Spark if it was used in the UI
        exitSpark();
    }

    /**
     * Reads XML file into a JavaRDD object. Then, iterates over all pages
     * using Spark's map() function. Each page is then searched and parsed
     * for soccer players or clubs. If a player or club is found, it is added
     * to the inverted index.
     * The function also measures the time it takes to parse and index all pages.
     *
     * @param fileName XML file to read
     */
    public static void runSpark(String fileName) {
        if (fileName == null) return;

        // check if file exists
        File file = new File(fileName);
        if (!file.exists() || !file.isFile()) {
            System.err.println("File " + fileName + " does not exist");
            return;
        }

        if (sc == null || spark == null) initSpark();

        // measure execution start time
        long startTime = System.nanoTime();

        // read XML file
        JavaRDD<Row> df = spark.read()
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
    }

    /**
     * Calls 'runSpark()' for every XML file.
     *
     * @param fileNames array of XML filenames to read
     */
    public static void runSpark(String[] fileNames) {
        Arrays.stream(fileNames).forEach(Main::runSpark);
    }

    /**
     * Initializes Spark and the Wikipedia XML schema.
     */
    public static void initSpark() {
        if (sc == null) {
            System.out.println("Initializing spark...");
            SparkConf sparkConfig = new SparkConf()
                    .setMaster(Settings.SPARK_MASTER)
                    .setAppName(Settings.APP_NAME);
            sc = new JavaSparkContext(sparkConfig);
            spark = SparkSession
                    .builder()
                    .config(sparkConfig)
                    .getOrCreate();
            wikipediaXMLSchema = getSchema();
        }
    }

    /**
     * Stops Spark.
     */
    public static void exitSpark() {
        if (sc != null) {
            System.out.println("Stopping spark...");
            sc.close();
            sc = null;
            spark.close();
            spark = null;
            wikipediaXMLSchema = null;
        }
    }

    /**
     * Returns the Wikipedia pre-defined XML schema for english Wikipedia.
     *
     * @return the Wikipedia XML schema
     */
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

    public static JavaSparkContext getSparkContext() {
        return sc;
    }

}
