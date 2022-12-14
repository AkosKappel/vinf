package org.vinf.documents;

import org.vinf.utils.Regex;

import java.io.Serializable;
import java.util.regex.Matcher;

public abstract class Page implements Serializable {

    private static final long serialVersionUID = 1L;

    String title;
    String name;

    protected Page(String title) {
        this.title = title;
        Matcher nameMatcher = Regex.nonDigitTextPattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;
    }

    /**
     * Checks if the page is considered a valid soccer player or club.
     */
    public abstract boolean isValid();

    @Override
    public String toString() {
        return "Page(" + title + ')';
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
