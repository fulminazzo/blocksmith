package it.fulminazzo.blocksmith.data.redis;

import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.util.TestUtils;
import redis.embedded.RedisServer;

import java.util.concurrent.ExecutionException;

public final class RedisRepositoryExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        RedisServer server = new RedisServer();
        server.start();
        try (
                RedisDataSource dataSource = RedisDataSource.builder()
                        .host("localhost")
                        .build()
        ) {
            Repository<User, Long> repository = dataSource.newRepository(
                    User::getId,
                    User.class
            );
            User user = new User(1337L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), false, "User should not exist at start");
            TestUtils.assertEquals(repository.save(user).get(), user, "Saved user should be equal to current");
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), true, "User should exist after save");
            repository.delete(user.getId()).get();
            TestUtils.assertEquals(repository.existsById(user.getId()).get(), false, "User should not exist after delete");
        } finally {
            server.stop();
        }
    }

}