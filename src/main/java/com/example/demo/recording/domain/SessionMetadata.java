package com.example.demo.recording.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
public class SessionMetadata {
    private String cameraModel;
    private String cameraSettings;
    private String lightingConditions;
    private String environmentNotes;
    private String deviceId;
    private String appVersion;
    private Map<String, Object> customFields;

    public static SessionMetadata fromMap(Map<String, Object> metadata) {
        if (metadata == null) {
            return SessionMetadata.builder().build();
        }

        return SessionMetadata.builder()
            .cameraModel((String) metadata.get("cameraModel"))
            .cameraSettings((String) metadata.get("cameraSettings"))
            .lightingConditions((String) metadata.get("lightingConditions"))
            .environmentNotes((String) metadata.get("environmentNotes"))
            .deviceId((String) metadata.get("deviceId"))
            .appVersion((String) metadata.get("appVersion"))
            .customFields(metadata)
            .build();
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new java.util.HashMap<>(customFields != null ? customFields : new java.util.HashMap<>());

        if (cameraModel != null) map.put("cameraModel", cameraModel);
        if (cameraSettings != null) map.put("cameraSettings", cameraSettings);
        if (lightingConditions != null) map.put("lightingConditions", lightingConditions);
        if (environmentNotes != null) map.put("environmentNotes", environmentNotes);
        if (deviceId != null) map.put("deviceId", deviceId);
        if (appVersion != null) map.put("appVersion", appVersion);

        return map;
    }
}