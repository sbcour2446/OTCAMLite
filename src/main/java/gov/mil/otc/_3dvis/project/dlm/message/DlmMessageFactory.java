package gov.mil.otc._3dvis.project.dlm.message;

public class DlmMessageFactory {

    public static DlmMessage createMessage(GenericDlmMessage genericDlmMessage) {
        return switch (genericDlmMessage.getMessageType()) {
            case DwellMessage.MESSAGE_CODE -> DwellMessage.create(genericDlmMessage.getData());
            case FusionMessage.MESSAGE_CODE -> FusionMessage.create(genericDlmMessage.getData());
            case LaunchDiscreteTimeMessage.MESSAGE_CODE -> LaunchDiscreteTimeMessage.create(genericDlmMessage.getData());
            case LmsVersionMessage.MESSAGE_CODE -> LmsVersionMessage.create(genericDlmMessage.getData());
            case PingRadarStatusMessage.MESSAGE_CODE -> PingRadarStatusMessage.create(genericDlmMessage.getData());
            case TextMessage.MESSAGE_CODE -> TextMessage.create(genericDlmMessage.getData());
            case TrackMessage.MESSAGE_CODE -> TrackMessage.create(genericDlmMessage.getData());
            default -> genericDlmMessage;
        };
    }

    public static DlmMessage createMessage(int type, byte[] data) {
        return switch (type) {
            case DwellMessage.MESSAGE_CODE -> DwellMessage.create(data);
            case FusionMessage.MESSAGE_CODE -> FusionMessage.create(data);
            case LaunchDiscreteTimeMessage.MESSAGE_CODE -> LaunchDiscreteTimeMessage.create(data);
            case LmsVersionMessage.MESSAGE_CODE -> LmsVersionMessage.create(data);
            case PingRadarStatusMessage.MESSAGE_CODE -> PingRadarStatusMessage.create(data);
            case TextMessage.MESSAGE_CODE -> TextMessage.create(data);
            case TrackMessage.MESSAGE_CODE -> TrackMessage.create(data);
            default -> new GenericDlmMessage(type, 0, data);
        };
    }

    private DlmMessageFactory() {
    }
}
