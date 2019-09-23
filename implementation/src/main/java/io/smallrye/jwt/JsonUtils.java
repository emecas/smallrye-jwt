package io.smallrye.jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

public class JsonUtils {

    private JsonUtils() {
    }

    public static JsonObject replaceMap(Map<String, Object> map) {
        JsonObjectBuilder builder = Json.createObjectBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object entryValue = entry.getValue();
            if (entryValue instanceof Map) {
                @SuppressWarnings("unchecked")
                JsonObject entryJsonObject = replaceMap((Map<String, Object>) entryValue);
                builder.add(entry.getKey(), entryJsonObject);
            } else if (entryValue instanceof List) {
                JsonArray array = (JsonArray) wrapValue(entryValue);
                builder.add(entry.getKey(), array);
            } else if (entryValue instanceof Long || entryValue instanceof Integer) {
                long lvalue = ((Number) entryValue).longValue();
                builder.add(entry.getKey(), lvalue);
            } else if (entryValue instanceof Double || entryValue instanceof Float) {
                double dvalue = ((Number) entryValue).doubleValue();
                builder.add(entry.getKey(), dvalue);
            } else if (entryValue instanceof Boolean) {
                boolean flag = ((Boolean) entryValue).booleanValue();
                builder.add(entry.getKey(), flag);
            } else if (entryValue instanceof String) {
                builder.add(entry.getKey(), entryValue.toString());
            }
        }
        return builder.build();
    }

    private static JsonArray toJsonArray(Collection<?> collection) {
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();

        for (Object element : collection) {
            if (element instanceof String) {
                arrayBuilder.add(element.toString());
            } else if (element == null) {
                arrayBuilder.add(JsonValue.NULL);
            } else {
                JsonValue jvalue = wrapValue(element);
                arrayBuilder.add(jvalue);
            }
        }

        return arrayBuilder.build();
    }

    public static JsonValue wrapValue(Object value) {
        JsonValue jsonValue = null;

        if (value instanceof JsonValue) {
            // This may already be a JsonValue
            jsonValue = (JsonValue) value;
        } else if (value instanceof String) {
            jsonValue = Json.createValue(value.toString());
        } else if ((value instanceof Long) || (value instanceof Integer)) {
            jsonValue = Json.createValue(((Number) value).longValue());
        } else if (value instanceof Number) {
            jsonValue = Json.createValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            jsonValue = (Boolean) value ? JsonValue.TRUE : JsonValue.FALSE;
        } else if (value instanceof Collection) {
            jsonValue = toJsonArray((Collection<?>) value);
        } else if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            JsonObject entryJsonObject = replaceMap((Map<String, Object>) value);
            jsonValue = entryJsonObject;
        }

        return jsonValue;
    }
}