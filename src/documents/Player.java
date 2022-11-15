package documents;

import utils.Regex;

import java.util.ArrayList;
import java.util.regex.Matcher;

public class Player extends Page {

    private ArrayList<ClubHistory> youthClubs;
    private ArrayList<ClubHistory> collegeClubs;
    private ArrayList<ClubHistory> professionalClubs;
    private ArrayList<ClubHistory> nationalTeams;

    public Player(String title) {
        super(title);

        Matcher nameMatcher = Regex.textPattern.matcher(title);
        this.name = nameMatcher.find() ? nameMatcher.group(1).trim() : title;

        this.youthClubs = new ArrayList<>();
        this.collegeClubs = new ArrayList<>();
        this.professionalClubs = new ArrayList<>();
        this.nationalTeams = new ArrayList<>();
    }

    @Override
    public boolean isValid() {
        return !name.isEmpty() && hasClubHistory();
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

    public ArrayList<ClubHistory> getPlayedAtWith(Player player) {
        ArrayList<ClubHistory> playedAt = getPlayedAtWith(player, ClubType.PROFESSIONAL);
        playedAt.addAll(getPlayedAtWith(player, ClubType.NATIONAL));
        playedAt.addAll(getPlayedAtWith(player, ClubType.COLLEGE));
        playedAt.addAll(getPlayedAtWith(player, ClubType.YOUTH));
        return playedAt;
    }

    public ArrayList<ClubHistory> getPlayedAtWith(Player player, ClubType type) {
        ArrayList<ClubHistory> clubs = getClubsByType(type);
        ArrayList<ClubHistory> otherClubs = player.getClubsByType(type);
        ArrayList<ClubHistory> playedAt = new ArrayList<>();

        for (ClubHistory club : clubs) {
            for (ClubHistory otherClub : otherClubs) {
                if (club.getClubName().equals(otherClub.getClubName()) && yearsOverlap(club, otherClub)) {
                    playedAt.add(new ClubHistory(
                            club.getClubName(),
                            Math.max(club.getYearStart(), otherClub.getYearStart()),
                            Math.min(club.getYearEnd(), otherClub.getYearEnd()))
                    );
                }
            }
        }

        return playedAt;
    }

    public static boolean yearsOverlap(ClubHistory club, ClubHistory otherClub) {
        if (club.getYearStart() == 0 || otherClub.getYearStart() == 0 ||
                club.getYearEnd() == 0 || otherClub.getYearEnd() == 0) return false;
        return club.getYearStart() <= otherClub.getYearEnd() && club.getYearEnd() >= otherClub.getYearStart();
    }

    public ArrayList<ClubHistory> getClubsByType(ClubType type) {
        return switch (type) {
            case YOUTH -> youthClubs;
            case COLLEGE -> collegeClubs;
            case PROFESSIONAL -> professionalClubs;
            case NATIONAL -> nationalTeams;
        };
    }

    public boolean hasClubHistory() {
        return !youthClubs.isEmpty() || !collegeClubs.isEmpty() || !professionalClubs.isEmpty() || !nationalTeams.isEmpty();
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
        clubToUpdate.setYearStart(year);
    }

    public void updateYearLeft(int clubIndex, String yearLeft, ClubType type) {
        ArrayList<ClubHistory> clubListToUpdate = getClubsByType(type);
        if (clubListToUpdate == null) return;

        while (clubListToUpdate.size() <= clubIndex) {
            clubListToUpdate.add(new ClubHistory());
        }

        ClubHistory clubToUpdate = clubListToUpdate.get(clubIndex);
        if (yearLeft == null || !yearLeft.matches(Regex.digits)) {
            clubToUpdate.setYearEnd(clubToUpdate.getYearStart());
        } else {
            int year = Integer.parseInt(yearLeft);
            clubToUpdate.setYearEnd(year);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getName()).append('\n');
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
        if (!professionalClubs.isEmpty()) {
            sb.append("\tProfessional clubs:\n");
            for (ClubHistory club : professionalClubs) {
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

    public ArrayList<ClubHistory> getProfessionalClubs() {
        return professionalClubs;
    }

    public void setProfessionalClubs(ArrayList<ClubHistory> professionalClubs) {
        this.professionalClubs = professionalClubs;
    }

    public ArrayList<ClubHistory> getNationalTeams() {
        return nationalTeams;
    }

    public void setNationalTeams(ArrayList<ClubHistory> nationalTeams) {
        this.nationalTeams = nationalTeams;
    }
}
