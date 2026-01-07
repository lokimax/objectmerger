package de.x132.objectmerger.service;

import de.x132.LabeledSource;
import de.x132.MergeDefinition;
import de.x132.ObjectMerger;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObjectMergerService {

    /**
     * Merges multiple labeled sources into a single object.
     *
     * @param targetClass the fully qualified class name
     * @param definition the merge definition
     * @param sources the labeled sources (as Objects)
     * @return the merged object
     * @throws ClassNotFoundException if the target class cannot be resolved
     */
    public Object merge(String targetClass, MergeDefinition definition, List<LabeledSource<?>> sources)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<Object> clazz = (Class<Object>) Class.forName(targetClass);
        return ObjectMerger.merge(clazz, definition, sources.toArray(new LabeledSource[0]));
    }
}
