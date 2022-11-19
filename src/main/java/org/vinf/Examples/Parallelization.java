package org.vinf.Examples;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;

import java.util.Arrays;

public class Parallelization {

    private static final SparkConf conf = new SparkConf()
            .setMaster("local[*]")
            .setAppName("Parallelization");

    public static void main(String[] args) {
        int N = 1_000_000;
        int slices = 4;

        Integer[] numbers = new Integer[N];
        for (int i = 0; i < N; i++) {
            numbers[i] = i;
        }

        long sum = run(numbers, slices);
        System.out.println("Sum: " + sum);
    }

    private static long run(Integer[] numbers, int slices) {
        try (JavaSparkContext sc = new JavaSparkContext(conf)) {
            long start = System.nanoTime();
            int sum = sc.parallelize(Arrays.asList(numbers), slices)
                    .reduce((x, y) -> (int) x + y);
            long end = System.nanoTime();

            long time = (end - start) / 1_000_000;
            System.out.println("time: " + time + " ms");

            return sum;
        }
    }
}
