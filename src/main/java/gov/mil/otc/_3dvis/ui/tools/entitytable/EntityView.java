package gov.mil.otc._3dvis.ui.tools.entitytable;

import gov.mil.otc._3dvis.Common;
import gov.mil.otc._3dvis.datamodel.Affiliation;
import gov.mil.otc._3dvis.datamodel.EntityDetail;
import gov.mil.otc._3dvis.datamodel.EntityType;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.utility.ImageLoader;
import gov.nasa.worldwind.geom.Position;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.ImageView;

import java.text.SimpleDateFormat;
import java.util.*;

public class EntityView {

    private final EntityId entityId;
    private final StringProperty source = new SimpleStringProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty description = new SimpleStringProperty();
    private final ObjectProperty<Affiliation> affiliation = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> urn = new SimpleObjectProperty<>();
    private final StringProperty rtcaState = new SimpleStringProperty();
    private final ObjectProperty<ImageView> killCatastrophic = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> killMobility = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> killFirepower = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> killCommunication = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> suppression = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> jammed = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> hitNoKill = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> miss = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> outOfComms = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> outOfScope = new SimpleObjectProperty<>();
    private final ObjectProperty<ImageView> timedOut = new SimpleObjectProperty<>();
    private final StringProperty lastUpdate = new SimpleStringProperty();
    private final ObjectProperty<Position> position = new SimpleObjectProperty<>();
    private final ObjectProperty<Integer> milesPid = new SimpleObjectProperty<>();
    private final ObjectProperty<EntityType> entityType = new SimpleObjectProperty<>();
    private final StringProperty militarySymbol = new SimpleStringProperty("");

