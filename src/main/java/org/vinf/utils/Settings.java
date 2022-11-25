package org.vinf.utils;

public final class Settings {

    public static final String DATA_FOLDER = "./data/";
    public static final String INDEX_FOLDER = "./index/";

    public static final String[] XML_FILES = {
            DATA_FOLDER + "soccer-clubs.xml", // small dataset with clubs
            DATA_FOLDER + "soccer-players.xml", // small dataset with players
//            DATA_FOLDER + "enwiki-latest-pages-articles1.xml", // first 1 GB part of the dataset
//            DATA_FOLDER + "enwiki-latest-pages-articles2.xml",
//            DATA_FOLDER + "enwiki-latest-pages-articles3.xml",
//            DATA_FOLDER + "enwiki-latest-pages-articles4.xml",
//            DATA_FOLDER + "enwiki-latest-pages-articles5.xml",
//            DATA_FOLDER + "enwiki-latest-pages-articles.xml", // entire dataset (more than 80 GB)
    };

    public static final boolean USE_DISTRIBUTED = true;

    private Settings() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Settings class");
    }

}
