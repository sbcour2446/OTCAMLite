package gov.mil.otc._3dvis.tena;

import TENA.LVC.DamageState;
import TENA.LVC.Engagement.CasualtyAssessment;
import TENA.UnsignedShort;

import java.util.Objects;

/**
 * RtcaState() RTCA State Object Class used to maintain the composite RTCA for an Entity
 */
public class RtcaState {
    // Lasting or Permanent States
    //
    private boolean rtcaKillCatastrophic; // Cat Kill
    private boolean rtcaKillMobility; // Mobility Kill
    private boolean rtcaKillFirepower; // Firepower Kill
    private boolean rtcaKillCommunication; // Communications Kill

    // Temporary or Transitory States
    //
    private boolean rtcaSuppression; // Firepower Suppression on/off (can use suppressUntil Timer if set)
    private boolean rtcaJamming; // EW Effects on/off
    private boolean rtcaHitNoKill; // Was Hit (reset at Resurrect)
    private boolean rtcaMiss; // Was Missed (reset at Resurrect)

    // RTCA State (if Other)
    //
    private String rtcaOther; // Undefined, Unresolved, No PhPk, or Other (set when

    // Modifiers
    //
    private long suppressUntil; // Suppression Timer (if specified)
    private int rtcaDamagePercent; // Damage Percent (0 - 100)

    /**
     * Constructor (Default Initialized State of 'Alive')
     */
    public RtcaState() {
        setRtcaAlive(); // Initialize to Resurrected State (Alive)
    }

    /**
     * Constructor using the OTCAM RTCA State Object
     *
     * @param rtcaState OTCAM RTCA State Object
     */
    public RtcaState(OTC.OTCAM.RtcaState.ImmutableLocalClass rtcaState) {
        this.rtcaKillCatastrophic = (rtcaState != null) && rtcaState.get_rtcaKillCatastrophic();
        this.rtcaKillMobility = (rtcaState != null) && rtcaState.get_rtcaKillMobility();
        this.rtcaKillFirepower = (rtcaState != null) && rtcaState.get_rtcaKillFirepower();
        this.rtcaKillCommunication = (rtcaState != null) && rtcaState.get_rtcaKillCommunication();
        this.rtcaSuppression = (rtcaState != null) && rtcaState.get_rtcaSuppression();
        this.rtcaJamming = (rtcaState != null) && rtcaState.get_rtcaJamming();
        this.rtcaHitNoKill = (rtcaState != null) && rtcaState.get_rtcaHitNoKill();
        this.rtcaMiss = (rtcaState != null) && rtcaState.get_rtcaHitNoKill();
        this.rtcaOther = (rtcaState != null) && rtcaState.is_rtcaOther_set() ? rtcaState.get_rtcaOther() : null;
        this.rtcaDamagePercent = (rtcaState != null) && rtcaState.is_damagePercent_set() ? rtcaState.get_damagePercent().intValue() : 0;
        this.suppressUntil = 0;
    }

