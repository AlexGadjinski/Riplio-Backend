package app.report.client;

import app.report.client.dto.CreateReportRequest;
import app.report.client.dto.ReportResponse;
import app.report.client.dto.UpdateReportRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.UUID;

@HttpExchange("/api/v1/reports")
public interface ModerationClient {

    @PostExchange
    ReportResponse createReport(@RequestBody CreateReportRequest request);

    @PutExchange("/{id}")
    ReportResponse updateReport(@PathVariable UUID id, @RequestBody UpdateReportRequest request);

    @GetExchange
    List<ReportResponse> getReportsByCommunity(@RequestParam UUID communityId, @RequestParam(required = false) String status);
}
