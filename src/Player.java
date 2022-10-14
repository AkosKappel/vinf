import java.util.ArrayList;

public class Player {

    private String name;
    private ArrayList<ClubHistory> youthClubs;
    private ArrayList<ClubHistory> collegeClubs;
    private ArrayList<ClubHistory> clubs;
    private ArrayList<ClubHistory> nationalTeams;

    public Player(String name) {
        this.name = name;
        this.youthClubs = new ArrayList<>();
        this.collegeClubs = new ArrayList<>();
        this.clubs = new ArrayList<>();
        this.nationalTeams = new ArrayList<>();
    }

    private ArrayList<ClubHistory> getClubListByType(ClubType type) {
        return switch (type) {
            case YOUTH -> youthClubs;
            case COLLEGE -> collegeClubs;
            case DEFAULT -> clubs;
            case NATIONAL -> nationalTeams;
            default -> null;
        };
    }

    public void updateClubName(int clubIndex, String clubName, ClubType type) {
        ArrayList<ClubHistory> clubsToUpdate = getClubListByType(type);
        if (clubsToUpdate == null) return;

        while (clubsToUpdate.size() <= clubIndex) {
            clubsToUpdate.add(new ClubHistory());
        }
        clubsToUpdate.get(clubIndex).setClubName(clubName);
    }

    public void updateYearJoined(int clubIndex, String yearJoined, ClubType type) {
        if (yearJoined == null || !yearJoined.matches("\\d+")) return;

        ArrayList<ClubHistory> clubListToUpdate = getClubListByType(type);
        if (clubListToUpdate == null) return;

        while (clubListToUpdate.size() <= clubIndex) {
            clubListToUpdate.add(new ClubHistory());
        }

        int year = Integer.parseInt(yearJoined);
        ClubHistory clubToUpdate = clubListToUpdate.get(clubIndex);
        clubToUpdate.setYearJoined(year);
    }

    public void updateYearLeft(int clubIndex, String yearLeft, ClubType type) {
        ArrayList<ClubHistory> clubListToUpdate = getClubListByType(type);
        if (clubListToUpdate == null) return;

        while (clubListToUpdate.size() <= clubIndex) {
            clubListToUpdate.add(new ClubHistory());
        }

        ClubHistory clubToUpdate = clubListToUpdate.get(clubIndex);
        if (yearLeft == null || !yearLeft.matches("\\d+")) {
            clubToUpdate.setYearLeft(clubToUpdate.getYearJoined());
        } else {
            int year = Integer.parseInt(yearLeft);
            clubToUpdate.setYearLeft(year);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name).append('\n');
        if (!youthClubs.isEmpty()) {
            sb.append("\tYouth Clubs:\n");
            for (ClubHistory club : youthClubs) {
                sb.append("\t\t").append(club).append('\n');
            }
        }
        if (!collegeClubs.isEmpty()) {
            sb.append("\tCollege Clubs:\n");
            for (ClubHistory club : collegeClubs) {
                sb.append("\t\t").append(club).append('\n');
            }
        }
        if (!clubs.isEmpty()) {
            sb.append("\tClubs:\n");
            for (ClubHistory club : clubs) {
                sb.append("\t\t").append(club).append('\n');
            }
        }
        if (!nationalTeams.isEmpty()) {
            sb.append("\tNational Teams:\n");
            for (ClubHistory club : nationalTeams) {
                sb.append("\t\t").append(club).append('\n');
            }
        }
        return sb.toString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ClubHistory> getYouthClubs() {
        return youthClubs;
    }

    public void setYouthClubs(ArrayList<ClubHistory> youthClubs) {
        this.youthClubs = youthClubs;
    }

    public ArrayList<ClubHistory> getCollegeClubs() {
        return collegeClubs;
    }

    public void setCollegeClubs(ArrayList<ClubHistory> collegeClubs) {
        this.collegeClubs = collegeClubs;
    }

    public ArrayList<ClubHistory> getClubs() {
        return clubs;
    }

    public void setClubs(ArrayList<ClubHistory> clubs) {
        this.clubs = clubs;
    }

    public ArrayList<ClubHistory> getNationalTeams() {
        return nationalTeams;
    }

    public void setNationalTeams(ArrayList<ClubHistory> nationalTeams) {
        this.nationalTeams = nationalTeams;
    }
}
