package gov.mil.otc._3dvis.data.oadms;

public class CodeName {

    public static String decode(String codeName) {
        if (codeName.toLowerCase().contains("zaptrap")) {
            return "iMCAD";
        } else if (codeName.toLowerCase().contains("boomerang")) {
            return "cSDS";
        } else if (codeName.toLowerCase().contains("tar")) {
            return "B330";
        }
        return codeName;
    }

    private CodeName() {
    }
}
