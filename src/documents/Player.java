package documents;

import utils.Regex;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class Player extends Page {

    private String name;
    private ArrayList<ClubHistory> youthClubs;
    private ArrayList<ClubHistory> collegeClubs;
    private ArrayList<ClubHistory> clubs;
    private ArrayList<ClubHistory> nationalTeams;

    public Player(String title) {
        super(title);
        Matcher nameMatcher = Regex.playerNamePattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;
        this.youthClubs = new ArrayList<>();
        this.collegeClubs = new ArrayList<>();
        this.clubs = new ArrayList<>();
        this.nationalTeams = new ArrayList<>();
    }

    public boolean hasPlayedWith(Player player) {
        return hasPlayedWith(player, ClubType.PROFESSIONAL) ||
                hasPlayedWith(player, ClubType.NATIONAL) ||
                hasPlayedWith(player, ClubType.COLLEGE) ||
                hasPlayedWith(player, ClubType.YOUTH);
    }

    public boolean hasPlayedWith(Player player, ClubType type) {
        ArrayList<ClubHistory> clubs = getClubsByType(type);
        ArrayList<ClubHistory> otherClubs = player.getClubsByType(type);

        for (ClubHistory club : clubs) {
            for (ClubHistory otherClub : otherClubs) {
                if (club.getClubName().equals(otherClub.getClubName()) && yearsOverlap(club, otherClub)) {
                    return true;
                }
            }
        }

        return false;
    }

    // TODO: should be a list of clubs, where players played together ?
    public String getPlayedAtWith(Player player) {
        String playedAt = getPlayedAtWith(player, ClubType.PROFESSIONAL);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtWith(player, ClubType.NATIONAL);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtWith(player, ClubType.COLLEGE);
        if (playedAt != null) return playedAt;
        playedAt = getPlayedAtWith(player, ClubType.YOUTH);
        if (playedAt != null) return playedAt;
        return "";
    }

    public String getPlayedAtWith(Player player, ClubType type) {
        ArrayList<ClubHistory> clubs = getClubsByType(type);
        ArrayList<ClubHistory> otherClubs = player.getClubsByType(type);

        for (ClubHistory club : clubs) {
            for (ClubHistory otherClub : otherClubs) {
                if (club.getClubName().equals(otherClub.getClubName()) && yearsOverlap(club, otherClub)) {
                    return club.getClubName();
                }
            }
        }
        return "";
    }

    public boolean yearsOverlap(ClubHistory club, ClubHistory otherClub) {
        if (club.getYearJoined() == 0 || otherClub.getYearJoined() == 0 ||
                club.getYearLeft() == 0 || otherClub.getYearLeft() == 0) return false;
        return club.getYearJoined() <= otherClub.getYearLeft() && club.getYearLeft() >= otherClub.getYearJoined();
    }

    private ArrayList<ClubHistory> getClubsByType(ClubType type) {
        return switch (type) {
            case YOUTH -> youthClubs;
            case COLLEGE -> collegeClubs;
            case PROFESSIONAL -> clubs;
            case NATIONAL -> nationalTeams;
        };
    }

    public boolean hasClubHistory() {
        return !youthClubs.isEmpty() || !collegeClubs.isEmpty() || !clubs.isEmpty() || !nationalTeams.isEmpty();
    }

    public void updateClubName(int clubIndex, String clubName, ClubType type) {
        ArrayList<ClubHistory> clubsToUpdate = getClubsByType(type);
        if (clubsToUpdate == null) return;

        while (clubsToUpdate.size() <= clubIndex) {
            clubsToUpdate.add(new ClubHistory());
        }
        clubsToUpdate.get(clubIndex).setClubName(clubName);
    }

    public void updateYearJoined(int clubIndex, String yearJoined, ClubType type) {
        if (yearJoined == null || !yearJoined.matches(Regex.digits)) return;

        ArrayList<ClubHistory> clubListToUpdate = getClubsByType(type);
        if (clubListToUpdate == null) return;

        while (clubListToUpdate.size() <= clubIndex) {
            clubListToUpdate.add(new ClubHistory());
        }

        int year = Integer.parseInt(yearJoined);
        ClubHistory clubToUpdate = clubListToUpdate.get(clubIndex);
        clubToUpdate.setYearJoined(year);
    }

    public void updateYearLeft(int clubIndex, String yearLeft, ClubType type) {
        ArrayList<ClubHistory> clubListToUpdate = getClubsByType(type);
        if (clubListToUpdate == null) return;

        while (clubListToUpdate.size() <= clubIndex) {
            clubListToUpdate.add(new ClubHistory());
        }

        ClubHistory clubToUpdate = clubListToUpdate.get(clubIndex);
        if (yearLeft == null || !yearLeft.matches(Regex.digits)) {
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
