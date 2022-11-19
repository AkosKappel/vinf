package org.vinf.Examples;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;

import java.util.ArrayList;
import java.util.List;

public final class JavaSparkPi {

    public static void main(String[] args) throws Exception {
        SparkSession spark = SparkSession
                .builder()
                .master("local[*]")
                .appName("JavaSparkPi")
                .getOrCreate();

        try (JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext())) {

            int slices = 2;
            int n = 1_000_000 * slices;
            List<Integer> l = new ArrayList<>(n);
            for (int i = 0; i < n; i++) {
                l.add(i);
            }

            JavaRDD<Integer> dataSet = jsc.parallelize(l, slices);

            int count = dataSet.map(integer -> {
                double x = Math.random() * 2 - 1;
                double y = Math.random() * 2 - 1;
                return (x * x + y * y <= 1) ? 1 : 0;
            }).reduce((integer1, integer2) -> (int) integer1 + integer2);

            System.out.println("Pi is roughly " + 4.0 * count / n);

            spark.stop();
        }
    }
}