# Refactor Notes

- Converted `OrderController` and `UserController` to use constructor injection instead of `new Service()` so Spring manages the full dependency graph and services are mockable in tests.
- Annotated `OrderService`, `UserService`, and `NotificationClient` with `@Service` / `@Component` so they are proper Spring beans rather than manually instantiated objects.
- Removed the `auditSnapshot()` method from `OrderService` because it serialised store state to a string and immediately discarded the result, making it pure dead code with a silent `ObjectMapper` dependency.
- Removed the no-op block `if (u != null) { o.setStatus(o.getStatus()); }` from `listOrdersForUser` because it fetched a user and assigned a field to its own value, providing no behaviour.
- Added `synchronized` to all read methods on `InMemoryStore` (`getUser`, `getOrder`, `getOrdersForUser`, `userCount`, `orderCount`) to make the store's access pattern consistently thread-safe alongside its existing synchronised writes.
- Extracted `calculateTotal()` and `calculateStats()` in `OrderService` to adhere to the Single Responsinility Principle.
- Made `saveUser` and `saveOrder` in `InMemoryStore` return `void` since their return values were never used by any caller.
