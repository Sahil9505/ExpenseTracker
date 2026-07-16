package com.nova.finance.dashboard.web;

import com.nova.auth.security.NovaUserPrincipal;
import com.nova.common.api.ApiResponse;
import com.nova.finance.dashboard.DashboardService;
import com.nova.finance.dashboard.DashboardSummaryResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ApiResponse<DashboardSummaryResponse> summary(
            @AuthenticationPrincipal NovaUserPrincipal principal) {
        return ApiResponse.ok(dashboardService.summary(principal.getUserId()));
    }
}
