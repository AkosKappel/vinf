import java.util.regex.Pattern;

// TODO: remove Pattern.CASE_INSENSITIVE where not necessary
public class Regex {
    public static final Pattern pageStartPattern = Pattern.compile(
            "<page>", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern pageEndPattern = Pattern.compile(
            "</page>", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern titlePattern = Pattern.compile(
            "<title>(.*)</title>"
    );
    public static final Pattern infoboxStartPattern = Pattern.compile(
            "\\{\\{Infobox ?([\\w .-]*)", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern bracketsStartPattern = Pattern.compile(
            "\\{\\{"
    );
    public static final Pattern bracketsEndPattern = Pattern.compile(
            "}}"
    );
    public static final Pattern playerCategoryPattern = Pattern.compile(
            "\\[\\[Category:.*Soccer player.*]]", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern wikiLinkPattern = Pattern.compile(
            "\\[\\[([^]|]+)\\|?([^]|]*)]]"
    );
    public static final Pattern namePattern = Pattern.compile(
            "\\| *name *= *([\\w .-]*)", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern birthDatePattern = Pattern.compile(
            "\\| *birth_date *= *([\\w .-]*)", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern birthPlacePattern = Pattern.compile(
            "\\| *birth_place *= *([\\w .-]*)", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern positionPattern = Pattern.compile(
            "\\| *position *= *([\\w .-]*)", Pattern.CASE_INSENSITIVE
    );
    // | years1         = 1956–1973 |clubs1 = [[Manchester United F.C.|Manchester United]] |caps1 = 606 |goals1 = 199
    // | years2         = 1974–1975 |clubs2 = [[Preston North End|Preston North End]]      |caps2 = 38  |goals2 = 8
    // | years3         = 1976      |clubs3 = → [[Waterford F.C.]] (loan)                  |caps3 = 3   |goals3 = 1
    // | years4         = 1978      |clubs4 = [[Newcastle KB United]]                      |caps4 = 1   |goals4 = 0
    // match the above patterns
    public static final Pattern yearsPattern = Pattern.compile(
            "\\| *years(\\d+) *= *(\\d+)[–-]?(\\d+)?", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern clubsPattern = Pattern.compile(
            "\\| *clubs(\\d+) *= *→? *\\[?\\[?([^]|]+)\\|?([^]|]*)]?]?", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern capsPattern = Pattern.compile(
            "\\| *caps(\\d+) *= *(\\d+)", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern goalsPattern = Pattern.compile(
            "\\| *goals(\\d+) *= *(\\d+)", Pattern.CASE_INSENSITIVE
    );
    // | youthyears1     = 1988      |youthclubs1 = Ridgeway Rovers
    // | youthyears2     = 1987–1991 |youthclubs2 = [[Tottenham Hotspur F.C. Under-23s and Academy|Tottenham Hotspur]]
    // | youthyears3     = 1989–1991 |youthclubs3 = → [[Brimsdown Rovers F.C.|Brimsdown Rovers]] (loan)
    // | youthyears4     = 1991–1994 |youthclubs4 = [[Manchester United F.C. Reserves and Academy|Manchester United]]
    public static final Pattern youthYearsPattern = Pattern.compile(
            "\\| *youthyears(\\d+) *= *(\\d+)[–-]?(\\d+)?", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern youthClubsPattern = Pattern.compile(
            "\\| *youthclubs(\\d+) *= *→? *\\[?\\[?([^]|]+)\\|?([^]|]*)]?]?", Pattern.CASE_INSENSITIVE
    );
    // | nationalyears1  = 1992–1993 |nationalteam1 = [[England national under-18 football team|England U18]] |nationalcaps1 = 3   |nationalgoals1 = 0
    // | nationalyears2  = 1994–1996 |nationalteam2 = [[England national under-21 football team|England U21]] |nationalcaps2 = 9   |nationalgoals2 = 0
    // | nationalyears3  = 1996–2009 |nationalteam3 = → [[England national football team|England]] (loan)     |nationalcaps3 = 115 |nationalgoals3 = 17
    // | nationalyears4  = 2010      |nationalteam4 = [[England national football team|England]]              |nationalcaps4 = 169 |nationalgoals4 = 42
    public static final Pattern nationalYearsPattern = Pattern.compile(
            "\\| *nationalyears(\\d+) *= *(\\d+)[–-]?(\\d+)?", Pattern.CASE_INSENSITIVE
    );
    public static final Pattern nationalTeamPattern = Pattern.compile(
            "\\| *nationalteam(\\d+) *= *\\[?\\[?([^]|]+)\\|?([^]|]*)]?]?", Pattern.CASE_INSENSITIVE
    );
}
