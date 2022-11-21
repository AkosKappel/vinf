package org.vinf.Examples;

import org.vinf.documents.*;
import org.vinf.utils.*;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class Soccer {

    private static final SparkConf config = new SparkConf()
            .setMaster("local[*]")
            .setAppName("SoccerParser");

    private static final InvertedIndex invertedIndex = new InvertedIndex();
    private static final CommandLine cli = new CommandLine(invertedIndex);
    private static final JavaSparkContext sc = new JavaSparkContext(config);
    private static final SQLContext sqlContext = new SQLContext(sc);
    private static final StructType wikipediaXMLSchema = getSchema();

    private static final String dataFolder = "data/";
    private static final String[] xmlFiles = {
//            "soccer-clubs.xml",
//            "soccer-players.xml",
            "enwiki-latest-pages-articles1.xml",
//            "enwiki-latest-pages-articles2.xml",
//            "enwiki-latest-pages-articles3.xml",
//            "enwiki-latest-pages-articles4.xml",
//            "enwiki-latest-pages-articles5.xml",
    };

    public static void main(String[] args) {

        long startTime = System.nanoTime();

        for (String fileName : xmlFiles) {
            runSpark(dataFolder + fileName);
        }

        long endTime = System.nanoTime();
        long duration = (endTime - startTime);

        long count = invertedIndex.size();
        System.out.println("Found " + count + " documents in " + duration / 1_000_000 + " ms");

        sc.close();

//        cli.run();
    }

    private static void runSpark(String fileName) {
        JavaRDD<Row> df = sqlContext.read()
                .format("com.databricks.spark.xml")
                .option("rowTag", "page")
                .schema(wikipediaXMLSchema)
                .load(fileName)
                .javaRDD();

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

        pages.foreach(tuple -> invertedIndex.addDocument(tuple._1, tuple._2));
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
}
