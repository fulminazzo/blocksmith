package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.util.TestUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Comparator;
import java.util.Optional;
import java.util.concurrent.*;

public final class CustomFileRepositoryExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            User first = new User(1L, "Alexander", "Drinkwater", "alex@fulminazzo.it", 23);
            User second = new User(1L, "Camilla", "Drinkwater", "cami@fulminazzo.it", 20);
            UserRepository repository = new UserRepository(
                    new File("example/build/resources/main/data/file/custom"),
                    executor,
                    LoggerFactory.getLogger(FileRepositoryExample.class),
                    ConfigurationFormat.YAML
            );
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

    public static final class UserRepository extends FileRepository<User, Long> {

        UserRepository(final @NotNull File dataDirectory,
                       final @NotNull Executor executor,
                       final @NotNull Logger logger,
                       final @NotNull ConfigurationFormat format) {
            super(
                    dataDirectory,
                    User.class,
                    User::getId,
                    executor,
                    logger,
                    format
            );
        }

        public @NotNull CompletableFuture<Optional<User>> findYoungestUser() {
            return findAll().thenApply(l ->
                    l.stream()
                            .min(Comparator.comparingInt(User::getAge))
                            .stream().findFirst()
            );
        }

    }

}