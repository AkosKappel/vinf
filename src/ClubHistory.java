public class ClubHistory {
    private String clubName;
    private int yearJoined;
    private int yearLeft;

    public ClubHistory() {
        this.clubName = null;
        this.yearJoined = 0;
        this.yearLeft = 0;
    }

    @Override
    public String toString() {
        if (clubName == null) return "";
        else if (yearJoined == 0) return clubName;
        else return clubName + " | (" + yearJoined + " - " + yearLeft + ")";
    }

    public String getClubName() {
        return clubName;
    }

    public void setClubName(String clubName) {
        this.clubName = clubName;
    }

    public int getYearJoined() {
        return yearJoined;
    }

    public void setYearJoined(int yearJoined) {
        this.yearJoined = yearJoined;
    }

    public int getYearLeft() {
        return yearLeft;
    }

    public void setYearLeft(int yearLeft) {
        this.yearLeft = yearLeft;
    }
}
