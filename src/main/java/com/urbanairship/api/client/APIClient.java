/*
 * Copyright 2013 Urban Airship and Contributors
 */

package com.urbanairship.api.client;

import com.google.common.base.Preconditions;
import com.urbanairship.api.client.model.*;
import com.urbanairship.api.push.model.PushPayload;
import com.urbanairship.api.schedule.model.SchedulePayload;
import com.urbanairship.api.tag.model.AddRemoveDeviceFromTagPayload;
import com.urbanairship.api.tag.model.BatchModificationPayload;

import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.entity.ContentType;
import org.apache.http.params.CoreProtocolPNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URI;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * The APIClient class handles HTTP requests to the Urban Airship API
 */

public class APIClient {

    /* Header keys/values */
    private final static String CONTENT_TYPE_KEY = "Content-type";
    private final static String ACCEPT_KEY = "Accept";
    private final static String UA_APPLICATION_JSON = "application/vnd.urbanairship+json;";

    /* URI Paths */
    private final static String API_PUSH_PATH = "/api/push/";
    private final static String API_VALIDATE_PATH = "/api/push/validate/";
    private final static String API_SCHEDULE_PATH = "/api/schedules/";
    private final static String API_TAGS_PATH = "/api/tags/";
    private final static String API_TAGS_BATCH_PATH = "/api/tags/batch/";

    /* User auth */
    private final String appKey;
    private final String appSecret;

    /* URI, version */
    private final URI baseURI;
    private final Number version;

    /* HTTP */
    private final HttpHost uaHost;

    private final String proxyhost; // proxy hostname
    private final int proxyport; // proxy portnumber
    private final String proxyuserid; // proxy userid
    private final String proxypassword; // proxy password

    private final static Logger logger = LoggerFactory.getLogger("com.urbanairship.api");

    public static Builder newBuilder(){
        return new Builder();
    }

    private APIClient(String appKey, String appSecret, String baseURI, Number version, String proxyhost, int proxyport, String proxyuserid, String proxypassword) {
        Preconditions.checkArgument(StringUtils.isNotBlank(appKey),
                "App key must be provided.");
        Preconditions.checkArgument(StringUtils.isNotBlank(appSecret),
                "App secret must be provided");
        this.appKey = appKey;
        this.appSecret = appSecret;
        this.baseURI = URI.create(baseURI);
        this.version = version;
        this.uaHost = new HttpHost(URI.create(baseURI).getHost(), 443, "https");

        this.proxyhost = proxyhost; // proxy hostname
        this.proxyport = proxyport; // proxy portnumber
        this.proxyuserid = proxyuserid; // proxy userid
        this.proxypassword = proxypassword; // proxy password
    }

    public String getAppSecret() { return appSecret; }
    public String getAppKey() { return appKey; }

    /* Add the version number to the default version header */

    private String versionedAcceptHeader(Number version){
        return String.format("%s version=%s", UA_APPLICATION_JSON, version.toString());
    }

    /*
    Retrieves Java Client API Version
    */
    public String getUserAgent() {
        InputStream stream = getClass().getResourceAsStream("/client.properties");

        if (stream == null) { return "UNKNOWN"; }

        Properties props = new Properties();

        try {
            props.load(stream);
            stream.close();
            return "UAJavaLib/" + props.get("client.version");
        } catch (IOException e) {
            return "UNKNOWN";
        }
    }
    
    /* Provisioning Methods */

    private Request provisionRequest(Request object) {
        return object
                .config(CoreProtocolPNames.USER_AGENT, getUserAgent())
                .addHeader(CONTENT_TYPE_KEY, versionedAcceptHeader(version))
                .addHeader(ACCEPT_KEY, versionedAcceptHeader(version));
    }

    private Executor provisionExecutor(Request request) {
        Executor executor = Executor.newInstance()
                .auth(uaHost, appKey, appSecret)
                .authPreemptive(uaHost);

        // If proxy has been set, set it on the executor
        if( proxyhost != null ) {
			HttpHost proxy = new HttpHost(proxyhost, proxyport);
			executor.authPreemptiveProxy(proxy);
			request = request.viaProxy(proxy);
			//If proxy authentication has been set, set it on the executor
			if (proxyuserid != null) {
				UsernamePasswordCredentials credentials = new UsernamePasswordCredentials(proxyuserid, proxypassword);
				executor.auth(proxy, credentials);
			}
		}

        return executor;
    }

    /* Push API */

