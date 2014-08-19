package com.urbanairship.api.push.parse.notification.ios;

import com.urbanairship.api.push.model.notification.ios.IOSDevicePayload;
import com.urbanairship.api.common.parse.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.DeserializationContext;

import java.io.IOException;

public class IOSDevicePayloadReader implements JsonObjectReader<IOSDevicePayload> {

    private IOSDevicePayload.Builder builder = IOSDevicePayload.newBuilder();
    private IOSAlertDataDeserializer alertDS = new IOSAlertDataDeserializer();

    public IOSDevicePayloadReader() {
    }

    public void readAlert(JsonParser parser, DeserializationContext context) throws IOException {
        builder.setAlert(alertDS.deserialize(parser, context));
    }

    public void readSound(JsonParser parser, DeserializationContext context) throws IOException {
        builder.setSound(StringFieldDeserializer.INSTANCE.deserialize(parser, "sound"));
    }

    public void readBadge(JsonParser parser, DeserializationContext context) throws IOException {
        builder.setBadge(IOSBadgeDataDeserializer.INSTANCE.deserialize(parser, "badge"));
    }

    public void readContentAvailable(JsonParser parser, DeserializationContext context) throws IOException {
        builder.setContentAvailable(BooleanFieldDeserializer.INSTANCE.deserialize(parser, "content_available"));
    }

    public void readExtra(JsonParser parser, DeserializationContext context) throws IOException {
        builder.addAllExtraEntries(MapOfStringsDeserializer.INSTANCE.deserialize(parser, "extra"));
    }

    @Override
    public IOSDevicePayload validateAndBuild() throws IOException {
        try {
            return builder.build();
        } catch (Exception e) {
            throw new APIParsingException(e.getMessage(), e);
        }
    }
}
