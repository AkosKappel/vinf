package org.vinf.utils;

import java.util.regex.Pattern;

public final class Regex {
    private Regex() {
        throw new UnsupportedOperationException("Cannot instantiate utils.Regex class");
    }

    // Raw regex strings
    public static final String digits = "\\d+";
    public static final String nonAscii = "[^\\p{ASCII}]";
    public static final String specialCharacters = "[^\\p{L} 0-9]";
    public static final String bracketedText = "\\(.*\\)";
    public static final String multiSpace = " {2,}";
    public static final String punctuation = "\\p{P}";
    public static final String delimiterCharacters = " .,;:?!+-ā*=~_\"'`()[]{}<>/\\|^&%#@$";

    // Wiki dump regex patterns
    public static final Pattern pageStartPattern = Pattern.compile("<page>");
    public static final Pattern pageEndPattern = Pattern.compile("</page>");
    public static final Pattern textStartPattern = Pattern.compile("<text.*>");
    public static final Pattern textEndPattern = Pattern.compile("</text>");
    public static final Pattern titlePattern = Pattern.compile("<title>(.*)</title>");
    public static final Pattern bracketsStartPattern = Pattern.compile("\\{\\{");
    public static final Pattern bracketsEndPattern = Pattern.compile("}}");

    // Misc regex patterns
    public static final Pattern wikiLinkPattern = Pattern.compile(
            "\\[\\[([^]|]+)\\|?([^]|]*)]]"
    );
    public static final Pattern nonDigitTextPattern = Pattern.compile(
            "([\\p{L}\\]\\[ .ā-]*).*"
    );
    public static final Pattern playsInPattern = Pattern.compile(
            "(?:play|compete)s? in (?:the )?\\[\\[([^]|]+)\\|?([^]|]*)]]"
    );
    public static final Pattern bracketsPattern = Pattern.compile(
            "[({\\[].*[]})]"
    );
    public static final Pattern intervalPattern = Pattern.compile(
            "[\\dā-]+"
    );

    // Infobox patterns
    public static final Pattern infoboxPattern = Pattern.compile(
            "\\{\\{Infobox"
    );
    public static final Pattern infoboxPersonPattern = Pattern.compile(
            "\\{\\{Infobox person"
    );
    public static final Pattern infoboxFootballClubPattern = Pattern.compile(
            "\\{\\{Infobox football club"
    );
    public static final Pattern infoboxFootballBiographyPattern = Pattern.compile(
            "\\{\\{Infobox football biography"
    );

    // Infobox content patterns
    public static final Pattern leaguePattern = Pattern.compile(
            "\\| *league *= *([\\p{L}\\d .ā-]*)"
    );
    public static final Pattern namePattern = Pattern.compile(
            "\\| *name *= *([\\p{L}\\d\\]\\[ .ā-]*)"
    );
    public static final Pattern birthDatePattern = Pattern.compile(
            "\\| *birth_date *= *([\\p{L}\\d .ā-]*)"
    );
    public static final Pattern birthPlacePattern = Pattern.compile(
            "\\| *birth_place *= *([\\p{L}\\d .ā-]*)"
    );
    public static final Pattern positionPattern = Pattern.compile(
            "\\| *position *= *([\\p{L}\\d .ā-]*)"
    );

    // Football Biography patterns
    public static final Pattern clubNamePattern = Pattern.compile(
            "\\| *(youthclubs|college|clubs|nationalteam)(\\d+) *= *ā? *\\[?\\[?([^]|<{&]+)\\|?([^]|<{&]*)]?]?"
    );
    public static final Pattern clubYearsPattern = Pattern.compile(
            "\\| *(youthyears|collegeyears|years|nationalyears)(\\d+) *= *(\\d+)([ā-]?)(\\d+)?"
    );
    public static final Pattern clubCapsPattern = Pattern.compile(
            "\\| *(youthcaps|collegecaps|caps|nationalcaps)(\\d+) *= *(\\d+)"
    );
    public static final Pattern clubGoalsPattern = Pattern.compile(
            "\\| *(youthgoals|collegegoals|goals|nationalgoals)(\\d+) *= *(\\d+)"
    );
}
