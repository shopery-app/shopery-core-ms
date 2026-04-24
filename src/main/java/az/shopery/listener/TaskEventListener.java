package az.shopery.listener;

import az.shopery.model.event.TaskEvent;
import az.shopery.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskEventListener {

    private final TaskService taskService;

    @Async("taskExecutor")
    @EventListener
    public void onTask(TaskEvent taskEvent) {
        taskService.createTask(taskEvent);
    }
}
