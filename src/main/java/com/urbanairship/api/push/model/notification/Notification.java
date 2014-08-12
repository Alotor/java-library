package com.urbanairship.api.push.model.notification;

import com.urbanairship.api.push.model.DeviceType;
import com.urbanairship.api.push.model.PushModelObject;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;

import java.util.Map;

public final class Notification extends PushModelObject {

    private final Optional<String> alert;
    private final ImmutableMap<NotificationPayloadOverrideKey, ? extends DevicePayloadOverride> platformPayloadOverrides;

    public static Builder newBuilder() {
        return new Builder();
    }

    public Notification(Optional<String> alert,
                        ImmutableMap<NotificationPayloadOverrideKey, ? extends DevicePayloadOverride> platformPayloadOverrides) {
        this.alert = alert;
        this.platformPayloadOverrides = platformPayloadOverrides;
    }

    public Optional<String> getAlert() {
        return alert;
    }

    public Optional<ImmutableSet<DeviceType>> getOverridePlatforms() {
        if (platformPayloadOverrides == null || platformPayloadOverrides.size() == 0 ) {
            return Optional.<ImmutableSet<DeviceType>>absent();
        } else {
            ImmutableSet.Builder<DeviceType> builder = ImmutableSet.<DeviceType>builder();
            for (NotificationPayloadOverrideKey key : platformPayloadOverrides.keySet()) {
                builder.add(key.getDeviceType());
            }
            return Optional.of(builder.build());
        }
    }

    @SuppressWarnings("unchecked")
    public <O extends DevicePayloadOverride> Optional<O> getPlatformOverride(DeviceType deviceType, Class<O> overrideType) {
        // Safe because the builder enforces the tie between the Class key and the value in the map
        return Optional.fromNullable((O) platformPayloadOverrides.get(new NotificationPayloadOverrideKey(deviceType, overrideType)));
    }

    public Map<DeviceType, DevicePayloadOverride> getPlatformPayloadOverrides() {
        Map<DeviceType, DevicePayloadOverride> overrides = Maps.newHashMap();
        for (Map.Entry<NotificationPayloadOverrideKey, ? extends DevicePayloadOverride> entry : platformPayloadOverrides.entrySet()) {
            overrides.put(entry.getKey().getDeviceType(), entry.getValue());
        }

        return overrides;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Notification that = (Notification) o;

        if (alert != null ? !alert.equals(that.alert) : that.alert != null) {
            return false;
        }
        if (platformPayloadOverrides != null ? !platformPayloadOverrides.equals(that.platformPayloadOverrides)
                : that.platformPayloadOverrides != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = alert != null ? alert.hashCode() : 0;
        result = 31 * result + (platformPayloadOverrides != null ? platformPayloadOverrides.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "alert=" + alert +
                ", platformPayloadOverrides=" + platformPayloadOverrides +
                '}';
    }

    public static class Builder {

        private final ImmutableMap.Builder<NotificationPayloadOverrideKey, DevicePayloadOverride> platformPayloadOverridesBuilder = ImmutableMap.builder();

        private String alert = null;

        private Builder() { }

        public Builder setAlert(String alert) {
            this.alert = alert;
            return this;
        }

        public <P extends DevicePayloadOverride> Builder addPlatformOverride(DeviceType deviceType, P payload) {
            this.platformPayloadOverridesBuilder.put(new NotificationPayloadOverrideKey(deviceType, payload.getClass()), payload);
            return this;
        }

        public Notification build() {
            ImmutableMap<NotificationPayloadOverrideKey, DevicePayloadOverride> overrides = platformPayloadOverridesBuilder.build();
            Preconditions.checkArgument(alert != null || !overrides.isEmpty(),
                    "Must either specify default notification keys or at least a single platform override");

            return new Notification(
                Optional.fromNullable(alert),
                overrides
            );
        }
    }
}
