package com.dex.insights.insights;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Thresholds behind the overview insights. Operations teams tune these per fleet, so they are
 * configuration rather than constants.
 *
 * @param lowTankThreshold fill ratio at or below which a tank is flagged as a runout risk
 * @param topN             number of entries returned in each ranked list
 */
@ConfigurationProperties(prefix = "dex.insights")
public record InsightsProperties(double lowTankThreshold, int topN) {

    public InsightsProperties {
        lowTankThreshold = lowTankThreshold <= 0 ? 0.25d : lowTankThreshold;
        topN = topN <= 0 ? 5 : topN;
    }
}
