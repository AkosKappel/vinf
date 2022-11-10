package documents;

import utils.Regex;

import java.util.regex.Matcher;

public class Club extends Page {

    private String league;

    public Club(String title) {
        super(title);
        Matcher nameMatcher = Regex.textPattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;
        this.league = "";
    }

    @Override
    public boolean isValid() {
        return !name.isEmpty() && !name.matches(Regex.digits);
    }

    @Override
    public String toString() {
        return name + (league.isEmpty() ? "" : " (" + league + ")");
    }

    public String getLeague() {
        return league;
    }

    public void setLeague(String league) {
        if (!league.isEmpty() && !title.equals(league)) {
            this.league = league;
        }
    }

}
