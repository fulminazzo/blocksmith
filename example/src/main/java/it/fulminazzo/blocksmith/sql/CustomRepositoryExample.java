package it.fulminazzo.blocksmith.sql;

import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.data.sql.SqlRepository;
import it.fulminazzo.blocksmith.sql.tables.Users;
import it.fulminazzo.blocksmith.util.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class CustomRepositoryExample {

    public static void main(String[] args) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (
                SqlDataSource dataSource = SqlDataSource.builder()
                        .username("sa")
                        .password("")
                        .h2()
                        .disk("./example/build/h2db/test")
                        .allowSimultaneousFileConnections()
                        .build()
        ) {
            User first = new User(1L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            User second = new User(1L, "Camilla", "Drinkwater", "cami@fulminazzo.it", 20);
            UserRepository repository = dataSource.newRepository(UserRepository::new, executor);
            repository.save(first).join();
            repository.save(second).join();

            Optional<User> result = repository.findYoungestUser().join();
            TestUtils.assertEquals(result.isEmpty(), false, "Could not find youngest user");
            TestUtils.assertEquals(result.get(), second, "Youngest user was not youngest");

            repository.delete(first.getId()).join();
            repository.delete(second.getId()).join();
        } finally {
            executor.shutdown();
        }
    }

    public static final class UserRepository extends SqlRepository<User, Long, Users> {

        public UserRepository(final @NotNull DSLContext context,
                              final @NotNull Executor executor) {
            super(
                    context,
                    Tables.USERS,
                    Tables.USERS.ID,
                    User.class,
                    executor
            );
        }

        public @NotNull CompletableFuture<Optional<User>> findYoungestUser() {
            return query(dsl ->
                    dsl.selectFrom(table)
                            .orderBy(table.AGE.asc())
                            .limit(1)
                            .fetchOptionalInto(User.class)
            );
        }

    }

}
