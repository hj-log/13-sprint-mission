package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Message;
import com.sprint.mission.discodeit.entity.ReadStatus;
import com.sprint.mission.discodeit.exception.message.MessageNotFoundException;
import com.sprint.mission.discodeit.repository.MessageRepository;
import com.sprint.mission.discodeit.repository.ReadStatusRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@RequiredArgsConstructor
public class NotificationRequiredEventListener {

    private final MessageRepository messageRepository;
    private final ReadStatusRepository readStatusRepository;
    private final NotificationService notificationService;

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async("taskExecutor")
    public void on(MessageCreatedEvent event) {
        Message message = messageRepository.findById(event.messageId())
                .orElseThrow(()-> new MessageNotFoundException(event.messageId()));

        List<ReadStatus> receivers =
                readStatusRepository
                        .findAllByChannelIdAndNotificationEnabledTrue(
                                message.getChannel().getId()
                        );

        String title = message.getAuthor().getUsername()
                + " (#"
                + message.getChannel().getName()
                + ")";

        receivers.stream()
                .filter(readStatus -> !readStatus.getUser().getId()
                        .equals(message.getAuthor().getId())
                )
                .forEach(readStatus ->
                        notificationService.create(readStatus.getUser().getId(),
                                title,
                                message.getContent()
                        )
                );
    }

    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT
    )
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Async("taskExecutor")
    public void on(RoleUpdatedEvent event) {
        String title = "권한이 변경되었습니다.";

        String content = event.previousRole().name() + " -> " + event.newRole().name();

        notificationService.create(event.userId(), title, content);
    }




}
