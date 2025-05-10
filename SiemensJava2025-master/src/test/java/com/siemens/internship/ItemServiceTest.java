package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * Test that all items are processed correctly,
     * and their statuses are set to "PROCESSED".
     */
    @Test
    void shouldProcessItemsAndReturnOnlyValidOnes() throws Exception {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        Item item1 = new Item(1L, "Item 1", "desc", "PENDING", "a@b.com");
        Item item2 = new Item(2L, "Item 2", "desc", "PENDING", "b@c.com");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // Simulate that saving the item also sets it to "PROCESSED"
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setStatus("PROCESSED");
            return i;
        });

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get();

        // Assert
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(i -> "PROCESSED".equals(i.getStatus())));
    }

    /**
     * Test that if one item is missing (not found in DB),
     * only the valid item is processed and returned.
     */
    @Test
    void shouldSkipInvalidItemAndReturnOnlyFoundOnes() throws Exception {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        Item item1 = new Item(1L, "Item 1", "desc", "PENDING", "a@b.com");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.empty()); // Simulate missing item
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    /**
     * Test the basic service methods: findAll, findById, save, and delete.
     */
    @Test
    void shouldSupportBasicCrudOperations() {
        // Arrange
        Item item = new Item(1L, "CRUD", "desc", "NEW", "test@crud.com");

        when(itemRepository.findAll()).thenReturn(List.of(item));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(item)).thenReturn(item);

        // Act + Assert
        assertEquals(1, itemService.findAll().size());
        assertEquals(item, itemService.findById(1L).orElse(null));
        assertEquals(item, itemService.save(item));

        // Delete test
        itemService.deleteById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    /**
     * Test that when saving an item throws an exception,
     * the service skips it and continues with others.
     */
    @Test
    void shouldSkipItemWhenSaveFails() throws Exception {
        // Arrange
        List<Long> ids = List.of(1L, 2L);
        Item item1 = new Item(1L, "Item 1", "desc", "PENDING", "a@b.com");
        Item item2 = new Item(2L, "Item 2", "desc", "PENDING", "b@c.com");

        when(itemRepository.findAllIds()).thenReturn(ids);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));

        // Simulate success for item1
        when(itemRepository.save(argThat(i -> i != null && i.getId() != null && i.getId().equals(1L)))).thenAnswer(invocation -> {
            Item i = invocation.getArgument(0);
            i.setStatus("PROCESSED");
            return i;
        });

        // Simulate failure for item2
        when(itemRepository.save(argThat(i -> i.getId().equals(2L))))
                .thenThrow(new RuntimeException("Simulated DB error"));

        // Act
        CompletableFuture<List<Item>> future = itemService.processItemsAsync();
        List<Item> result = future.get();

        // Assert
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("PROCESSED", result.get(0).getStatus());
    }


}
