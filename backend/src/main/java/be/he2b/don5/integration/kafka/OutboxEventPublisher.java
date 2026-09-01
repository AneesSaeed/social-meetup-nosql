package be.he2b.don5.integration.kafka;

import be.he2b.don5.integration.outbox.OutboxEvent;
import be.he2b.don5.integration.outbox.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisher {

    private final OutboxEventRepository outboxRepo;
    private final OutboxEventProcessor eventProcessor;

    @Scheduled(fixedDelay = 5000)
    @SchedulerLock(
        name = "OutboxEventPublisher_publishPendingEvents", 
        lockAtLeastFor = "2s", 
        lockAtMostFor = "4s"
    )
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents = outboxRepo.findByProcessedFalseOrderByCreatedAtAsc();

        if (pendingEvents.isEmpty()) {
            return;
        }

        for (OutboxEvent event : pendingEvents) {
            eventProcessor.processEvent(event);
        }
    }
}