package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(ItemController.class)
public class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Test that a valid item gets created successfully,
     * and a 201 Created response is returned.
     */
    @Test
    void shouldCreateItemWhenInputIsValid() throws Exception {
        Item item = new Item(null, "Valid Name", "desc", "ACTIVE", "valid@email.com");

        when(itemService.save(any(Item.class))).thenReturn(
                new Item(1L, item.getName(), item.getDescription(), item.getStatus(), item.getEmail())
        );

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(item)))
                .andExpect(status().isCreated());
    }

    /**
     * Test that invalid input (blank name and bad email)
     * results in a 400 Bad Request response.
     */
    @Test
    void shouldReturnBadRequestWhenInputIsInvalid() throws Exception {
        Item badItem = new Item(null, " ", "desc", "READY", "bad-email");

        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badItem)))
                .andExpect(status().isBadRequest());
    }

    /**
     * Test that a valid ID returns the corresponding item with 200 OK.
     */
    @Test
    void shouldReturnItemById() throws Exception {
        Item item = new Item(1L, "Test", "desc", "NEW", "x@y.com");
        when(itemService.findById(1L)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk());
    }

    /**
     * Test that a non-existent ID returns 404 Not Found.
     */
    @Test
    void shouldReturn404WhenItemNotFound() throws Exception {
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound());
    }

    /**
     * Test that calling /process returns a 200 OK
     * with the processed item list once async tasks are done.
     */
    @Test
    void shouldReturnProcessedItemsFromProcessEndpoint() throws Exception {
        Item processed1 = new Item(1L, "Done 1", "desc", "PROCESSED", "a@a.com");
        Item processed2 = new Item(2L, "Done 2", "desc", "PROCESSED", "b@b.com");

        when(itemService.processItemsAsync()).thenReturn(
                CompletableFuture.completedFuture(List.of(processed1, processed2))
        );

        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk());
    }
}
