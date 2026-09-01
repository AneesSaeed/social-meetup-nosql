package be.he2b.don5.integration.outbox;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@ActiveProfiles("test")
class OutboxEventRepositoryTest {

    @Autowired
    private OutboxEventRepository outboxRepo;

    @AfterEach
    void tearDown() {
        outboxRepo.deleteAll();
    }

    @Test
    void findByProcessedFalseOrderByCreatedAtAsc_shouldLimitToBatchSizeAndMaintainOrder() {
        LocalDateTime baseTime = LocalDateTime.now();

        // Save 150 events with strictly ascending timestamps
        for (int i = 1; i <= 150; i++) {
            OutboxEvent event = new OutboxEvent(
                "agg-" + i,
                "TEST_TYPE",
                "TEST_EVENT",
                "{\"key\":\"value\"}"
            );
            event.setCreatedAt(baseTime.plusSeconds(i));
            outboxRepo.save(event);
        }

        List<OutboxEvent> batch = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, 100));

        // Verify limit of 100 items and FIFO order (agg-1 oldest, agg-100 newest in batch)
        assertThat(batch).hasSize(100);
        assertThat(batch.get(0).getAggregateId()).isEqualTo("agg-1");
        assertThat(batch.get(99).getAggregateId()).isEqualTo("agg-100");
    }
}