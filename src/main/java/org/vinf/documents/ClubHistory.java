package org.vinf.documents;

import java.io.Serializable;

public class ClubHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    private String clubName;
    private int yearStart;
    private int yearEnd;

    public ClubHistory() {
        this.clubName = null;
        this.yearStart = 0;
        this.yearEnd = 0;
    }

    public ClubHistory(String clubName, int yearStart, int yearEnd) {
        this.clubName = clubName;
        this.yearStart = yearStart;
        this.yearEnd = yearEnd;
    }

    @Override
    public String toString() {
        if (clubName == null) return "";
        else if (yearStart == 0) return clubName;
        else return clubName + " | (" + yearStart + " - " + yearEnd + ")";
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public int getYearStart() {
        return yearStart;
    }

    public void setYearStart(int yearStart) {
        this.yearStart = yearStart;
    }

    public int getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(int yearEnd) {
        this.yearEnd = yearEnd;
    }
}
