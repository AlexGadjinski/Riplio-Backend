package app.common.scheduler;

import app.post.service.PostService;
import app.report.client.ModerationClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;

@Component
@RequiredArgsConstructor
@Slf4j
public class RiplioScheduler {

    private final PostService postService;
    private final ModerationClient moderationClient;

    @Scheduled(cron = "0 */10 * * * *")
    public void refreshTrendingPosts() {
        postService.evictTrendingPosts();
        postService.getTrendingPosts();

        log.info("Trending posts cache refreshed via scheduled cron job.");
    }

    @Scheduled(fixedRate = 60000)
    public void checkModerationServiceHealth() {
        try {
            moderationClient.checkHealth();
            log.info("Moderation service health check: UP.");
        } catch (RestClientException e) {
            log.warn("Moderation service health check: DOWN — {}", e.getMessage());
        }
    }
}