    private final Calendar timestamp = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Common.DATE_TIME_WITH_MILLIS);
    private final Map<String, ImageView> imageViewMap = new HashMap<>();

    public EntityView(IEntity entity) {
        entityId = entity.getEntityId();
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        update(entity);
    }

    private ImageView getImageView(String key, boolean value) {
        key += value;
        if (value) {
            return imageViewMap.computeIfAbsent(key, k -> new ImageView(ImageLoader.getFxImage("/images/dot_red.png")));
        } else {
            return imageViewMap.computeIfAbsent(key, k -> new ImageView(ImageLoader.getFxImage("/images/dot_white.png")));
        }
    }

    public void update(IEntity entity) {
        EntityDetail entityDetail = entity.getEntityDetail();
        if (entityDetail == null) {
            source.set(null);
            name.set(null);
            description.set(null);
            affiliation.set(null);
            urn.set(null);
            rtcaState.set(null);
            killCatastrophic.set(null);
            killMobility.set(null);
            killFirepower.set(null);
            killCommunication.set(null);
            suppression.set(null);
            jammed.set(null);
            hitNoKill.set(null);
            miss.set(null);
            outOfComms.set(null);
            milesPid.set(null);
            entityType.set(null);
            militarySymbol.set("");
        } else {
            source.set(entityDetail.getSource());
            name.set(entityDetail.getName());
            description.set(entityDetail.getEntityType().getDescription());
            affiliation.set(entityDetail.getAffiliation());
            urn.set(entityDetail.getUrn());
            rtcaState.set(entityDetail.getRtcaState().getAbbreviatedString());
            killCatastrophic.set(getImageView("killCatastrophic", entityDetail.getRtcaState().isKillCatastrophic()));
            killMobility.set(getImageView("killMobility", entityDetail.getRtcaState().isKillMobility()));
            killFirepower.set(getImageView("killFirepower", entityDetail.getRtcaState().isKillFirepower()));
            killCommunication.set(getImageView("killCommunication", entityDetail.getRtcaState().isKillCommunication()));
            suppression.set(getImageView("suppression", entityDetail.getRtcaState().isSuppression()));
            jammed.set(getImageView("jammed", entityDetail.getRtcaState().isJammed()));
            hitNoKill.set(getImageView("hitNoKill", entityDetail.getRtcaState().isHitNoKill()));
            miss.set(getImageView("miss", entityDetail.getRtcaState().isMiss()));
            outOfComms.set(getImageView("outOfComms", entityDetail.isOutOfComms()));
            milesPid.set(entityDetail.getMilesPid());
            entityType.set(entityDetail.getEntityType());
            if (!militarySymbol.get().equals(entityDetail.getMilitarySymbol())) {
                militarySymbol.set(entityDetail.getMilitarySymbol());
            }
        }

        outOfScope.set(getImageView("outOfScope", !entity.isInScope()));
        timedOut.set(getImageView("timedOut", entity.isTimedOut()));
        position.set(entity.getPosition());

        long lastUpdateTime = entity.getLastUpdateTime();
        timestamp.setTimeInMillis(lastUpdateTime);
        if (lastUpdateTime > 0) {
            lastUpdate.set(simpleDateFormat.format(timestamp.getTime()));
        } else {
            lastUpdate.set("");
        }
    }

    public EntityId getEntityId() {
        return entityId;
    }

    public String getSource() {
        return source.get();
    }

    public StringProperty sourceProperty() {
        return source;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getDescription() {
        return description.get();
    }

    public StringProperty descriptionProperty() {
        return description;
    }

    public Affiliation getAffiliation() {
        return affiliation.get();
    }

    public ObjectProperty<Affiliation> affiliationProperty() {
        return affiliation;
    }

    public Integer getUrn() {
        return urn.get();
    }

    public ObjectProperty<Integer> urnProperty() {
        return urn;
    }

    public String getRtcaState() {
        return rtcaState.get();
    }

    public StringProperty rtcaStateProperty() {
        return rtcaState;
    }

    public ImageView getKillCatastrophic() {
        return killCatastrophic.get();
    }

    public ObjectProperty<ImageView> killCatastrophicProperty() {
        return killCatastrophic;
    }

    public ImageView getKillMobility() {
        return killMobility.get();
    }

    public ObjectProperty<ImageView> killMobilityProperty() {
        return killMobility;
    }

    public ImageView getKillFirepower() {
        return killFirepower.get();
    }

    public ObjectProperty<ImageView> killFirepowerProperty() {
        return killFirepower;
    }

    public ImageView getKillCommunication() {
        return killCommunication.get();
    }

    public ObjectProperty<ImageView> killCommunicationProperty() {
        return killCommunication;
    }

    public ImageView getJammed() {
        return jammed.get();
    }

    public ObjectProperty<ImageView> jammedProperty() {
        return jammed;
    }

    public ImageView getSuppression() {
        return suppression.get();
    }

    public ObjectProperty<ImageView> suppressionProperty() {
        return suppression;
    }

    public ImageView getHitNoKill() {
        return hitNoKill.get();
    }

    public ObjectProperty<ImageView> hitNoKillProperty() {
        return hitNoKill;
    }

    public ImageView getMiss() {
        return miss.get();
    }

    public ObjectProperty<ImageView> missProperty() {
        return miss;
    }

    public ImageView getOutOfComms() {
        return outOfComms.get();
    }

    public ObjectProperty<ImageView> outOfCommsProperty() {
        return outOfComms;
    }

    public ImageView getOutOfScope() {
        return outOfScope.get();
    }

    public ObjectProperty<ImageView> outOfScopeProperty() {
        return outOfScope;
    }

    public ImageView getTimedOut() {
        return timedOut.get();
    }

    public ObjectProperty<ImageView> timedOutProperty() {
        return timedOut;
    }

    public String getLastUpdate() {
        return lastUpdate.get();
    }

    public StringProperty lastUpdateProperty() {
        return lastUpdate;
    }

    public Position getPosition() {
        return position.get();
    }

    public ObjectProperty<Position> positionProperty() {
        return position;
    }

    public Integer getMilesPid() {
        return milesPid.get();
    }

    public ObjectProperty<Integer> milesPidProperty() {
        return milesPid;
    }

    public EntityType getEntityType() {
        return entityType.get();
    }

    public ObjectProperty<EntityType> entityTypeProperty() {
        return entityType;
    }

    public String getMilitarySymbol() {
        return militarySymbol.get();
    }

    public StringProperty militarySymbolProperty() {
        return militarySymbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EntityView entityView = (EntityView) o;
        return entityId.equals(entityView.entityId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(entityId);
    }
}
