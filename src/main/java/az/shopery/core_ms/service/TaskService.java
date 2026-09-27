package az.shopery.core_ms.service;

import az.shopery.core_ms.model.event.TaskEvent;

public interface TaskService {
    void createTask(TaskEvent taskEvent);
}
