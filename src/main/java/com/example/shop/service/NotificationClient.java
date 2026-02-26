package com.example.shop.service;

import com.example.shop.model.Order;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.springframework.stereotype.Component;

@Component
public class NotificationClient {
  private final HttpClient httpClient = HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(2))
          .build();
  private final ExecutorService executor = Executors.newSingleThreadExecutor();

  public CompletableFuture<Void> notifyAsync(Order order) {
    if (order.getNotifyUrl() == null || order.getNotifyUrl().isBlank()) {
      return CompletableFuture.completedFuture(null);
    }
    return CompletableFuture.runAsync(() -> {
      try {
        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(order.getNotifyUrl()))
                .timeout(Duration.ofSeconds(3))
                .POST(HttpRequest.BodyPublishers.ofString("orderId=" + order.getId()))
                .build();
        httpClient.send(req, HttpResponse.BodyHandlers.discarding());
      } catch (Exception ignored) {
      }
    }, executor);
  }
}