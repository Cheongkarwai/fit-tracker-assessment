package org.cheong.fittrackermiddleware.factory;

import org.cheong.fittrackermiddleware.properties.FitTrackerProperties;

public class FitTrackerPropertiesFactory {

    public static FitTrackerProperties createFitTrackerProperties(String baseUrl) {
        return new FitTrackerProperties(
                baseUrl,
                "test-client-id",
                "test-client-secret",
                "v1"
        );
    }
}
