package it.fulminazzo.blocksmith.data.file;

import org.jetbrains.annotations.NotNull;

/**
 * A builder for {@link FileRepository}.
 *
 * @param <T> the type of the data
 */
public final class FileRepositoryBuilder<T> extends MappableFileRepositoryBuilder<T, FileRepositoryBuilder<T>> {

    /**
     * Instantiates a new File repository builder.
     *
     * @param dataType the data type
     */
    FileRepositoryBuilder(final @NotNull Class<T> dataType) {
        super(dataType);
    }

}
