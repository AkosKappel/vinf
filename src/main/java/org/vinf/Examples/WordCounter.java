package org.vinf.Examples;

import org.apache.commons.io.FileUtils;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.SparkConf;
import scala.Tuple2;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class WordCounter {

    public static void main(String[] args) {
        String fileName = "./data/ezo.txt";
        String outputFolder = "./data/output/WordCount";

        // Delete output folder if exists
        File file = new File(outputFolder);
        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //Create a SparkContext to initialize
        SparkConf conf = new SparkConf().setMaster("local").setAppName("Word Count");

        // Create a Java version of the Spark Context
        JavaSparkContext sc = new JavaSparkContext(conf);

        // Load the text into a Spark RDD, which is a distributed representation of each line of text
        JavaRDD<String> textFile = sc.textFile(fileName);
        JavaPairRDD<String, Integer> counts = textFile
                .flatMap(line -> Arrays.asList(line.split("[ ,]")).iterator())
                .mapToPair(word -> new Tuple2<>(word, 1))
                .reduceByKey((a, b) -> (int) a + b)
                .mapToPair(Tuple2::swap)
                .sortByKey(false)
                .mapToPair(Tuple2::swap);

        System.out.println("Total words: " + counts.count());
        for (Tuple2<String, Integer> tuple : counts.collect()) {
            System.out.println(tuple._1() + " " + tuple._2());
        }
//        counts.saveAsTextFile(outputFolder);

        // Stop the Spark Context
        sc.stop();
    }

}