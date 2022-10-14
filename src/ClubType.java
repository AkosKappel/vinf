public enum ClubType {
    YOUTH,
    COLLEGE,
    PROFESSIONAL,
    NATIONAL;

    public static ClubType getClubType(String text) {
        text = text.toLowerCase();
        if (text.contains("youth")) return ClubType.YOUTH;
        else if (text.contains("college")) return ClubType.COLLEGE;
        else if (text.contains("national")) return ClubType.NATIONAL;
        else return ClubType.PROFESSIONAL;
    }

}
