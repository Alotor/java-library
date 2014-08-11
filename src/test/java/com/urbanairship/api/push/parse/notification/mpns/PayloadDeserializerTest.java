package com.urbanairship.api.push.parse.notification.mpns;

import com.urbanairship.api.push.model.notification.mpns.MPNSToastData;
import com.urbanairship.api.push.parse.*;
import com.urbanairship.api.common.parse.*;
import com.urbanairship.api.push.model.notification.mpns.MPNSDevicePayload;
import com.urbanairship.api.push.model.notification.mpns.MPNSPush;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import static org.junit.Assert.*;

public class PayloadDeserializerTest {
    private static final ObjectMapper mapper = PushObjectMapper.getInstance();

    @Test
    public void testSimpleToast() throws Exception {
        String json
            = "{"
            + "  \"toast\": {"
            + "    \"text1\": \"Title\""
            + "  }"
            + "}";

        MPNSToastData expectedToast = MPNSToastData.newBuilder()
            .setText1("Title")
            .build();

        MPNSDevicePayload payload = mapper.readValue(json, MPNSDevicePayload.class);
        assertNotNull(payload.getBody());

        MPNSPush body = payload.getBody().get();
        assertEquals(MPNSPush.Type.TOAST, body.getType());
        assertEquals(expectedToast, body.getToast().get());
    }

    @Test
    public void testBatchInterval() throws Exception {
        String json
            = "{"
            + "  \"toast\": {"
            + "    \"text1\": \"Title\""
            + "  },"
            + "  \"batching_interval\":\"immediate\""
            + "}";


        MPNSDevicePayload payload = mapper.readValue(json, MPNSDevicePayload.class);
        assertNotNull(payload.getBody());

        MPNSPush body = payload.getBody().get();
        assertEquals(MPNSPush.BatchingInterval.IMMEDIATE, body.getBatchingInterval());
    }

    @Test(expected=APIParsingException.class)
    public void testValidate_Empty() throws Exception  {
        mapper.readValue("{}", MPNSDevicePayload.class);
    }

    @Test(expected=APIParsingException.class)
    public void testValidate_AlertAndToast() throws Exception  {
        mapper.readValue("{\"alert\":\"wat\", \"toast\" : { \"text1\":\"foo\"}}",
                         MPNSDevicePayload.class);
    }

    @Test(expected=APIParsingException.class)
    public void testValidate_RawUnsupported() throws Exception  {
        mapper.readValue("{\"type\":\"raw\"}", MPNSDevicePayload.class);
    }

    @Test(expected=APIParsingException.class)
    public void testValidate_ToastMismatch() throws Exception {
        mapper.readValue("{\"type\":\"tile\", \"toast\" : {}}", MPNSDevicePayload.class);
    }
}
