package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.data.sql.tables.Users;
import it.fulminazzo.blocksmith.util.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.jooq.DSLContext;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;

public final class CustomSqlRepositoryExample {

    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (
                SqlDataSource dataSource = SqlDataSource.builder()
                        .username("sa")
                        .password("")
                        .database("test")
                        .h2()
                        .disk("example/build/resources/main/data/sql")
                        .allowSimultaneousFileConnections()
                        .build()
        ) {
            dataSource.executeScriptFromFile(new File("example/build/resources/main/schema.sql"));
            User first = new User(1L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            User second = new User(1L, "Camilla", "Drinkwater", "cami@fulminazzo.it", 20);
            UserRepository repository = dataSource.newRepository(UserRepository::new, executor);
            repository.save(first).get();
            repository.save(second).get();

            Optional<User> result = repository.findYoungestUser().get();
            TestUtils.assertEquals(result.isEmpty(), false, "Could not find youngest user");
            TestUtils.assertEquals(result.get(), second, "Youngest user was not youngest");

            repository.delete(first.getId()).get();
            repository.delete(second.getId()).get();
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
