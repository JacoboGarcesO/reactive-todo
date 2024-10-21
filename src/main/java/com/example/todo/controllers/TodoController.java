package com.example.todo.controllers;

import com.example.todo.model.Todo;
import com.example.todo.services.TodoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1")
public class TodoController {
  private final TodoService todoService;

  public TodoController(TodoService todoService) {
    this.todoService = todoService;
  }

  @GetMapping(value = "/todos", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<Todo> getAllTasks() {
    return todoService.getAllTasks();
  }

  @GetMapping("/todos/{id}")
  public Mono<Todo> getTaskById(String id) {
    return todoService.getTaskById(id);
  }

  @PostMapping("/todos")
  @ResponseStatus(HttpStatus.CREATED)
  public Mono<Todo> createTask(@RequestBody Todo todo) {
    return todoService.createTask(todo);
  }

  @PutMapping("/todos/{id}")
  public Mono<ResponseEntity<Todo>> updateTask(@PathVariable String id, @RequestBody Todo task) {
    return todoService.updateTask(id, task)
            .map(updatedTask -> ResponseEntity.ok(updatedTask))
            .defaultIfEmpty(ResponseEntity.notFound().build());
  }

  @DeleteMapping("/todos/{id}")
  public Mono<ResponseEntity<Void>> deleteTask(@PathVariable String id) {
    return todoService.deleteTask(id)
            .then(Mono.just(ResponseEntity.noContent().build()));
  }
}
