package gov.mil.otc._3dvis.datamodel;

import TENA.LVC.DamageState;

import java.util.Objects;

public class RtcaState {

    private final boolean isKillCatastrophic;
    private final boolean isKillFirepower;
    private final boolean isKillMobility;
    private final boolean isKillCommunication;
    private final boolean isSuppression;
    private final boolean isJammed;
    private final boolean isHitNoKill;
    private final boolean isMiss;
    private final String rtcaOther;
    private final int damagePercent;

    public RtcaState(boolean isKillCatastrophic, boolean isKillFirepower, boolean isKillMobility,
                     boolean isKillCommunication, boolean isSuppression, boolean isJammed,
                     boolean isHitNoKill, boolean isMiss, String rtcaOther, int damagePercent) {
        this.isKillCatastrophic = isKillCatastrophic;
        this.isKillFirepower = isKillFirepower;
        this.isKillMobility = isKillMobility;
        this.isKillCommunication = isKillCommunication;
        this.isSuppression = isSuppression;
        this.isJammed = isJammed;
        this.isHitNoKill = isHitNoKill;
        this.isMiss = isMiss;
        this.rtcaOther = rtcaOther;
        this.damagePercent = damagePercent;
    }

    public RtcaState(RtcaState rtcaState) {
        this.isKillCatastrophic = rtcaState.isKillCatastrophic;
        this.isKillFirepower = rtcaState.isKillFirepower;
        this.isKillMobility = rtcaState.isKillMobility;
        this.isKillCommunication = rtcaState.isKillCommunication;
        this.isSuppression = rtcaState.isSuppression;
        this.isJammed = rtcaState.isJammed;
        this.isHitNoKill = rtcaState.isHitNoKill;
        this.isMiss = rtcaState.isMiss;
        this.rtcaOther = rtcaState.rtcaOther;
        this.damagePercent = rtcaState.damagePercent;
    }

    public static RtcaState createAlive() {
        return new RtcaState(false, false, false, false,
                false, false, false, false, "", 0);
    }

