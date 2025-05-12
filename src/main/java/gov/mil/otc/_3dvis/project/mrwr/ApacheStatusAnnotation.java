package gov.mil.otc._3dvis.project.mrwr;

import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.entity.render.StatusAnnotation;

public class ApacheStatusAnnotation extends StatusAnnotation {

    public ApacheStatusAnnotation(IEntity entity) {
        super(entity);
    }

    @Override
    protected String getStatusAnnotation() {
        StringBuilder stringBuilder = new StringBuilder(super.getStatusAnnotation());

        if (!(getEntity() instanceof ApacheEntity apacheEntity)) {
            return stringBuilder.toString();
        }

        WeaponSystemResponse weaponSystemResponse = apacheEntity.getCurrentWeaponSystemResponse();
        if (weaponSystemResponse != null) {
            for (int i = 1; i < WeaponSystemResponse.NUMBER_OF_MESSAGES; i++) {
                WeaponSystemResponse.WeaponSystemResponseMessage weaponSystemResponseMessage =
                        weaponSystemResponse.getWeaponSystemResponseMessageMap().get(i);
                if (weaponSystemResponseMessage != null) {
                    String azString = String.format("%3.2f", weaponSystemResponseMessage.azimuth());
                    String value = String.format("Az:%6s°, ID:%s", azString, weaponSystemResponseMessage.getWeaponSystemName());
                    addRow(stringBuilder, "\u00A0\u00A0\u00A0WPN MSG" + i, value);
                }
            }
        }

        return stringBuilder.toString();
    }
}
