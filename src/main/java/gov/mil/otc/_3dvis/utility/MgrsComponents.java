package gov.mil.otc._3dvis.utility;

public class MgrsComponents {

    public static final char[] ZONE_ROW_DESIGNATORS = new char[]{'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X'};
    public static final char[] SQUARE_ROW_COLUMN_DESIGNATORS = new char[]{'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X'};

    public static MgrsComponents getMgrsComponents(String mgrsString) {
        mgrsString = removeSpaces(mgrsString);

        int[] letters = new int[3];
        long easting;
        long northing;
        int precision;
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        int index = 0;

        int numDigits = getNumDigits(mgrsString, index);
        if (numDigits < 1 || numDigits > 2) {
            return null;
        }

        int zone = Integer.parseInt(mgrsString.substring(0, numDigits));
        if (zone < 1 || zone > 60) {
            return null;
        }

        index = numDigits;
        int numLetters = 0;
        for (int i = index; i < mgrsString.length(); ++i) {
            if (!Character.isLetter(mgrsString.charAt(i))) {
                numLetters = i - index;
                break;
            }
        }

        if (numLetters != 3) {
            return null;
        }

        letters[0] = alphabet.indexOf(Character.toUpperCase(mgrsString.charAt(index++)));
        if (letters[0] == 8 || letters[0] == 14) {
            return null;
        }

        letters[1] = alphabet.indexOf(Character.toUpperCase(mgrsString.charAt(index++)));
        if (letters[1] == 8 || letters[1] == 14) {
            return null;
        }

        letters[2] = alphabet.indexOf(Character.toUpperCase(mgrsString.charAt(index++)));
        if (letters[2] == 8 || letters[2] == 14) {
            return null;
        }

        numDigits = getNumDigits(mgrsString, index);
        if (numDigits <= 10 && numDigits % 2 == 0) {
            int n = numDigits / 2;
            precision = n;
            if (n > 0) {
                easting = Integer.parseInt(mgrsString.substring(index, index + n));
                northing = Integer.parseInt(mgrsString.substring(index + n, index + n + n));
                double multiplier = Math.pow(10.0, 5.0 - n);
                easting = (long) (easting * multiplier);
                northing = (long) (northing * multiplier);
            } else {
                easting = 0L;
                northing = 0L;
            }
        } else {
            return null;
        }

        return new MgrsComponents(zone, letters[0], letters[1], letters[2], (int) easting, (int) northing, precision);
    }

    private static String removeSpaces(String mgrsString) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < mgrsString.length(); i++) {
            char c = mgrsString.charAt(i);
            if (c != ' ') {
                stringBuilder.append(c);
            }
        }
        return stringBuilder.toString();
    }

    private static int getNumDigits(String mgrsString, int start) {
        int i = start;
        for (; i < mgrsString.length(); ++i) {
            if (!Character.isDigit(mgrsString.charAt(i))) {
                break;
            }
        }
        return i - start;
    }

    private final int zone;
    private final int latitudeBand;
    private final int squareLetter1;
    private final int squareLetter2;
    private final int easting;
    private final int northing;
    private final int precision;

    public MgrsComponents(int zone, int latitudeBand, int squareLetter1, int squareLetter2,
                          int easting, int northing, int precision) {
        this.zone = zone;
        this.latitudeBand = latitudeBand;
        this.squareLetter1 = squareLetter1;
        this.squareLetter2 = squareLetter2;
        this.easting = easting;
        this.northing = northing;
        this.precision = precision;
    }

    public int getZone() {
        return zone;
    }

    public char getLatitudeBand() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(latitudeBand);
    }

    public char getSquareLetter1() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(squareLetter1);
    }

    public char getSquareLetter2() {
        return "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(squareLetter2);
    }

    public int getEasting() {
        return easting;
    }

    public int getNorthing() {
        return northing;
    }

    public int getPrecision() {
        return precision;
    }
}
