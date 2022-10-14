import java.util.ArrayList;

public class Player {

    private String name;
    private ArrayList<ClubHistory> clubs;

    public Player(String name) {
        this.name = name;
        this.clubs = new ArrayList<>();
    }

    public void updateClubName(int clubIndex, String clubName) {
        while (clubs.size() <= clubIndex) {
            clubs.add(new ClubHistory());
        }
        clubs.get(clubIndex).setClubName(clubName);
    }

    public void updateYearJoined(int clubIndex, int yearJoined) {
        while (clubs.size() <= clubIndex) {
            clubs.add(new ClubHistory());
        }
        clubs.get(clubIndex).setYearJoined(yearJoined);
    }

    public void updateYearJoined(int clubIndex, String yearJoined) {
        if (yearJoined != null) {
            updateYearJoined(clubIndex, Integer.parseInt(yearJoined));
        } else {
            updateYearJoined(clubIndex, 0);
        }
    }

    public void updateYearLeft(int clubIndex, int yearLeft) {
        while (clubs.size() <= clubIndex) {
            clubs.add(new ClubHistory());
        }
        clubs.get(clubIndex).setYearLeft(yearLeft);
    }

    public void updateYearLeft(int clubIndex, String yearLeft) {
        if (yearLeft != null) {
            updateYearLeft(clubIndex, Integer.parseInt(yearLeft));
        } else {
            updateYearLeft(clubIndex, clubs.get(clubIndex).getYearJoined());
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append("\n");
        for (ClubHistory club : clubs) {
            sb.append("\t").append(club).append("\n");
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ClubHistory> getClubs() {
        return clubs;
    }

    public void setClubs(ArrayList<ClubHistory> clubs) {
        this.clubs = clubs;
    }
}
