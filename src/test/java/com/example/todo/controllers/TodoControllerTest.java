package com.example.todo.controllers;

import com.example.todo.model.Todo;
import com.example.todo.services.TodoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TodoControllerTest {
  private final WebTestClient webTestClient;
  private final TodoService todoService;

  public TodoControllerTest() {
    todoService = mock(TodoService.class);
    webTestClient = WebTestClient.bindToController(new TodoController(todoService)).build();
  }

  @Test
  @DisplayName("Get all todos")
  void getAllTasks() {
    when(todoService.getAllTasks()).thenReturn(getTodos());

    webTestClient.get().uri("/api/v1/todos")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBodyList(Todo.class)
            .hasSize(3)
            .value(todos -> {
              assertEquals("Todo 1", todos.get(0).getTitle());
              assertEquals("Todo 2", todos.get(1).getTitle());
              assertEquals("Todo 3", todos.get(2).getTitle());
            });

    Mockito.verify(todoService).getAllTasks();
  }

  @Test
  @DisplayName("Get a todo by id")
  void getTaskById() {
    Todo todo = new Todo("1", "Todo 1", "Description 1", false);
    when(todoService.getTaskById(anyString())).thenReturn(Mono.just(todo));

    webTestClient
            .get()
            .uri("/api/v1/todos/{id}", "1")
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Todo.class)
            .value(t -> {
              assertEquals(todo.getTitle(), t.getTitle());
              assertEquals(todo.getDescription(), t.getDescription());
              assertEquals(todo.isCompleted(), t.isCompleted());
            });

    Mockito.verify(todoService).getTaskById(anyString());
  }

  @Test
  @DisplayName("Get a todo by id when doesn't exist")
  void getTaskByIdNotFound() {
    when(todoService.getTaskById(anyString())).thenReturn(Mono.error(new RuntimeException("Not Found")));

    webTestClient
            .get()
            .uri("/api/v1/todos/{id}", "1")
            .exchange()
            .expectStatus().isNotFound();

    Mockito.verify(todoService).getTaskById(anyString());
  }

  @Test
  @DisplayName("Create a todo")
  void createTask() {
    Todo todo = new Todo("5", "Todo 5", "Description 5", false);
    when(todoService.createTask(any(Todo.class))).thenReturn(Mono.just(todo));

    webTestClient
            .post()
            .uri("/api/v1/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(todo)
            .exchange()
            .expectStatus().isCreated()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Todo.class)
            .value(t -> {
              assertEquals(todo.getTitle(), t.getTitle());
              assertEquals(todo.getDescription(), t.getDescription());
              assertEquals(todo.isCompleted(), t.isCompleted());
            });

    Mockito.verify(todoService).createTask(any(Todo.class));
  }

  @Test
  @DisplayName("Update a todo")
  void updateTask() {
    Todo todo = new Todo("1", "Todo 1", "Description 1", false);
    when(todoService.updateTask(anyString(), any(Todo.class))).thenReturn(Mono.just(todo));

    webTestClient
            .put()
            .uri("/api/v1/todos/{id}", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(todo)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(MediaType.APPLICATION_JSON)
            .expectBody(Todo.class)
            .value(t -> {
              assertEquals(todo.getTitle(), t.getTitle());
              assertEquals(todo.getDescription(), t.getDescription());
              assertEquals(todo.isCompleted(), t.isCompleted());
            });

    Mockito.verify(todoService).updateTask(anyString(), any(Todo.class));
  }

  @Test
  @DisplayName("Update a todo when doesn't exist")
  void updateTaskNotFound() {
    Todo todo = new Todo("1", "Todo 1", "Description 1", false);
    when(todoService.updateTask(anyString(), any(Todo.class))).thenReturn(Mono.empty());

    webTestClient
            .put()
            .uri("/api/v1/todos/{id}", "1")
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(todo)
            .exchange()
            .expectStatus().isNotFound();

    Mockito.verify(todoService).updateTask(anyString(), any(Todo.class));
  }

  @Test
  @DisplayName("Delete a todo")
  void deleteTask() {
    Todo todo = new Todo("5", "Todo 5", "Description 5", false);
    when(todoService.deleteTask(anyString())).thenReturn(Mono.just(todo));

    webTestClient
            .delete()
            .uri("/api/v1/todos/{id}", "1")
            .exchange()
            .expectStatus().isNoContent();

    Mockito.verify(todoService).deleteTask(anyString());
  }

  @Test
  @DisplayName("Delete a todo when doesn't exist")
  void deleteTodoNotFound() {
    when(todoService.deleteTask(anyString())).thenReturn(Mono.error(new RuntimeException("Task not found")));

    webTestClient
            .delete()
            .uri("/api/v1/todos/{id}", "1")
            .exchange()
            .expectStatus().isNotFound();

    Mockito.verify(todoService).deleteTask(anyString());
  }

  private Flux<Todo> getTodos() {
    return Flux.just(
            new Todo("1", "Todo 1", "Description 1", false),
            new Todo("2", "Todo 2", "Description 2", true),
            new Todo("3", "Todo 3", "Description 3", false)
    );
  }
}