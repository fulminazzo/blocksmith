package it.fulminazzo.blocksmith.data;

/**
 * Base interface for query engines.
 * <br>
 * A QueryEngine encapsulates all backend-specific resources and operations needed
 * to interact with a particular data storage system.
 * Each backend implements this interface with its own specific methods and resources.
 * <br>
 * This is a marker interface - subclasses provide their own backend-specific methods.
 * There are no shared methods because each backend has fundamentally different async models
 * and APIs.
 *
 * @param <T>  the type of the entities
 * @param <ID> the type of the id of the entities
 */
public interface QueryEngine<T, ID> {

}