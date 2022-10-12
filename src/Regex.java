import java.util.regex.Pattern;

public class Regex {
    public static final Pattern pageStartPattern = Pattern.compile(
            "<page>",
            Pattern.CASE_INSENSITIVE
    );
    public static final Pattern pageEndPattern = Pattern.compile(
            "</page>",
            Pattern.CASE_INSENSITIVE
    );
    public static final Pattern titlePattern = Pattern.compile(
            "<title>(.*)</title>"
    );
    public static final Pattern infoboxStartPattern = Pattern.compile(
            "\\{\\{Infobox ?([\\w. ]*)",
            Pattern.CASE_INSENSITIVE
    );
    public static final Pattern startBracketsPattern = Pattern.compile(
            "\\{\\{"
    );
    public static final Pattern endBracketsPattern = Pattern.compile(
            "}}"
    );
    public static final Pattern playerCategoryPattern = Pattern.compile(
            "\\[\\[Category:.*Soccer player.*]]",
            Pattern.CASE_INSENSITIVE
    );

}