    /**
     * Constructor using the TENA Casualty Assessment Object
     *
     * @param casualtyAssessment TENA Casualty Assessment Object
     */
    public RtcaState(CasualtyAssessment casualtyAssessment) {
        setRtcaResurrect(); // Start Resurrected

        switch (casualtyAssessment) {
            case CasualtyAssessment_KillCatastrophic:
                rtcaDamagePercent = 100; // Cat Kill is always 100% damage
                rtcaKillCommunication = true;
                rtcaKillMobility = true;
                rtcaKillFirepower = true;
                rtcaKillCatastrophic = true;
                break;
            case CasualtyAssessment_KillCommunication:
                rtcaKillCommunication = true;
                break;
            case CasualtyAssessment_HitNoKill, CasualtyAssessment_Wounded:
                rtcaHitNoKill = true;
                break;
            case CasualtyAssessment_Suppression:
                rtcaSuppression = true;
                break;
            case CasualtyAssessment_KillMobility, CasualtyAssessment_DelayedMobilityKill:
                rtcaKillMobility = true;
                break;
            case CasualtyAssessment_KillFirepower:
                rtcaKillFirepower = true;
                break;
            case CasualtyAssessment_KillCommAndMobility:
                rtcaKillCommunication = true;
                rtcaKillMobility = true;
                break;
            case CasualtyAssessment_KillCommAndFirepower:
                rtcaKillCommunication = true;
                rtcaKillFirepower = true;
                break;
            case CasualtyAssessment_KillFirepowerAndMobility:
                rtcaKillFirepower = true;
                rtcaKillMobility = true;
                break;
            case CasualtyAssessment_Miss:
                rtcaMiss = true;
                break;
            case CasualtyAssessment_Alive:
                rtcaDamagePercent = 0;
                break;
            default: // Other, NoPHPK, Unresolved, StateUnknown, DamagedUnspecified:
                break; // Perform No action
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RtcaState rtcaState = (RtcaState) o;
        return rtcaKillCatastrophic == rtcaState.rtcaKillCatastrophic
                && rtcaKillMobility == rtcaState.rtcaKillMobility
                && rtcaKillFirepower == rtcaState.rtcaKillFirepower
                && rtcaKillCommunication == rtcaState.rtcaKillCommunication
                && rtcaSuppression == rtcaState.rtcaSuppression
                && rtcaJamming == rtcaState.rtcaJamming
                && rtcaHitNoKill == rtcaState.rtcaHitNoKill
                && rtcaMiss == rtcaState.rtcaMiss
                && suppressUntil == rtcaState.suppressUntil
                && rtcaDamagePercent == rtcaState.rtcaDamagePercent
                && Objects.equals(rtcaOther, rtcaState.rtcaOther);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return Objects.hash(
                rtcaKillCatastrophic,
                rtcaKillMobility,
                rtcaKillFirepower,
                rtcaKillCommunication,
                rtcaSuppression,
                rtcaJamming,
                rtcaHitNoKill,
                rtcaMiss,
                rtcaOther,
                suppressUntil,
                rtcaDamagePercent);
    }

    /**
     * Initialize and return a RtcaState Object initialized from a TENA Casualty Assessment
     *
     * @param casualtyAssessment TENA CasualtyAssessment Object
     * @return this RtcaState Object
     */
    public RtcaState set(CasualtyAssessment casualtyAssessment) {
        return new RtcaState(casualtyAssessment);
    }

    /**
     * Initialize and return an OTCAM RTCA State Object
     *
     * @param rtcaState OTCAM RTCA State Object
     * @return this RtcaState Object
     */
    public RtcaState set(OTC.OTCAM.RtcaState.ImmutableLocalClass rtcaState) {
        return new RtcaState(rtcaState);
    }

    /**
     * Get the RTCA Assessment from the RTCA State
     *
     * @return RtcaAssessment Object with current Assessment
     */
    public OTC.OTCAM.RtcaAssessment.LocalClass getAssessment () {
        return OTC.OTCAM.RtcaAssessment.LocalClass.create(
                isRtcaKillCatastrophic(),
                isRtcaKillMobility(),
                isRtcaKillFirepower(),
                isRtcaKillCommunication());
    }

    /**
     * Constructor using the TENA Casualty Assessment Object
     * /**
     * Convert the RtcaState Object to a TENA Casualty Assessment Enumeration
     *
     * @return CasualtyAssessment Enumeration
     */
    public CasualtyAssessment getCasualtyAssessment() {
        if (isRtcaKillCatastrophic())
            return CasualtyAssessment.CasualtyAssessment_KillCatastrophic;
        else if (isRtcaKillMobility()
                && isRtcaKillCommunication())
            return CasualtyAssessment.CasualtyAssessment_KillCommAndMobility;
        else if (isRtcaKillFirepower()
                && isRtcaKillCommunication())
            return CasualtyAssessment.CasualtyAssessment_KillCommAndFirepower;
        else if (isRtcaKillFirepower()
                && isRtcaKillMobility())
            return CasualtyAssessment.CasualtyAssessment_KillFirepowerAndMobility;
        else if (isRtcaKillMobility())
            return CasualtyAssessment.CasualtyAssessment_KillMobility;
        else if (isRtcaKillFirepower())
            return CasualtyAssessment.CasualtyAssessment_KillFirepower;
        else if (isRtcaKillCommunication())
            return CasualtyAssessment.CasualtyAssessment_KillCommunication;
        else if (isRtcaHitNoKill())
            return CasualtyAssessment.CasualtyAssessment_HitNoKill;
        else if (isRtcaSuppression())
            return CasualtyAssessment.CasualtyAssessment_Suppression;
        else if (isRtcaMiss())
            return CasualtyAssessment.CasualtyAssessment_Miss;
        else
            return CasualtyAssessment.CasualtyAssessment_Alive;
    }

    /**
     * Construct the DIS Appearance-record Damage State from the Rtca State
     *
     * @return DIS Appearance-record Damage State
     */
    public DamageState getDamageState() {
        if (isRtcaKillCatastrophic())
            return DamageState.DamageState_Destroyed;
        else if (isRtcaKillFirepower() && isRtcaKillMobility())
            return DamageState.DamageState_ModerateDamage;
        else if (isRtcaKillCommunication() || isRtcaHitNoKill())
            return DamageState.DamageState_SlightDamage;
        else
            return DamageState.DamageState_NoDamage;
    }

    /**
     * Update the damage % based on new damage <= 100%
     *
     * @param damagePercent The new damage %.
     */
    public void updateDamagePercent(int damagePercent) {
        this.rtcaDamagePercent = Math.min((this.rtcaDamagePercent + damagePercent), 100); // todo: Damage% must be added to the Engagement/Adjudication Message
    }

    /**
     * Reset the damage % to Zero
     */
    public void resetDamage() {
        this.rtcaDamagePercent = 0;
    }

    /**
     * Catastrophic Kill is the combination of Mobility, Firepower and Communications or 100% Damage
     *
     * @return true if Catastrophic Killed, otherwise false
     */
    public boolean isRtcaKillCatastrophic() {
        return (rtcaKillCatastrophic
                || (rtcaDamagePercent >= 100)
                || (rtcaKillMobility
                && rtcaKillFirepower
                && rtcaKillCommunication));
    }

    /**
     * Total kill is the combination of Mobility & Firepower Kill
     *
     * @return true if Total Killed, otherwise false
     */
    public boolean isRtcaKillCTotal() {
        return (rtcaKillMobility && rtcaKillFirepower);
    }

    /**
     * Wounded is any combination of Mobility, Firepower and Communications or any Damage
     *
     * @return true if Wounded, otherwise false
     */
    public boolean isRtcaWounded() {
        return ((rtcaDamagePercent >= 0)
                || (rtcaKillMobility
                || rtcaKillFirepower
                || rtcaKillCommunication));
    }

    /**
     * Incapacitated is the combination of Mobility, Firepower and Communications or 100% Damage
     *
     * @return true if Incapacitated, otherwise false
     */
    public boolean isRtcaIncapacitated() {
        return (rtcaKillCatastrophic
                || (rtcaDamagePercent >= 100)
                || (rtcaKillMobility
                && rtcaKillFirepower && rtcaKillCommunication));
    }

    /**
     * Alive is the absence of Mobility, Firepower and Communications and any Damage
     *
     * @return true if Incapacitated, otherwise false
     */
    public boolean isRtcaAlive() {
        return ((rtcaDamagePercent == 0)
                && (!rtcaKillCatastrophic
                && !rtcaKillMobility
                && !rtcaKillFirepower
                && !rtcaKillCommunication));
    }

    /**
     * Set Catastrophic Kill by setting Mobility, Firepower and Communications Kill and Damage to 100%
     */
    public void setRtcaKillCatastrophic() {
        this.rtcaDamagePercent = 100;
        this.rtcaKillMobility = true;
        this.rtcaKillFirepower = true;
        this.rtcaKillCommunication = true;
        this.rtcaKillCatastrophic = true;
    }

    /**
     * Set Total Kill by setting Mobility & Firepower Kill
     */
    public void setRtcaKillTotal() {
        this.rtcaKillMobility = true;
        this.rtcaKillFirepower = true;
    }

    /**
     * Resurrect by unsetting setting Mobility, Firepower and Communications Kill and Damage
     */
    public void setRtcaAlive() {
        setRtcaResurrect();
    }

    /**
     * Resurrect by unsetting setting Mobility, Firepower and Communications Kill and Damage
     */
    public void setRtcaResurrect() {
        this.rtcaKillCatastrophic = false;
        this.rtcaKillMobility = false;
        this.rtcaKillFirepower = false;
        this.rtcaKillCommunication = false;
        this.rtcaSuppression = false;
        this.rtcaJamming = false;
        this.rtcaHitNoKill = false;
        this.rtcaMiss = false;
        this.rtcaOther = null;
        this.rtcaDamagePercent = 0;
    }

    /**
     * Return in the form of an OTCAM RTCA State Object
     *
     * @return OTCAM RTCA State Object
     */
    public OTC.OTCAM.RtcaState.LocalClass get() {
        OTC.OTCAM.RtcaState.LocalClass rtcaState = OTC.OTCAM.RtcaState.LocalClass.create(
                rtcaKillCatastrophic,
                rtcaKillMobility,
                rtcaKillFirepower,
                rtcaKillCommunication,
                rtcaSuppression,
                rtcaJamming,
                rtcaHitNoKill,
                rtcaMiss);

        if (rtcaOther != null) rtcaState.set_rtcaOther(rtcaOther);
        if (rtcaDamagePercent > 0) rtcaState.set_damagePercent(UnsignedShort.valueOf(rtcaDamagePercent));

        return rtcaState;
    }

    public boolean isRtcaKillMobility() {
        return rtcaKillMobility;
    }

    public void setRtcaKillMobility(boolean rtcaKillMobility) {
        this.rtcaKillMobility = rtcaKillMobility;
    }

    public void setRtcaKillMobility() {
        this.rtcaKillMobility = true;
    }

    public boolean isRtcaKillFirepower() {
        return rtcaKillFirepower;
    }

    public void setRtcaKillFirepower(boolean rtcaKillFirepower) {
        this.rtcaKillFirepower = rtcaKillFirepower;
    }

    public void setRtcaKillFirepower() {
        this.rtcaKillFirepower = true;
    }

    public boolean isRtcaKillCommunication() {
        return rtcaKillCommunication;
    }

    public void setRtcaKillCommunication(boolean rtcaKillCommunication) {
        this.rtcaKillCommunication = rtcaKillCommunication;
    }

    public void setRtcaKillCommunication() {
        this.rtcaKillCommunication = true;
    }

    public boolean isRtcaSuppression() {
        return rtcaSuppression;
    }

    public void setRtcaSuppression(boolean rtcaSuppression) {
        this.rtcaSuppression = rtcaSuppression;
    }

    public void setRtcaSuppression() {
        this.rtcaSuppression = true;
    }

    public boolean isRtcaJamming() {
        return rtcaJamming;
    }

    public void setRtcaJamming(boolean rtcaJamming) {
        this.rtcaJamming = rtcaJamming;
    }

    public void setRtcaJamming() {
        this.rtcaJamming = true;
    }

    public boolean isRtcaHitNoKill() {
        return rtcaHitNoKill;
    }

    public void setRtcaHitNoKill(boolean rtcaHitNoKill) {
        this.rtcaHitNoKill = rtcaHitNoKill;
    }

    public void setRtcaHitNoKill() {
        this.rtcaHitNoKill = true;
    }

    public boolean isRtcaMiss() {
        return rtcaMiss;
    }

    public void setRtcaMiss(boolean rtcaMiss) {
        this.rtcaMiss = rtcaMiss;
    }

    public void setRtcaMiss() {
        this.rtcaMiss = true;
    }

    public boolean isRtcaOtherSet() {
        return ((rtcaOther == null) || rtcaOther.isEmpty());
    }

    public String getRtcaOther() {
        return rtcaOther;
    }

    public void setRtcaOther(String rtcaOther) {
        this.rtcaOther = rtcaOther;
    }

    public void clearRtcaOther() {
        this.rtcaOther = null;
    }

    public long getSuppressUntil() {
        return suppressUntil;
    }

    public boolean isSuppressUntilSet() {
        return (suppressUntil != 0);
    }

    public void setSuppressUntil(long suppressUntil) {
        this.suppressUntil = suppressUntil;
    }

    public void clearSuppressUntil() {
        this.suppressUntil = 0;
    }

    public boolean isDamageSet() {
        return (rtcaDamagePercent != 0);
    }

    public void setRtcaDamagePercent(int rtcaDamagePercent) {
        this.rtcaDamagePercent = rtcaDamagePercent;
    }

    public void clearDamage() {
        this.rtcaDamagePercent = 0;
    }

    public int getRtcaDamagePercent() {
        return (rtcaDamagePercent);
    }
}
