package documents;

import utils.Regex;

import java.util.regex.Matcher;

public class Club extends Page {

    private String name;

    public Club(String title) {
        super(title);
        Matcher nameMatcher = Regex.textPattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
