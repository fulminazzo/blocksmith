package it.fulminazzo.blocksmith.sql;

import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.sql.SqlDataSource;
import it.fulminazzo.blocksmith.util.TestUtils;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class SqlExample {

    public static void main(String[] args) throws IOException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try (
                SqlDataSource dataSource = SqlDataSource.builder()
                        .username("sa")
                        .password("")
                        .database("test")
                        .h2()
                        .disk("./example/build/h2db/")
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
            TestUtils.assertEquals(repository.existsById(user.getId()).join(), false, "User should not exist at start");
            TestUtils.assertEquals(repository.save(user).join(), user, "Saved user should be equal to current");
            TestUtils.assertEquals(repository.existsById(user.getId()).join(), true, "User should exist after save");
            repository.delete(user.getId()).join();
            TestUtils.assertEquals(repository.existsById(user.getId()).join(), false, "User should not exist after delete");
        } finally {
            executor.shutdown();
        }
    }

}
