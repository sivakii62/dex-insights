package com.dex.insights.insights;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/insights")
@Tag(name = "Insights", description = "Derived operational views over the fleet")
public class InsightsController {

    private final InsightsService insightsService;

    public InsightsController(InsightsService insightsService) {
        this.insightsService = insightsService;
    }

    @GetMapping("/overview")
    @Operation(summary = "Operational overview",
            description = "Fleet availability roll-up, stores ranked by offline pumps, "
                    + "tanks at runout risk, and incident counts by severity, status and category.")
    public InsightsOverview overview() {
        return insightsService.overview();
    }
}
