package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;
    private static ExecutorService executor = Executors.newFixedThreadPool(10);

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }


    /**
     * Your Tasks
     * Identify all concurrency and asynchronous programming issues in the code
     * Fix the implementation to ensure:
     * All items are properly processed before the CompletableFuture completes
     * Thread safety for all shared state
     * Proper error handling and propagation
     * Efficient use of system resources
     * Correct use of Spring's @Async annotation
     * Add appropriate comments explaining your changes and why they fix the issues
     * Write a brief explanation of what was wrong with the original implementation
     * <p>
     * Hints
     * Consider how CompletableFuture composition can help coordinate multiple async operations
     * Think about appropriate thread-safe collections
     * Examine how errors are handled and propagated
     * Consider the interaction between Spring's @Async and CompletableFuture
     */

    /** Modified processItemsAsync
     * Asynchronously processes all items by:
     * -fetching their IDs from the database
     * -simulating a delay
     * -setting their status to "PROCESSED"
     * -updating them in the repository
     * <p>
     * Each item is processed in parallel using CompletableFutures (as suggested)
     * Any errors are caught and logged, and only successfully processed items
     * are returned once all tasks are complete
     *
     * @return a CompletableFuture containing a list of successfully processed items
     * */
    @Async
    public CompletableFuture<List<Item>> processItemsAsync() {

        List<Long> itemIds = itemRepository.findAllIds();
        List<CompletableFuture<Item>> futures = new ArrayList<>();

        // Launch async tasks to process each item ID in parallel
        for (Long id : itemIds) {
            CompletableFuture<Item> future = CompletableFuture.supplyAsync(()->{
                try{
                    Thread.sleep(100);

                    // Retrieve the item; skip if it doesn't exist
                    Optional<Item> optionalItem = itemRepository.findById(id);
                    if(optionalItem.isPresent()) {
                        Item item = optionalItem.get();
                        item.setStatus("Processed");
                        itemRepository.save(item);
                        return item;
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    System.out.println("Interrupted"+e.getMessage());
                } catch (Exception e){
                    System.err.println("Error processing item "+id+": "+e.getMessage());
                }
                return null; // On failure or missing item
            },executor);

            futures.add(future);

        }

        // Wait for all futures to complete and collect non-null (successful) results
        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(voidResult -> {
                    List<Item> processed = new ArrayList<>();
                    for (CompletableFuture<Item> future : futures) {
                        try{
                            Item item = future.get();
                            if(item!=null){
                                processed.add(item);
                            }
                        } catch (Exception e){
                            System.err.println("Failed to retrieve processed item: "+e.getMessage());
                        }
                    }
                    return processed;
                });
    }

}

