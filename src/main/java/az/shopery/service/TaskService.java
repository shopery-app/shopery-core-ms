package az.shopery.service;

import az.shopery.model.event.TaskEvent;

public interface TaskService {
    void createTask(TaskEvent taskEvent);
}
