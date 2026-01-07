package de.x132.objectmerger.service;

import com.google.gson.Gson;
import de.x132.LabeledSource;
import de.x132.MergeDefinition;
import de.x132.ObjectMerger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ObjectMergerService {

    private final Gson gson = new Gson();

    /**
     * Merges multiple labeled sources into a single object.
     * Converts JSON maps to POJOs if necessary.
     *
     * @param targetClass the fully qualified class name
     * @param definition  the merge definition
     * @param sources     the labeled sources (as Objects or Maps)
     * @return the merged object
     * @throws ClassNotFoundException if the target class cannot be resolved
     */
    public Object merge(String targetClass, MergeDefinition definition, List<LabeledSource<?>> sources)
            throws ClassNotFoundException {
        @SuppressWarnings("unchecked")
        Class<Object> clazz = (Class<Object>) Class.forName(targetClass);

        // Convert sources from Maps to POJOs if necessary
        List<LabeledSource<Object>> convertedSources = new ArrayList<>();
        for (LabeledSource<?> source : sources) {
            Object sourceData = source.getSource();

            // If source is a Map, convert it to the target POJO type
            if (sourceData instanceof java.util.Map) {
                String json = gson.toJson(sourceData);
                Object pojo = gson.fromJson(json, clazz);
                convertedSources.add(new LabeledSource<>(source.getLabel(), pojo));
            } else {
                convertedSources.add(new LabeledSource<>(source.getLabel(), sourceData));
            }
        }

        return ObjectMerger.merge(clazz, definition, convertedSources.toArray(new LabeledSource[0]));
    }
}
