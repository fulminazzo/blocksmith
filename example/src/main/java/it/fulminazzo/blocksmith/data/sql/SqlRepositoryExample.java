package it.fulminazzo.blocksmith.data.sql;

import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.util.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqlRepositoryExample {

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
            Repository<User, Long> repository = dataSource.newRepository(
                    User.class,
                    Tables.USERS,
                    Tables.USERS.ID,
                    executor
            );
            User user = new User(1337L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), false, "User should not exist at start");
            TestUtils.assertEquals(repository.save(user).get(), user, "Saved user should be equal to current");
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), true, "User should exist after save");
            repository.delete(user.getId()).get();
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), false, "User should not exist after delete");
        } finally {
            executor.shutdown();
        }
    }

}
