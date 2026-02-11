package it.fulminazzo.blocksmith.data.file;

import it.fulminazzo.blocksmith.config.ConfigurationFormat;
import it.fulminazzo.blocksmith.data.Repository;
import it.fulminazzo.blocksmith.data.User;
import it.fulminazzo.blocksmith.util.TestUtils;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class FileRepositoryExample {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Repository<User, Long> repository = FileRepository.builder(User.class)
                    .dataDirectory(new File("example/build/resources/main/data/file/simple"))
                    .executor(executor)
                    .dataLanguageFormat(ConfigurationFormat.YAML)
                    .logger(LoggerFactory.getLogger(FileRepositoryExample.class))
                    .idMapper(User::getId)
                    .build();
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