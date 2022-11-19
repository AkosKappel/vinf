package org.vinf.Examples;

import org.vinf.documents.*;
import org.vinf.utils.*;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.catalyst.expressions.GenericRowWithSchema;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.Function1;
import scala.Tuple2;

import java.util.Objects;

public class Soccer {

    private static final SparkConf conf = new SparkConf()
            .setMaster("local[*]")
            .setAppName("XML Parser");

    public static void main(String[] args) {
//        String fileName = "./data/soccer-players.xml";
        String fileName = "./data/soccer-clubs.xml";
//        String fileName = "./data/enwiki-latest-pages-articles1.xml";

        try (JavaSparkContext sc = new JavaSparkContext(conf)) {
            SQLContext sqlContext = new SQLContext(sc);
            StructType schema = getSchema();

            JavaRDD<Row> df = sqlContext.read()
                    .format("com.databricks.spark.xml")
                    .option("rowTag", "page")
                    .schema(schema)
                    .load(fileName)
                    .javaRDD();

            // parallelize the data TODO: try if it's faster
//            df = df.repartition(4);

            JavaRDD<Page> pages = df.map(row -> {
                // get page title
                String title = row.getAs("title");

                // get wiki text
                GenericRowWithSchema revision = row.getAs("revision");
                GenericRowWithSchema text = revision.getAs("text");
                String wikiText = text.getAs("_VALUE");

                // count lines in wiki text
                int lines = wikiText.split("\n").length;
                int length = wikiText.length();

//                System.out.println(title + ", length = " + length + ", lines = " + lines);
//                System.out.println(row.schema());

                Page page = Parser.parsePage(title, wikiText);
                return page;
            }).filter(Objects::nonNull);

//            pages.foreach(page -> {
//                System.out.print("# Page: ");
//                System.out.println(page);
//            });

            long count = pages.count();
            System.out.println("Found " + count + " documents");
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
}