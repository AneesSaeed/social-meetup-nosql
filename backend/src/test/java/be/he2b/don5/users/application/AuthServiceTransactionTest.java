package be.he2b.don5.users.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import be.he2b.don5.integration.outbox.OutboxWriter;
import be.he2b.don5.users.api.dto.RegisterRequest;
import be.he2b.don5.users.domain.User;
import be.he2b.don5.users.infrastructure.mongo.UserRepository;

@SpringBootTest
class AuthServiceTransactionTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepo;

    @MockitoBean
    private OutboxWriter outboxWriter;

    @Test
    void register_shouldRollbackUser_whenOutboxFails() {
        // Given
        RegisterRequest request = new RegisterRequest(
            "alice for transaction test", "alice-for-transaction-test@gmail.com", "Bio", List.of()
        );

        // Force OutboxWriter to throw an exception when called inside the transaction
        doThrow(new RuntimeException("Outbox service failure"))
            .when(outboxWriter)
            .addEvent(any(), any(), any(), any());

        // When
        assertThatThrownBy(() -> authService.register(request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Outbox service failure");

        // Then: Verify user was not saved to MongoDB due to rollback
        Optional<User> savedUser = userRepo.findByEmail("alice-for-transaction-test@gmail.com");
        assertThat(savedUser).isEmpty();
    }
}