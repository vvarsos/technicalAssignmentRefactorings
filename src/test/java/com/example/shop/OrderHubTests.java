package com.example.shop;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.shop.store.InMemoryStore;
import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class OrderHubTests {
  @Autowired
  private MockMvc mvc;

  @BeforeEach
  void resetStore() throws Exception {
    InMemoryStore store = InMemoryStore.getInstance();
    store.getUsersUnsafe().clear();
    store.getOrdersUnsafe().clear();

    Field byUserField = InMemoryStore.class.getDeclaredField("ordersByUser");
    byUserField.setAccessible(true);
    ((Map<?, ?>) byUserField.get(store)).clear();

    Field seqField = InMemoryStore.class.getDeclaredField("orderSeq");
    seqField.setAccessible(true);
    AtomicLong seq = (AtomicLong) seqField.get(store);
    seq.set(1000L);
  }

  @Test
  void createsUserAndGetsUser() throws Exception {
    String userJson = "{\"email\":\"a@example.com\",\"name\":\"A\"}";
    mvc.perform(post("/users").contentType(MediaType.APPLICATION_JSON).content(userJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("a@example.com"))
        .andExpect(jsonPath("$.email").value("a@example.com"))
        .andExpect(jsonPath("$.name").value("A"));

    mvc.perform(get("/users/{id}", "a@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("a@example.com"));
  }

  @Test
  void upsertsUserWhenEmailAlreadyExists() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@example.com\",\"name\":\"A\"}"))
        .andExpect(status().isOk());

    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"A@EXAMPLE.COM\",\"name\":\"Updated\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value("a@example.com"))
        .andExpect(jsonPath("$.name").value("Updated"))
        .andExpect(jsonPath("$.lastSeen").exists());
  }

  @Test
  void rejectsInvalidUserPayload() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"bad-email\",\"name\":\"\"}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsNotFoundForMissingUser() throws Exception {
    mvc.perform(get("/users/{id}", "missing@example.com"))
        .andExpect(status().isNotFound());
  }

  @Test
  void createsOrderAndGetsItById() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@example.com\",\"name\":\"A\"}"))
        .andExpect(status().isOk());

    String orderJson = "{\"userId\":\"a@example.com\",\"items\":[{\"sku\":\"sku-1\",\"quantity\":2,\"unitPrice\":12.5}],\"notifyUrl\":\"\"}";
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1001))
        .andExpect(jsonPath("$.total").value(25.0))
        .andExpect(jsonPath("$.status").value("NEW"))
        .andExpect(jsonPath("$.notifyUrl").value(""));

    mvc.perform(get("/orders/{id}", 1001))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1001));
  }

  @Test
  void returnsAcceptedBodyWhenOrderUserIsUnknown() throws Exception {
    String orderJson = "{\"userId\":\"missing-user\",\"items\":[{\"sku\":\"sku-1\",\"quantity\":1,\"unitPrice\":12.5}]}";
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("accepted"))
        .andExpect(jsonPath("$.orderId").isEmpty());
  }

  @Test
  void rejectsInvalidOrderPayload() throws Exception {
    mvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"\",\"items\":[{\"sku\":\"\",\"quantity\":0,\"unitPrice\":-1.0}] }"))
        .andExpect(status().isBadRequest());
  }

  @Test
  void returnsNotFoundForMissingOrder() throws Exception {
    mvc.perform(get("/orders/{id}", 9999))
        .andExpect(status().isNotFound());
  }

  @Test
  void listsOrdersForUserAndNormalizesDuplicateItems() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@example.com\",\"name\":\"A\"}"))
        .andExpect(status().isOk());

    String orderJson = "{\"userId\":\"a@example.com\",\"items\":[{\"sku\":\"sku-b\",\"quantity\":1,\"unitPrice\":9.0},{\"sku\":\"sku-a\",\"quantity\":2,\"unitPrice\":5.0},{\"sku\":\"sku-a\",\"quantity\":3,\"unitPrice\":5.0}]}";
    mvc.perform(post("/orders").contentType(MediaType.APPLICATION_JSON).content(orderJson))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.total").value(34.0));

    mvc.perform(get("/orders").param("userId", "a@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[0].items", hasSize(2)))
        .andExpect(jsonPath("$[0].items[0].sku").value("sku-a"))
        .andExpect(jsonPath("$[0].items[0].quantity").value(5))
        .andExpect(jsonPath("$[0].items[1].sku").value("sku-b"));
  }

  @Test
  void returnsEmptyOrderListForUserWithNoOrders() throws Exception {
    mvc.perform(get("/orders").param("userId", "nobody"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$", hasSize(0)));
  }

  @Test
  void updatesUserLastSeenForHighValueOrder() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@example.com\",\"name\":\"A\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastSeen").doesNotExist());

    mvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"a@example.com\",\"items\":[{\"sku\":\"sku-1\",\"quantity\":1,\"unitPrice\":1200.0}]}"))
        .andExpect(status().isOk());

    mvc.perform(get("/users/{id}", "a@example.com"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.lastSeen").exists());
  }

  @Test
  void readsStatsForEmptyStore() throws Exception {
    mvc.perform(get("/orders/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.users").value(0))
        .andExpect(jsonPath("$.orders").value(0))
        .andExpect(jsonPath("$.latestOrderId").value(0));
  }

  @Test
  void readsStatsWithExistingData() throws Exception {
    mvc.perform(post("/users")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"email\":\"a@example.com\",\"name\":\"A\"}"))
        .andExpect(status().isOk());

    mvc.perform(post("/orders")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"userId\":\"a@example.com\",\"items\":[{\"sku\":\"sku-1\",\"quantity\":1,\"unitPrice\":10.0}]}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(1001));

    mvc.perform(get("/orders/stats"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.users").value(1))
        .andExpect(jsonPath("$.orders").value(1))
        .andExpect(jsonPath("$.latestOrderId").value(1001));
  }
}
