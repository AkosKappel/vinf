import java.util.regex.Pattern;

public class Regex {
    public static final Pattern pageStartPattern = Pattern.compile(
            "<page>"
    );
    public static final Pattern pageEndPattern = Pattern.compile(
            "</page>"
    );
    public static final Pattern titlePattern = Pattern.compile(
            "<title>(.*)</title>"
    );
    public static final Pattern infoboxPattern = Pattern.compile(
            "\\{\\{Infobox ?([\\w .-]*)"
    );
    public static final Pattern infoboxPersonPattern = Pattern.compile(
            "\\{\\{Infobox ?person"
    );
    public static final Pattern infoboxFootballBiographyPattern = Pattern.compile(
            "\\{\\{Infobox ?football biography"
    );
    public static final Pattern bracketsStartPattern = Pattern.compile(
            "\\{\\{"
    );
    public static final Pattern bracketsEndPattern = Pattern.compile(
            "}}"
    );
    public static final Pattern categorySoccerPlayerPattern = Pattern.compile(
            "\\[\\[Category:.*Soccer player.*]]", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern wikiLinkPattern = Pattern.compile(
            "\\[\\[([^]|]+)\\|?([^]|]*)]]"
    );
    public static final Pattern playerNamePattern = Pattern.compile(
            "([\\p{L}\\d\\]\\[ .-]*).*"
    );
    public static final Pattern namePattern = Pattern.compile(
            "\\| *name *= *([\\p{L}\\d\\]\\[ .-]*)"
    );
    public static final Pattern birthDatePattern = Pattern.compile(
            "\\| *birth_date *= *([\\p{L}\\d .-]*)"
    );
    public static final Pattern birthPlacePattern = Pattern.compile(
            "\\| *birth_place *= *([\\p{L}\\d .-]*)"
    );
    public static final Pattern positionPattern = Pattern.compile(
            "\\| *position *= *([\\p{L}\\d .-]*)"
    );
    public static final Pattern clubNamePattern = Pattern.compile(
            "\\| *(youthclubs|college|clubs|nationalteam)(\\d+) *= *→? *\\[?\\[?([^]|]+)\\|?([^]|]*)]?]?"
    );
    public static final Pattern clubYearsPattern = Pattern.compile(
            "\\| *(youthyears|collegeyears|years|nationalyears)(\\d+) *= *(\\d+)[–-]?(\\d+)?"
    );
    public static final Pattern clubCapsPattern = Pattern.compile(
            "\\| *(youthcaps|collegecaps|caps|nationalcaps)(\\d+) *= *(\\d+)"
    );
    public static final Pattern clubGoalsPattern = Pattern.compile(
            "\\| *(youthgoals|collegegoals|goals|nationalgoals)(\\d+) *= *(\\d+)"
    );
}
