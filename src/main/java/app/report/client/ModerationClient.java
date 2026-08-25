package app.report.client;

import app.common.dto.PagedResponse;
import app.report.client.dto.CreateReportRequest;
import app.report.client.dto.ReportResponse;
import app.report.client.dto.UpdateReportRequest;
import app.report.model.ReportStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.UUID;

@HttpExchange("/api/v1/reports")
public interface ModerationClient {

    @GetExchange("/health")
    void checkHealth();

    @PostExchange
    void createReport(@RequestBody CreateReportRequest request);

    @PutExchange("/{id}")
    ReportResponse updateReport(@PathVariable UUID id, @RequestBody UpdateReportRequest request);

    @GetExchange
    PagedResponse<ReportResponse> getReportsByCommunity(@RequestParam UUID communityId,
                                                        @RequestParam(required = false) ReportStatus status,
                                                        @RequestParam int page,
                                                        @RequestParam int size);
}
