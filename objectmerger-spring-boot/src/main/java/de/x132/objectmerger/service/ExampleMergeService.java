package de.x132.objectmerger.service;

/**
 * Service abstraction for executing sample merge requests.
 *
 * <p>Follows the Dependency Inversion Principle by defining an interface for the example merge
 * service.
 */
public interface ExampleMergeService {

    /**
     * Runs the pre-configured Person merge example using mock data from database, crm, and
     * analytics sources.
     *
     * @return The merged result object (typically a Person instance).
     * @throws Exception if class loading, conversion, or merge orchestration fails.
     */
    Object mergePersonExample() throws Exception;
}
