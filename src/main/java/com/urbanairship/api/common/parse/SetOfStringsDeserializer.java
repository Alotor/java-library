/*
 * Copyright (c) 2013-2014.  Urban Airship and Contributors
 */

package com.urbanairship.api.common.parse;

import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.type.TypeReference;

import java.io.IOException;
import java.util.Set;

public class SetOfStringsDeserializer {

    public static final SetOfStringsDeserializer INSTANCE = new SetOfStringsDeserializer();

    private SetOfStringsDeserializer() { }

    public Set<String> deserialize(JsonParser parser, String fieldName) throws IOException {
        try {
            return parser.readValueAs(new TypeReference<Set<String>>() {});
        }
        catch (IOException e) {
            APIParsingException.raise(String.format("%s must be an array of simple strings", fieldName), parser);
        }
        return null;
    }
}
