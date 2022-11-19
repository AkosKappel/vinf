package org.vinf.documents;

import org.vinf.utils.Regex;

import java.util.regex.Matcher;

public abstract class Page {

    String title;
    String name;

    protected Page(String title) {
        this.title = title;
        Matcher nameMatcher = Regex.nonDigitTextPattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;
    }

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