    public static RtcaState create(TENA.LVC.Entity.PublicationState publicationState) {
        boolean isKillCatastrophic = publicationState.get_damageState() == DamageState.DamageState_Destroyed;
        boolean isKillFirepower = false;
        boolean isKillMobility = false;
        boolean isKillCommunication = false;
        boolean isSuppression = false;
        boolean isJammed = false;
        boolean isHitNoKill = false;
        boolean isMiss = false;
        String rtcaOther = publicationState.get_damageState().toString();
        int damagePercent = 0;

        return new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                isKillCommunication, isSuppression, isJammed,
                isHitNoKill, isMiss, rtcaOther, damagePercent);
    }

    public static RtcaState create(OTC.OTCAM.RtcaState.ImmutableLocalClass otcamRtcaState) {
        boolean isKillCatastrophic = otcamRtcaState.get_rtcaKillCatastrophic();
        boolean isKillFirepower = otcamRtcaState.get_rtcaKillFirepower();
        boolean isKillMobility = otcamRtcaState.get_rtcaKillMobility();
        boolean isKillCommunication = otcamRtcaState.get_rtcaKillCommunication();
        boolean isSuppression = otcamRtcaState.get_rtcaSuppression();
        boolean isJammed = otcamRtcaState.get_rtcaJamming();
        boolean isHitNoKill = otcamRtcaState.get_rtcaHitNoKill();
        boolean isMiss = otcamRtcaState.get_rtcaMiss();
        String rtcaOther = otcamRtcaState.is_rtcaOther_set() ? otcamRtcaState.get_rtcaOther() : "";
        int damagePercent = otcamRtcaState.is_damagePercent_set() ? otcamRtcaState.get_damagePercent().intValue() : 0;

        return new RtcaState(isKillCatastrophic, isKillFirepower, isKillMobility,
                isKillCommunication, isSuppression, isJammed,
                isHitNoKill, isMiss, rtcaOther, damagePercent);
    }

    public boolean isKillCatastrophic() {
        return isKillCatastrophic;
    }

    public boolean isKillFirepower() {
        return isKillFirepower;
    }

    public boolean isKillMobility() {
        return isKillMobility;
    }

    public boolean isKillCommunication() {
        return isKillCommunication;
    }

    public boolean isSuppression() {
        return isSuppression;
    }

    public boolean isJammed() {
        return isJammed;
    }

    public boolean isHitNoKill() {
        return isHitNoKill;
    }

    public boolean isMiss() {
        return isMiss;
    }

    public String getRtcaOther() {
        return rtcaOther;
    }

    public int getDamagePercent() {
        return damagePercent;
    }

    public String getAbbreviatedString() {
        String value = "";
        if (isKillCatastrophic) {
            value += "K";
        }
        if (isKillFirepower) {
            value += "F";
        }
        if (isKillMobility) {
            value += "M";
        }
        if (isKillCommunication) {
            value += "C";
        }
        if (value.isEmpty()) {
            value = "A";
        }
        if (isSuppression) {
            value += "s";
        }
        if (isJammed) {
            value += "j";
        }
        if (isHitNoKill) {
            value += "h";
        }
        if (isMiss) {
            value += "m";
        }
        return value;
    }

    public String getAliveKillString() {
        String results = getKillString();
        if (results.isEmpty()) {
            results = "Alive";
        }
        return results;
    }

    private String getKillString() {
        String results = "";
        if (isKillCatastrophic) {
            results += "Catastrophic Kill";
        } else {
            String prefix = "";
            String suffix = "";
            if (isKillFirepower) {
                results += prefix + "Firepower";
                prefix = " & ";
                suffix = " Kill";
            }
            if (isKillMobility) {
                results += prefix + "Mobility";
                prefix = " & ";
                suffix = " Kill";
            }
            if (isKillCommunication) {
                results += prefix + "Communications";
                suffix = " Kill";
            }
            results += suffix;
        }
        return results;
    }

    public String getTransitoryStateString() {
        String results = "";
        String prefix = "";
        if (isSuppression) {
            results += prefix + "Suppressed";
            prefix = " & ";
        }
        if (isHitNoKill) {
            results += prefix + "Hit (no Kill)";
            prefix = " & ";
        }
        if (isMiss) {
            results += prefix + "Missed";
            prefix = " & ";
        }
        if (isJammed) {
            results += prefix + "Radio Jammed";
        }
        return results;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        RtcaState rtcaState = (RtcaState) o;
        return isKillCatastrophic == rtcaState.isKillCatastrophic
                && isKillFirepower == rtcaState.isKillFirepower
                && isKillMobility == rtcaState.isKillMobility
                && isKillCommunication == rtcaState.isKillCommunication
                && isSuppression == rtcaState.isSuppression
                && isJammed == rtcaState.isJammed
                && isHitNoKill == rtcaState.isHitNoKill
                && isMiss == rtcaState.isMiss
                && Objects.equals(rtcaOther, rtcaState.rtcaOther)
                && damagePercent == rtcaState.damagePercent;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isKillCatastrophic, isKillFirepower, isKillMobility, isKillCommunication,
                isSuppression, isJammed, isHitNoKill, isMiss, rtcaOther, damagePercent);
    }

    @Override
    public String toString() {
        String results = getAliveKillString();

        if (!isKillCatastrophic) {
            String transitoryStateString = getTransitoryStateString();
            if (!transitoryStateString.isEmpty()) {
                results += String.format(" (%s)", transitoryStateString);
            }
            if (damagePercent > 0) {
                results += String.format(" %d%% Damage", damagePercent);
            }
        }

        if (!rtcaOther.isEmpty()) {
            results += " " + rtcaOther;
        }

        return results;
    }
}
