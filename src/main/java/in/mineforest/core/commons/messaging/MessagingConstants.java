package in.mineforest.core.commons.messaging;

public enum MessagingConstants {
    ENGINE_CHANNEL("minefrost:channel");

    public final String channelName;

    MessagingConstants(String channelName) {
        this.channelName = channelName;
    }
}
