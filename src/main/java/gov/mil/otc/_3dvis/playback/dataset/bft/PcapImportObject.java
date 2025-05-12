package gov.mil.otc._3dvis.playback.dataset.bft;

import gov.mil.otc._3dvis.data.DataManager;
import gov.mil.otc._3dvis.datamodel.*;
import gov.mil.otc._3dvis.entity.EntityManager;
import gov.mil.otc._3dvis.entity.PlaybackEntity;
import gov.mil.otc._3dvis.entity.base.AdHocEntity;
import gov.mil.otc._3dvis.entity.base.EntityId;
import gov.mil.otc._3dvis.entity.base.IEntity;
import gov.mil.otc._3dvis.event.C2MessageEvent;
import gov.mil.otc._3dvis.event.EventManager;
import gov.mil.otc._3dvis.playback.dataset.ImportObject;
import gov.mil.otc._3dvis.vmf.*;
import gov.mil.otc._3dvis.vmf.pcap.ProtocolParser;
import javafx.scene.layout.VBox;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PcapImportObject extends ImportObject<List<VmfMessage>> {

    public static PcapImportObject scanAndCreate(File file, List<Integer> urnFilter) {
        ProtocolParser protocolParser = new ProtocolParser();
        List<VmfMessage> messages = protocolParser.processPcap(file);
        PcapImportObject importObject = new PcapImportObject(messages, file, urnFilter);
        if (messages.isEmpty()) {
            importObject.setMissing(true);
        }
        importObject.determineIsNew();
        return importObject;
    }

    private Map<Integer, AdHocEntity> entityMap = new HashMap<>();
    private final Map<Integer, IEntity> urnEntityMap = new HashMap<>();
    private final List<Integer> urnFilter = new ArrayList<>();

    public PcapImportObject(List<VmfMessage> object, File file, List<Integer> urnFilter) {
        super(object, file.getName());

        this.urnFilter.addAll(urnFilter);
    }

    public void setUto(UtoImportObject utoImportObject) {
        entityMap = utoImportObject.getEntityMap();
    }

    @Override
    public VBox getDisplayPane() {
        return null;
    }

    @Override
    public void doImport() {
        List<K0101> k0101List = new ArrayList<>();

        for (VmfMessage message : getObject()) {
            if (isFiltered(message)) {
                continue;
            }
            if (message instanceof K0101 k0101) {
                k0101List.add(k0101);
            } else if (message instanceof K0501 k0501) {
                processK0501(k0501);
            } else if (message instanceof Sdsa sdsa) {
                processSdsa(sdsa);
            }
        }

        long startTime = 0;//scopeStartPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;
        long stopTime = System.currentTimeMillis();//scopeEndPicker.getDateTimeValue().toEpochSecond(ZoneOffset.UTC) * 1000;

        for (AdHocEntity adHocEntity : entityMap.values()) {
            if (adHocEntity.getTspiDataList().isEmpty()) {
                continue;
            }
            EntityId entityId = DataManager.getNextGenericEntityId();
            PlaybackEntity playbackEntity = new PlaybackEntity(entityId);
            TspiData firstTspiData = adHocEntity.getTspiDataList().getFirst();
            String militarySymbol = EntityTypeUtility.getTacticalSymbol(adHocEntity.getEntityType());
            militarySymbol = MilitarySymbolUtility.validateSymbolCode(militarySymbol, adHocEntity.getAffiliation());
            EntityDetail entityDetail = new EntityDetail.Builder()
                    .setTimestamp(firstTspiData.getTimestamp())
                    .setEntityType(adHocEntity.getEntityType())
                    .setName(adHocEntity.getName())
                    .setSource(adHocEntity.getSource())
                    .setAffiliation(adHocEntity.getAffiliation())
                    .setMilitarySymbol(militarySymbol)
                    .setUrn(adHocEntity.getUrn())
                    .build();
            playbackEntity.addEntityDetail(entityDetail);

            for (TspiData tspiData : adHocEntity.getTspiDataList()) {
                playbackEntity.addTspi(tspiData);
            }

            EntityScope entityScope = new EntityScope(startTime, stopTime);
            playbackEntity.addEntityScope(entityScope);
            EntityManager.addEntity(playbackEntity);

            if (!urnEntityMap.containsKey(adHocEntity.getUrn())) {
                urnEntityMap.put(adHocEntity.getUrn(), playbackEntity);
            }
        }

        for (K0101 k0101 : k0101List) {
            processK0101(k0101);
        }
    }

    @Override
    public void doImport(IEntity entity) {
        // not implemented
    }

    private boolean isFiltered(VmfMessage message) {
        if (urnFilter.isEmpty()) {
            return false;
        }

        if (urnFilter.contains(message.getHeader().getSenderUrn())) {
            return false;
        }

        for (int urn : urnFilter) {
            if (message.getHeader().getDestUrns().contains(urn)) {
                return false;
            }
        }

        return true;
    }

    private void processK0101(K0101 k0101) {
        IEntity entity = getSenderEntity(k0101);
        if (entity != null) {
            C2MessageEvent c2MessageEvent = new C2MessageEvent(entity, k0101);
            EventManager.addEvent(c2MessageEvent);
        }
    }

    private IEntity getSenderEntity(VmfMessage vmfMessage) {
        IEntity senderEntity = urnEntityMap.get(vmfMessage.getHeader().getSenderUrn());
        if (senderEntity == null) {
            for (IEntity entity : EntityManager.getEntities()) {
                EntityDetail entityDetail = entity.getEntityDetailBefore(vmfMessage.getHeader().getOriginatorTime());
                if (entityDetail != null && entityDetail.getUrn() == vmfMessage.getHeader().getSenderUrn()) {
                    urnEntityMap.put(vmfMessage.getHeader().getSenderUrn(), entity);
                    return entity;
                }
            }
        }
        return senderEntity;
    }

    private void processK0501(K0501 k0501) {
        for (K0501.PositionReport report : k0501.getReports()) {
            if (report.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = entityMap.get(report.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                adHocEntity.setName(String.valueOf(report.getUrn()));
                adHocEntity.setSource("BFT");
                adHocEntity.setAffiliation(Affiliation.FRIENDLY);
                adHocEntity.setUrn(report.getUrn());
                entityMap.put(report.getUrn(), adHocEntity);
            }

            if (report.getPosition().getLatitude().getDegrees() != 0 && report.getPosition().getLongitude().getDegrees() != 0) {
                adHocEntity.getTspiDataList().add(new TspiData(report.getTime().getTimeInMillis(), report.getPosition()));
            }
        }
    }

    private void processSdsa(Sdsa sdsa) {
        for (Sdsa.Record record : sdsa.getRecords()) {
            if (record.getUrn() <= 0) {
                continue;
            }

            AdHocEntity adHocEntity = entityMap.get(record.getUrn());
            if (adHocEntity == null) {
                adHocEntity = new AdHocEntity();
                entityMap.put(record.getUrn(), adHocEntity);
            }

            EntityType entityType = VmfDictionary.getEntityType(
                    record.getDimension(),
                    record.getNationality(),
                    record.getType(),
                    record.getSubType());
            adHocEntity.setEntityType(entityType);

            adHocEntity.setAffiliation(Affiliation.FRIENDLY);
            adHocEntity.setUrn(record.getUrn());
            adHocEntity.setName(String.valueOf(record.getUrn()));

            // set name
            if (!record.getFullName().isEmpty()) {
                adHocEntity.setName(record.getFullName());
            } else if (!record.getShortName().isEmpty()) {
                adHocEntity.setName(record.getShortName());
            } else if (!record.getAlias().isEmpty()) {
                adHocEntity.setName(record.getAlias());
            }

            // set icon
            adHocEntity.setMilitarySymbol(record.getSymbol());
        }
    }
}
