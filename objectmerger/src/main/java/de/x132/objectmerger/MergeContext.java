package de.x132.objectmerger;

import java.util.List;

/**
 * Context object to hold the state of a merge operation. Used to pass state between merge methods.
 *
 * @param targetClass The class of the result object.
 * @param result The result object instance.
 * @param sources The list of sources to merge.
 * @param <T> The type of the object being merged.
 */
record MergeContext<T>(Class<T> targetClass, T result, List<LabeledSource<T>> sources) {}
