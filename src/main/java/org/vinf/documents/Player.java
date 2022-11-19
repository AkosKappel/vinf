package org.vinf.documents;

import org.vinf.utils.Regex;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.regex.Matcher;

public class Player extends Page {

    private final ArrayList<ClubHistory> youthClubs;
    private final ArrayList<ClubHistory> collegeClubs;
    private final ArrayList<ClubHistory> professionalClubs;
    private final ArrayList<ClubHistory> nationalTeams;

    public Player(String title) {
        super(title);

        // initialize club lists
        this.youthClubs = new ArrayList<>();
        this.collegeClubs = new ArrayList<>();
        this.professionalClubs = new ArrayList<>();
        this.nationalTeams = new ArrayList<>();
    }

    public Player(String title, String text) {
        this(title);

        // parse wiki text
        try (BufferedReader reader = new BufferedReader(new StringReader(text))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // find start of infobox
                Matcher infoboxMatcher = Regex.infoboxFootballBiographyPattern.matcher(line);
                if (infoboxMatcher.find()) {
                    // get club names and years, where the player played
                    Matcher stackMatcher = Regex.bracketsStartPattern.matcher(line);
                    long stack = 0;
                    while (stackMatcher.find()) stack++;
                    while (stack > 0) {
                        // get club names and years
                        findClubYears(line);
                        findClubNames(line);

                        // read next line of infobox
                        line = reader.readLine();
                        if (line == null) break;

                        // count opening brackets
                        stackMatcher = Regex.bracketsStartPattern.matcher(line);
                        while (stackMatcher.find()) stack++;

                        // count closing brackets
                        stackMatcher = Regex.bracketsEndPattern.matcher(line);
                        while (stackMatcher.find()) stack--;
                    }
                    break; // end of infobox
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        switch (type) {
            case YOUTH:
                return youthClubs;
            case COLLEGE:
                return collegeClubs;
            case PROFESSIONAL:
                return professionalClubs;
            case NATIONAL:
                return nationalTeams;
            default:
                return null;
        }
    }

    public boolean hasClubHistory() {
        return !youthClubs.isEmpty() || !collegeClubs.isEmpty() || !professionalClubs.isEmpty() || !nationalTeams.isEmpty();
    }

    private void findClubYears(String line) {
        Matcher yearsMatcher = Regex.clubYearsPattern.matcher(line);

        if (yearsMatcher.find()) {
            ClubType clubType = ClubType.getClubType(yearsMatcher.group(1));
            int index = Integer.parseInt(yearsMatcher.group(2)) - 1;
            String yearJoined = yearsMatcher.group(3);
            String yearsLeft = yearsMatcher.group(4);

            updateYearJoined(index, yearJoined, clubType);
            updateYearLeft(index, yearsLeft, clubType);
        }
    }

    private void findClubNames(String line) {
        Matcher clubsMatcher = Regex.clubNamePattern.matcher(line);

        if (clubsMatcher.find()) {
            ClubType clubType = ClubType.getClubType(clubsMatcher.group(1));
            int index = Integer.parseInt(clubsMatcher.group(2)) - 1;
            String clubName = clubsMatcher.group(3);

            updateClubName(index, clubName, clubType);
        }
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

    public ArrayList<ClubHistory> getCollegeClubs() {
        return collegeClubs;
    }

    public ArrayList<ClubHistory> getProfessionalClubs() {
        return professionalClubs;
    }

    public ArrayList<ClubHistory> getNationalTeams() {
        return nationalTeams;
    }
}
