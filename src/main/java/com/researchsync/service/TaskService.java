package com.researchsync.service;

import com.researchsync.model.Task;
import com.researchsync.model.User;
import com.researchsync.model.Workspace;
import com.researchsync.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private WorkspaceService workspaceService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    public Task createTask(Task task, User creator) {
        try {
            task.setCreatedBy(creator);
            task.setCreatedDate(LocalDateTime.now());
            task.setUpdatedDate(LocalDateTime.now());

            if (task.getStatus() == null) {
                task.setStatus(Task.TaskStatus.PENDING);
            }

            Task savedTask = taskRepository.save(task);

            if (savedTask.getAssignedTo() != null) {
                emailService.sendTaskAssignmentNotification(
                        savedTask.getAssignedTo(),
                        savedTask.getTitle(),
                        savedTask.getWorkspace().getName()
                );
            }

            return savedTask;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create task: " + e.getMessage());
        }
    }

    public Task findById(Long taskId) {
        try {
            return taskRepository.findById(taskId)
                    .orElseThrow(() -> new RuntimeException("Task not found with ID: " + taskId));
        } catch (Exception e) {
            throw new RuntimeException("Failed to find task: " + e.getMessage());
        }
    }

    public List<Task> getTasksByWorkspace(Long workspaceId) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.findByWorkspace(workspace);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks for workspace: " + e.getMessage());
        }
    }

    public List<Task> getTasksByUser(User user) {
        try {
            return taskRepository.findByAssignedTo(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks for user: " + e.getMessage());
        }
    }

    public List<Task> getTasksByCreator(User creator) {
        try {
            return taskRepository.findByCreatedBy(creator);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by creator: " + e.getMessage());
        }
    }

    public List<Task> getActiveTasksByUser(User user) {
        try {
            return taskRepository.findActiveTasksByUser(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get active tasks for user: " + e.getMessage());
        }
    }

    public List<Task> getTasksByStatus(Task.TaskStatus status) {
        try {
            return taskRepository.findByStatus(status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by status: " + e.getMessage());
        }
    }

    public List<Task> getTasksByWorkspaceAndStatus(Long workspaceId, Task.TaskStatus status) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.findByWorkspaceAndStatus(workspace, status);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by workspace and status: " + e.getMessage());
        }
    }

    public List<Task> getTasksByWorkspaceAndUser(Long workspaceId, User user) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.findByWorkspaceAndAssignedTo(workspace, user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks by workspace and user: " + e.getMessage());
        }
    }

    public Task assignTask(Long taskId, Long userId, User assigner) {
        try {
            Task task = findById(taskId);
            User assignee = userService.findById(userId);

            if (!workspaceService.isUserAdminOfWorkspace(assigner, task.getWorkspace().getWorkspaceId()) &&
                    !task.getCreatedBy().getUserId().equals(assigner.getUserId())) {
                throw new RuntimeException("You don't have permission to assign this task");
            }

            task.setAssignedTo(assignee);
            task.setUpdatedDate(LocalDateTime.now());
            Task savedTask = taskRepository.save(task);

            emailService.sendTaskAssignmentNotification(
                    assignee,
                    task.getTitle(),
                    task.getWorkspace().getName()
            );

            return savedTask;
        } catch (Exception e) {
            throw new RuntimeException("Failed to assign task: " + e.getMessage());
        }
    }

    public Task updateTaskStatus(Long taskId, Task.TaskStatus status, User updater) {
        try {
            Task task = findById(taskId);

            if (!workspaceService.isUserMemberOfWorkspace(updater, task.getWorkspace().getWorkspaceId())) {
                throw new RuntimeException("You don't have permission to update this task");
            }

            task.setStatus(status);
            task.setUpdatedDate(LocalDateTime.now());

            if (status == Task.TaskStatus.COMPLETED) {
                task.setCompletedDate(LocalDateTime.now());
            }

            return taskRepository.save(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task status: " + e.getMessage());
        }
    }

    public Task updateTask(Task task, User updater) {
        try {
            Task existingTask = findById(task.getTaskId());

            if (!workspaceService.isUserAdminOfWorkspace(updater, existingTask.getWorkspace().getWorkspaceId()) &&
                    !existingTask.getCreatedBy().getUserId().equals(updater.getUserId()) &&
                    (existingTask.getAssignedTo() == null || !existingTask.getAssignedTo().getUserId().equals(updater.getUserId()))) {
                throw new RuntimeException("You don't have permission to update this task");
            }

            existingTask.setTitle(task.getTitle());
            existingTask.setDescription(task.getDescription());
            existingTask.setPriority(task.getPriority());
            existingTask.setDueDate(task.getDueDate());
            existingTask.setUpdatedDate(LocalDateTime.now());

            return taskRepository.save(existingTask);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update task: " + e.getMessage());
        }
    }

    public void deleteTask(Long taskId, User deleter) {
        try {
            Task task = findById(taskId);

            if (!workspaceService.isUserAdminOfWorkspace(deleter, task.getWorkspace().getWorkspaceId()) &&
                    !task.getCreatedBy().getUserId().equals(deleter.getUserId())) {
                throw new RuntimeException("You don't have permission to delete this task");
            }

            taskRepository.delete(task);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete task: " + e.getMessage());
        }
    }

    public List<Task> getOverdueTasks() {
        try {
            return taskRepository.findOverdueTasks(LocalDateTime.now());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get overdue tasks: " + e.getMessage());
        }
    }

    public List<Task> getTasksDueSoon(User user) {
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dueSoon = now.plusDays(1);
            return taskRepository.findTasksDueSoonForUser(user, now, dueSoon);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get tasks due soon: " + e.getMessage());
        }
    }

    public List<Task> searchTasks(String searchTerm) {
        try {
            return taskRepository.findByTitleOrDescriptionContaining(searchTerm);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search tasks: " + e.getMessage());
        }
    }

    public List<Task> searchTasksInWorkspace(Long workspaceId, String searchTerm) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.findByWorkspaceAndTitleOrDescriptionContaining(workspace, searchTerm);
        } catch (Exception e) {
            throw new RuntimeException("Failed to search tasks in workspace: " + e.getMessage());
        }
    }

    public Long getTaskCountByWorkspace(Long workspaceId) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.countByWorkspace(workspace);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get task count: " + e.getMessage());
        }
    }

    public Long getCompletedTaskCountByWorkspace(Long workspaceId) {
        try {
            Workspace workspace = workspaceService.findById(workspaceId);
            return taskRepository.countByWorkspaceAndStatus(workspace, Task.TaskStatus.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get completed task count: " + e.getMessage());
        }
    }

    public Long getCompletedTaskCountByUser(User user) {
        try {
            return taskRepository.countByAssignedToAndStatus(user, Task.TaskStatus.COMPLETED);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get completed task count for user: " + e.getMessage());
        }
    }

    public void sendDeadlineReminders() {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            LocalDateTime dayAfterTomorrow = LocalDateTime.now().plusDays(2);

            List<Task> tasksDueSoon = taskRepository.findTasksByDueDateRange(LocalDateTime.now(), tomorrow);

            for (Task task : tasksDueSoon) {
                if (task.getAssignedTo() != null && task.getStatus() != Task.TaskStatus.COMPLETED) {
                    emailService.sendDeadlineAlert(
                            task.getAssignedTo(),
                            task.getTitle(),
                            task.getWorkspace().getName()
                    );
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to send deadline reminders: " + e.getMessage());
        }
    }
}
