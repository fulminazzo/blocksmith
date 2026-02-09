package it.fulminazzo.blocksmith.sql;

import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SqlExample {

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
            Repository<User, Long> repository = dataSource.newRepository(
                    User.class,
                    Tables.USERS,
                    Tables.USERS.ID,
                    executor
            );
            User user = new User(1337L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            assertEquals(repository.existsById(user.getId()).join(), false, "User should not exist at start");
            assertEquals(repository.save(user).join(), user, "Saved user should be equal to current");
            assertEquals(repository.existsById(user.getId()).join(), true, "User should exist after save");
            repository.delete(user.getId()).join();
            assertEquals(repository.existsById(user.getId()).join(), false, "User should not exist after delete");
        } finally {
            executor.shutdown();
        }
    }

    private static void assertEquals(final Object expected,
                                     final Object actual,
                                     final String message) {
        if (!Objects.equals(expected, actual))
            throw new IllegalArgumentException(message + String.format("\n%s != %s", expected, actual));
    }

}
