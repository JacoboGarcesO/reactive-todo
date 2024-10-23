package com.example.todo.services;

import com.example.todo.model.Todo;
import com.example.todo.repositories.TodoRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TodoServiceTest {
  private final TodoService todoService;
  private final TodoRepository todoRepository;

  public TodoServiceTest() {
    todoRepository = mock(TodoRepository.class);
    todoService = new TodoService(todoRepository);
  }

  @Test
  void getAllTodos() {
    // Arrange
    when(todoRepository.findAll()).thenReturn(getTodos());

    // Act
    Flux<Todo> todosService = todoService.getAllTasks();

    // Assert
    StepVerifier
            .create(todosService)
            .assertNext(todo -> assertEquals("Todo 1", todo.getTitle()))
            .assertNext(todo -> assertEquals("Todo 2", todo.getTitle()))
            .assertNext(todo -> assertEquals("Todo 3", todo.getTitle()))
            .verifyComplete();

    Mockito.verify(todoRepository).findAll();
  }

  @Test
  void getTodoById() {
    // Arrange
    Todo aux = new Todo("1", "Todo 1", "Description 1", false);
    when(todoRepository.findById(anyString())).thenReturn(Mono.just(aux));

    // Act
    Mono<Todo> todo = todoService.getTaskById("1");

    // Assert
    StepVerifier
            .create(todo)
            .assertNext(t -> assertEquals(aux.getTitle(), t.getTitle()))
            .verifyComplete();

    Mockito.verify(todoRepository).findById(anyString());
  }

  @Test
  void getTodoByIdNoFound() {
    // Arrange
    when(todoRepository.findById(anyString())).thenReturn(Mono.empty());

    // Act
    Mono<Todo> todo = todoService.getTaskById("4");

    // Assert
    StepVerifier
            .create(todo)
            .expectNextCount(0)
            .verifyComplete();

    Mockito.verify(todoRepository).findById(anyString());
  }

  @Test
  void createTodo() {
    // Arrange
    Todo todo = new Todo("5", "Todo 5", "Description 5", false);
    when(todoRepository.save(any(Todo.class))).thenReturn(Mono.just(todo));

    // Act
    Mono<Todo> todo1 = todoService.createTask(todo);

    // Assert
    StepVerifier
            .create(todo1)
            .assertNext(t -> assertEquals(todo.getTitle(), t.getTitle()))
            .verifyComplete();

    Mockito.verify(todoRepository).save(any(Todo.class));
  }

  @Test
  void deleteTodoById() {
    // Arrange
    when(todoRepository.deleteById(anyString())).thenReturn(Mono.empty());

    // Act
    Mono<Void> voidMono = todoService.deleteTask("1");

    // Assert
    StepVerifier
            .create(voidMono)
            .verifyComplete();

    Mockito.verify(todoRepository).deleteById(anyString());
  }

  @Test
  void deleteTodoByIdNoFound() {
    // Arrange
    when(todoRepository.deleteById(anyString())).thenReturn(Mono.error(new RuntimeException()));

    // Act
    Mono<Void> voidMono = todoService.deleteTask("4");

    // Assert
    StepVerifier
            .create(voidMono)
            .expectError(RuntimeException.class)
            .verify();

    Mockito.verify(todoRepository).deleteById("4");
  }

  @Test
  void updateTodo() {
    // Arrange
    Todo todo = new Todo("1", "Todo 1", "Description 1", false);
    Todo newTodo = new Todo("1", "Todo 2", "Description 2", true);
    when(todoRepository.findById(anyString())).thenReturn(Mono.just(todo));
    when(todoRepository.save(any(Todo.class))).thenReturn(Mono.just(newTodo));

    // Act
    Mono<Todo> todoMono = todoService.updateTask("1", newTodo);

    // Assert
    StepVerifier
            .create(todoMono)
            .assertNext(t -> {
              assertEquals(newTodo.getTitle(), t.getTitle());
              assertEquals(newTodo.getDescription(), t.getDescription());
              assertEquals(newTodo.isCompleted(), t.isCompleted());
            })
            .verifyComplete();

    Mockito.verify(todoRepository).findById(anyString());
    Mockito.verify(todoRepository).save(any(Todo.class));
  }

  @Test
  void updateTodoNoFound() {
    // Arrange
    Todo newTodo = new Todo("4", "Todo 4", "Description 4", true);
    when(todoRepository.findById(anyString())).thenReturn(Mono.empty());

    // Act
    Mono<Todo> todoMono = todoService.updateTask("4", newTodo);

    // Assert
    StepVerifier
            .create(todoMono)
            .expectNextCount(0)
            .verifyComplete();

    Mockito.verify(todoRepository).findById(anyString());
  }

  private Flux<Todo> getTodos() {
    return Flux.just(
            new Todo("1", "Todo 1", "Description 1", false),
            new Todo("2", "Todo 2", "Description 2", true),
            new Todo("3", "Todo 3", "Description 3", false)
    );
  }
}