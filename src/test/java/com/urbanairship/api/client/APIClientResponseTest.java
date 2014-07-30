package com.urbanairship.api.client;

import com.urbanairship.api.schedule.model.ScheduleResponseObject;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;

import org.junit.Test;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class APIClientResponseTest {


    @Test
    public void testAPIScheduleResponse(){
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP",1,1), 200, "OK"));
        APIScheduleResponse scheduleResponse = APIScheduleResponse.newBuilder()
                .setScheduleUrls(Arrays.asList("ID1", "ID2"))
                .setOperationId("ID")
                .build();
        APIClientResponse.Builder<APIScheduleResponse> builder =
                APIClientResponse.newScheduleResponseBuilder()
                                 .setApiResponse(scheduleResponse)
                                 .setHttpResponse(httpResponse);
        APIClientResponse<APIScheduleResponse> testResponse = builder.build();
        assertTrue("HTTP response not set properly",
                   testResponse.getHttpResponse().equals(httpResponse));

        assertTrue("APIResponse not set properly",
                   testResponse.getApiResponse().equals(scheduleResponse));
    }

    @Test
    public void testAPIListScheduleResponse(){
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP",1,1), 200, "OK"));

        ScheduleResponseObject sample = new ScheduleResponseObject();
        sample.setUrl("http://sample.com/");
        List<ScheduleResponseObject> samplelist = new ArrayList<ScheduleResponseObject>();
        samplelist.add(sample);

        APIListScheduleResponse listScheduleResponse = APIListScheduleResponse.newBuilder()
                .setCount(5)
                .setTotalCount(6)
                .setSchedule(samplelist)
                .build();
        APIClientResponse.Builder<APIListScheduleResponse> builder =
                APIClientResponse.newListScheduleResponseBuilder()
                        .setApiResponse(listScheduleResponse)
                        .setHttpResponse(httpResponse);
        APIClientResponse<APIListScheduleResponse> testResponse = builder.build();
        assertTrue("HTTP response not set properly",
                testResponse.getHttpResponse().equals(httpResponse));

        assertTrue("APIResponse not set properly",
                testResponse.getApiResponse().equals(listScheduleResponse));
    }

    @Test
    public void testAPIListTagsResponse(){
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP",1,1), 200, "OK"));

        List<String> listOTags = new ArrayList<String>();
        listOTags.add("Puppies");
        listOTags.add("Kitties");

        APIListTagsResponse listTagsResponse = APIListTagsResponse.newBuilder()
                .setTags(listOTags)
                .build();
        APIClientResponse.Builder<APIListTagsResponse> builder =
                APIClientResponse.newListTagsResponseBuilder()
                        .setApiResponse(listTagsResponse)
                        .setHttpResponse(httpResponse);
        APIClientResponse<APIListTagsResponse> testResponse = builder.build();
        assertTrue("HTTP response not set properly",
                testResponse.getHttpResponse().equals(httpResponse));

        assertTrue("APIResponse not set properly",
                testResponse.getApiResponse().equals(listTagsResponse));
    }

    @Test
    public void testAPIPushResponse(){
        HttpResponse httpResponse = new BasicHttpResponse(new BasicStatusLine(
                new ProtocolVersion("HTTP",1,1), 200, "OK"));
        APIPushResponse pushResponse = APIPushResponse.newBuilder()
                .setPushIds(Arrays.asList("ID1", "ID2"))
                .setOperationId("OpID")
                .build();
        APIClientResponse.Builder<APIPushResponse>builder =
                APIClientResponse.newPushResponseBuilder()
                .setApiResponse(pushResponse)
                .setHttpResponse(httpResponse);
        APIClientResponse<APIPushResponse> apiResponse = builder.build();
        assertTrue("HTTP response not set properly",
                   apiResponse.getHttpResponse().equals(httpResponse));
        assertTrue("APIResponse not set properly",
                   apiResponse.getApiResponse().equals(pushResponse));
    }

}
