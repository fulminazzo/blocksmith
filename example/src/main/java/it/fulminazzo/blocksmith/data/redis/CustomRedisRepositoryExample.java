package it.fulminazzo.blocksmith.data.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.data.mapper.Mapper;
import it.fulminazzo.blocksmith.util.TestUtils;
import org.jetbrains.annotations.NotNull;
import redis.embedded.RedisServer;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public final class CustomRedisRepositoryExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        RedisServer server = new RedisServer();
        server.start();
        try (
                RedisDataSource dataSource = RedisDataSource.builder()
                        .host("localhost")
                        .build()
        ) {
            User first = new User(1L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            User second = new User(1L, "Camilla", "Drinkwater", "cami@fulminazzo.it", 20);
            UserRepository repository = dataSource.newRepository(UserRepository::new);
            repository.save(first).get();
            repository.save(second).get();

            Optional<User> result = repository.findYoungestUser().get();
            TestUtils.assertEquals(result.isEmpty(), false, "Could not find youngest user");
            TestUtils.assertEquals(result.get(), second, "Youngest user was not youngest");

            repository.delete(first.getId()).get();
            repository.delete(second.getId()).get();
        } finally {
            server.stop();
        }
    }

    public static final class UserRepository extends RedisRepository<User, Long> {

        public UserRepository(final @NotNull StatefulRedisConnection<String, String> connection,
                              final @NotNull Mapper mapper) {
            super(
                    connection,
                    User::getId,
                    User.class,
                    mapper
            );
        }

        @Override
        public @NotNull CompletableFuture<User> save(final @NotNull User data) {
            return super.save(data)
                    .thenCompose(u -> query(async ->
                            async.zadd("users:by-age", u.getAge(), u.getId().toString())
                    ))
                    .thenApply(l -> data);
        }

        public @NotNull CompletableFuture<Optional<User>> findYoungestUser() {
            return query(async -> async.zrange("users:by-age", 0, 0))
                    .thenCompose(ids -> {
                        if (ids.isEmpty()) return CompletableFuture.completedFuture(Optional.empty());
                        else return query(async -> async.get(ids.get(0)))
                                .thenApply(Optional::ofNullable)
                                .thenApply(o -> o.map(s -> mapper.deserialize(s, User.class)));
                    });
        }

    }

}