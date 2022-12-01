package org.vinf.utils;

import java.time.Year;

public final class Settings {

    public static final String APP_NAME = "SoccerParser";
    public static final String SPARK_MASTER = "local[*]";
    public static final boolean USE_DISTRIBUTED = true;

    public static final String DATA_FOLDER = "./data/";
    public static final String INDEX_FOLDER = "./index/";
    public static final String OUTPUT_FOLDER = "./output/output.txt";

    public static final String SOCCER_PLAYERS_XML_FILE = DATA_FOLDER + "soccer-players.xml"; // small dataset with players
    public static final String SOCCER_CLUBS_XML_FILE = DATA_FOLDER + "soccer-clubs.xml"; // small dataset with clubs
    public static final String DAVID_BECKHAM_XML_FILE = DATA_FOLDER + "DavidBeckham.xml";
    public static final String MANCHESTER_UNITED_XML_FILE = DATA_FOLDER + "ManchesterUnited.xml";
    private static final String _ENWIKI_BASE = DATA_FOLDER + "enwiki-latest-pages-articles";
    public static final String ENWIKI_XML_FILE = _ENWIKI_BASE + ".xml"; // entire dataset (more than 90 GB)
    public static final String ENWIKI_1GB_XML_FILE = _ENWIKI_BASE + "1.xml"; // first 1 GB part of the dataset

    public static String ENWIKI_XML_FILE_PART(int i) {
        return _ENWIKI_BASE + i + ".xml";
    }

    public static final String[] XML_FILES = {
//            DATA_FOLDER + "soccer-player-exception.xml",
//            SOCCER_PLAYERS_XML_FILE,
//            SOCCER_CLUBS_XML_FILE,
//            DAVID_BECKHAM_XML_FILE,
//            MANCHESTER_UNITED_XML_FILE,
//            ENWIKI_1_XML_FILE,
//            ENWIKI_XML_FILE_PART(1),
//            ENWIKI_XML_FILE_PART(2),
//            ENWIKI_XML_FILE_PART(3),
//            ENWIKI_XML_FILE_PART(4),
//            ENWIKI_XML_FILE_PART(5),
//            ENWIKI_XML_FILE,
    };

    public static final String CURRENT_YEAR = String.valueOf(Year.now().getValue());

    private Settings() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Settings class");
    }

}