    public APIClientResponse<APIPushResponse> push(PushPayload payload) throws IOException {
        Preconditions.checkNotNull(payload, "Payload required when executing a push operation");
        Request request = provisionRequest(Request.Post(baseURI.resolve(API_PUSH_PATH)));
        request.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing push request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new PushAPIResponseHandler());
    }

    public APIClientResponse<APIPushResponse> validate(PushPayload payload) throws IOException {
        Preconditions.checkNotNull(payload, "Payload required when executing a validate push operation");
        Request request = provisionRequest(Request.Post(baseURI.resolve(API_VALIDATE_PATH)));
        request.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing validate push request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new PushAPIResponseHandler());
    }

    /* Schedules API */

    public APIClientResponse<APIScheduleResponse> schedule(SchedulePayload payload) throws IOException {
        Preconditions.checkNotNull(payload, "Payload required when scheduling a push request");
        Request request = provisionRequest(Request.Post(baseURI.resolve(API_SCHEDULE_PATH)));
        request.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing schedule request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new ScheduleAPIResponseHandler());
    }

    public APIClientResponse<APIListAllSchedulesResponse> listAllSchedules() throws IOException {
        Request request = provisionRequest(Request.Get(baseURI.resolve(API_SCHEDULE_PATH)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing list all schedules request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new ListAllSchedulesAPIResponseHandler());
    }

    public APIClientResponse<APIListAllSchedulesResponse> listAllSchedules(String start, int limit, String order) throws IOException {
        String path = "/api/schedules" + "?" + "start=" + start + "&limit=" + limit +"&order=" + order;
        Request request = provisionRequest(Request.Get(baseURI.resolve(path)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing list all schedules request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new ListAllSchedulesAPIResponseHandler());
    }

    public APIClientResponse<APIListAllSchedulesResponse> listAllSchedules(String next_page) throws IOException, URISyntaxException {
        URI np = new URI(next_page);
        Request request = provisionRequest(Request.Get(baseURI.resolve(np.getPath() + "?" + np.getQuery())));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing list all schedules request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new ListAllSchedulesAPIResponseHandler());
    }

    public APIClientResponse<SchedulePayload> listSchedule(String id) throws IOException {
        Request request = provisionRequest(Request.Get(baseURI.resolve(API_SCHEDULE_PATH + id)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing list specific schedule request %s", request));
        }

        return provisionExecutor(request).execute(request).handleResponse(new ListScheduleAPIResponseHandler());
    }

    public APIClientResponse<APIScheduleResponse> updateSchedule(SchedulePayload payload, String id) throws IOException {
        Preconditions.checkNotNull(payload, "Payload is required when updating schedule");
        Request req = provisionRequest(Request.Put(baseURI.resolve(API_SCHEDULE_PATH + id)));
        req.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing update schedule request %s", req));
        }

        return provisionExecutor(req).execute(req).handleResponse(new ScheduleAPIResponseHandler());
    }

    public HttpResponse deleteSchedule(String id) throws IOException {
        Request req = provisionRequest(Request.Delete(baseURI.resolve(API_SCHEDULE_PATH + id)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing delete schedule request %s", req));
        }

        return provisionExecutor(req).execute(req).returnResponse();
    }

    /* Tags API */

    public APIClientResponse<APIListTagsResponse> listTags() throws IOException {
        Request req = provisionRequest(Request.Get(baseURI.resolve(API_TAGS_PATH)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing list tags request %s", req));
        }

        return provisionExecutor(req).execute(req).handleResponse(new ListTagsAPIResponseHandler());
    }

    public HttpResponse createTag(String tag) throws IOException {
        Request req = provisionRequest(Request.Put(baseURI.resolve(API_TAGS_PATH + tag)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing create tag request %s", req));
        }

        return provisionExecutor(req).execute(req).returnResponse();
    }

    public HttpResponse deleteTag(String tag) throws IOException {
        Request req = provisionRequest(Request.Delete(baseURI.resolve(API_TAGS_PATH + tag)));

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing delete tag request %s", req));
        }

        return provisionExecutor(req).execute(req).returnResponse();
    }

    public HttpResponse addRemoveDevicesFromTag(String tag, AddRemoveDeviceFromTagPayload payload) throws IOException {
        Preconditions.checkNotNull(payload, "Payload is required when adding and/or removing devices from a tag");
        Request req = provisionRequest(Request.Post(baseURI.resolve(API_TAGS_PATH + tag)));
        req.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing add/remove devices from tag request %s", req));
        }

        return provisionExecutor(req).execute(req).returnResponse();
    }

    public HttpResponse batchModificationOfTags(BatchModificationPayload payload) throws IOException {
        Preconditions.checkNotNull(payload, "Payload is required when performing batch modification of tags");
        Request req = provisionRequest(Request.Post(baseURI.resolve(API_TAGS_BATCH_PATH)));
        req.bodyString(payload.toJSON(), ContentType.APPLICATION_JSON);

        if (logger.isDebugEnabled()) {
            logger.debug(String.format("Executing batch modification of tags request %s", req));
        }

        return provisionExecutor(req).execute(req).returnResponse();
    }

    /* Object methods */

    @Override
    public String toString() {
        return "APIClient\nAppKey:"+ appKey +"\nAppSecret:" + appSecret + "\n";
    }

    /* Builder for APIClient */

    public static class Builder {

        private String key;
        private String secret;
        private String baseURI;
        private Number version;

        /** For setting up calls through a HTTPS proxy */
        private String proxyhost;
        private int proxyport;
        private String proxyuserid;
        private String proxypassword;

        private Builder(){
            baseURI = "https://go.urbanairship.com";
            version = 3;
        }

        public Builder setKey(String key) {
            this.key = key;
            return this;
        }

        public Builder setSecret(String appSecret) {
            this.secret = appSecret;
            return this;
        }

        public Builder setBaseURI(String URI){
            this.baseURI = URI;
            return this;
        }

        public Builder setVersion(Number version){
            this.version = version;
            return this;
        }

        /**
         * @param hostName hostname of proxy
         * @param portNumber portnumber of proxy
         * @param userId user id for proxy (optional)
         * @param password password for proxy (optional)
         */
        public Builder setProxy(String hostName, int portNumber, String userId, String password) {
            this.proxyhost = hostName;
            this.proxyport = portNumber;
            this.proxyuserid = userId;
            this.proxypassword = password;
            return this;
        }

        /**
         * Build the APIClient using the given key, secret, baseURI and version.
         * @return APIClient
         */
        public APIClient build() {
            Preconditions.checkNotNull(key, "app key needed to build APIClient");
            Preconditions.checkNotNull(secret, "app secret needed to build APIClient");
            Preconditions.checkNotNull(baseURI, "base URI needed to build APIClient");
            Preconditions.checkNotNull(version, "version needed to build APIClient");
            return new APIClient(key, secret, baseURI, version, proxyhost, proxyport, proxyuserid, proxypassword);
        }

    }
}
