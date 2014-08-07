/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.api.client;


import com.urbanairship.api.client.parse.APIResponseObjectMapper;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Handle server responses for Scheduling.
 */
public final class ScheduleAPIResponseHandler implements
        ResponseHandler<APIClientResponse<APIScheduleResponse>> {

    /**
     * Handle HttpResponse. Returns an APIClientResponse on success, or
     * raises an APIRequestException, or an IOException.
     * @param response HttpResponse returned from the Request
     * @return APIClientResponse appropriate for the request.
     * @throws IOException
     */
    @Override
    public APIClientResponse<APIScheduleResponse> handleResponse(HttpResponse response)
            throws IOException {

        // HTTP response code
        int statusCode = response.getStatusLine().getStatusCode();

        // Documented cases
        switch (statusCode){
            case HttpStatus.SC_CREATED:
                return handleSuccessfulSchedule(response);

            case HttpStatus.SC_BAD_REQUEST:
            case HttpStatus.SC_UNAUTHORIZED:
            case HttpStatus.SC_FORBIDDEN:
                throw APIRequestException.exceptionForResponse(response);

        }

        // Uncommon, or unknown
        if (statusCode >= 200 && statusCode < 300){
            return handleSuccessfulSchedule(response);
        }
        // Handle unhandled server error codes
        else {
            throw APIRequestException.exceptionForResponse(response);
        }
    }

    /*
     * Create an APIResponse for the successful schedule request.
     * Any exceptions thrown by HttpResponse object that are related to
     * closing the response body are ignored.
     * @param response
     * @return APIClientResponse<APIScheduleResponse>
     * @throws IOException
     */
    private APIClientResponse<APIScheduleResponse> handleSuccessfulSchedule(HttpResponse response)
            throws IOException {
        String jsonPayload = EntityUtils.toString(response.getEntity());
        // toss out exceptions related to closing the entity
        EntityUtils.consumeQuietly(response.getEntity());
        ObjectMapper mapper = APIResponseObjectMapper.getInstance();
        APIScheduleResponse scheduleResponse =
                mapper.readValue(jsonPayload, APIScheduleResponse.class);
        APIClientResponse.Builder<APIScheduleResponse> builder =
                APIClientResponse.newScheduleResponseBuilder();
        builder.setHttpResponse(response);
        builder.setApiResponse(scheduleResponse);
        return builder.build();
    }
}
