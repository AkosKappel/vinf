package org.vinf.utils;

public final class Settings {

    public static final String DATA_FOLDER = "./data/";
    public static final String INDEX_FOLDER = "./index/";

    public static final String[] XML_FILES = {
//            "soccer-clubs.xml", // small dataset with clubs
//            "soccer-players.xml", // small dataset with players
            "enwiki-latest-pages-articles1.xml", // first 1 GB part of the dataset
//            "enwiki-latest-pages-articles2.xml",
//            "enwiki-latest-pages-articles3.xml",
//            "enwiki-latest-pages-articles4.xml",
//            "enwiki-latest-pages-articles5.xml",
//            "enwiki-latest-pages-articles.xml", // entire dataset (more than 80 GB)
    };

    private Settings() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Settings class");
    }

}
