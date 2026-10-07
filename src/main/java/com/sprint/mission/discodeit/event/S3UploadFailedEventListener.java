package com.sprint.mission.discodeit.event;

import com.sprint.mission.discodeit.entity.Role;
import com.sprint.mission.discodeit.entity.User;
import com.sprint.mission.discodeit.repository.UserRepository;
import com.sprint.mission.discodeit.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3UploadFailedEventListener {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @EventListener
    public void handle(S3UploadFailedEvent event) {
        List<User> admins =
                userRepository.findAllByRole(Role.ADMIN);

        String requestId =
                event.requestId() == null
                        ? "UNKNOWN"
                        : event.requestId();

        String title = "S3 파일 업로드 실패";

        String content = """
                Task: %s
                RequestId: %s
                BinaryContentId: %s
                Error: %s
                """.formatted(
                event.taskName(),
                requestId,
                event.binaryContentId(),
                event.errorMessage()
        );

        admins.forEach(admin ->
                notificationService.create(
                        admin.getId(),
                        title,
                        content
                )
        );

        log.error(
                "S3 파일 업로드 최종 실패. requestId={}, binaryContentId={}, error={}",
                requestId,
                event.binaryContentId(),
                event.errorMessage()
        );
    }
}