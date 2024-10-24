package com.example.todo.services;

import com.example.todo.model.Todo;
import com.example.todo.repositories.TodoRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class TodoService {
  private final TodoRepository todoRepository;

  public TodoService(TodoRepository todoRepository) {
    this.todoRepository = todoRepository;
  }

  public Flux<Todo> getAllTasks() {
    return todoRepository.findAll();
  }

  public Mono<Todo> getTaskById(String id) {
    return todoRepository.findById(id).switchIfEmpty(Mono.error(new RuntimeException("Task not found")));
  }

  public Mono<Todo> createTask(Todo todo) {
    return todoRepository.save(todo);
  }

  public Mono<Todo> updateTask(String id, Todo todo) {
    return todoRepository.findById(id)
            .flatMap(existingTask -> {
              existingTask.setTitle(todo.getTitle());
              existingTask.setDescription(todo.getDescription());
              existingTask.setCompleted(todo.isCompleted());
              return todoRepository.save(existingTask);
            });
  }
  
  public Mono<Todo> deleteTask(String id) {
    return todoRepository.findById(id)
            .flatMap(todo -> todoRepository.deleteById(id).then(Mono.just(todo)))
            .switchIfEmpty(Mono.error(new RuntimeException("Task not found")));
  }
}
