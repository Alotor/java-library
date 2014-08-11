package com.urbanairship.api.push.parse.notification.wns;

import com.urbanairship.api.push.model.notification.wns.WNSAudioData;
import com.urbanairship.api.push.model.notification.Notification;
import com.urbanairship.api.push.model.Platform;
import com.urbanairship.api.push.parse.*;
import com.urbanairship.api.common.parse.*;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.TypeReference;
import org.codehaus.jackson.map.DeserializationContext;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.Set;

public class WNSAudioReader implements JsonObjectReader<WNSAudioData> {

    private final WNSAudioData.Builder builder;

    public WNSAudioReader() {
        this.builder = WNSAudioData.newBuilder();
    }

    public void readSound(JsonParser parser, DeserializationContext context) throws IOException {
        builder.setSound(WNSSoundDeserializer.INSTANCE.deserialize(parser, context));
    }

    public void readLoop(JsonParser parser) throws IOException {
        builder.setLoop(BooleanFieldDeserializer.INSTANCE.deserialize(parser, "loop"));
    }

    @Override
    public WNSAudioData validateAndBuild() throws IOException {
        try {
            return builder.build();
        } catch (Exception e) {
            throw new APIParsingException("Toast audio element must contain a valid src.");
        }
    }
}
