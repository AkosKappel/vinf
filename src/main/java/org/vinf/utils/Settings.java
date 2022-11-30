package org.vinf.utils;

import java.time.Year;

public final class Settings {

    public static final String APP_NAME = "SoccerParser";
    public static final String SPARK_MASTER = "local[*]";
    public static final boolean USE_DISTRIBUTED = true;

    public static final String DATA_FOLDER = "./data/";
    public static final String INDEX_FOLDER = "./index/";
    public static final String OUTPUT_FOLDER = "./output/output.txt";

    public static final String SOCCER_PLAYERS_XML_FILE = DATA_FOLDER + "soccer-players.xml";
    public static final String SOCCER_CLUBS_XML_FILE = DATA_FOLDER + "soccer-clubs.xml";
    public static final String ENWIKI_BASE = DATA_FOLDER + "enwiki-latest-pages-articles";
    public static final String ENWIKI_XML_FILE = ENWIKI_BASE + ".xml";
    public static final String ENWIKI_1_XML_FILE = ENWIKI_BASE + "1.xml";
    public static final String ENWIKI_2_XML_FILE = ENWIKI_BASE + "2.xml";
    public static final String ENWIKI_3_XML_FILE = ENWIKI_BASE + "3.xml";
    public static final String ENWIKI_4_XML_FILE = ENWIKI_BASE + "4.xml";
    public static final String ENWIKI_5_XML_FILE = ENWIKI_BASE + "5.xml";

    public static final String[] XML_FILES = {
//            DATA_FOLDER + "soccer-player-exception.xml",
//            SOCCER_PLAYERS_XML_FILE, // small dataset with players
//            SOCCER_CLUBS_XML_FILE, // small dataset with clubs
//            ENWIKI_1_XML_FILE, // first 1 GB part of the dataset
//            ENWIKI_2_XML_FILE,
//            ENWIKI_3_XML_FILE,
//            ENWIKI_4_XML_FILE,
//            ENWIKI_5_XML_FILE,
//            ENWIKI_XML_FILE, // entire dataset (more than 90 GB)
    };

    public static final String CURRENT_YEAR = String.valueOf(Year.now().getValue());

    private Settings() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Settings class");
    }

}
