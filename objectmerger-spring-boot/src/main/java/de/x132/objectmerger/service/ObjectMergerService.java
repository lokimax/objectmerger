package de.x132.objectmerger.service;

import com.google.gson.Gson;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ObjectMergerService {

  private final Gson gson = new Gson();

  /**
   * Merges multiple labeled sources into a single object. Converts JSON maps to POJOs if necessary.
   *
   * @param targetClass the fully qualified class name
   * @param definition the merge definition
   * @param sources the labeled sources (as Objects or Maps)
   * @return the merged object
   * @throws ClassNotFoundException if the target class cannot be resolved
   */
  public Object merge(
      String targetClass, MergeDefinition definition, List<LabeledSource<?>> sources)
      throws ClassNotFoundException {
    @SuppressWarnings("unchecked")
    Class<Object> clazz = (Class<Object>) Class.forName(targetClass);

    if (java.util.Map.class.isAssignableFrom(clazz)) {
      System.out.println("DEBUG: Processing Map merge for targetClass: " + targetClass);
      if (definition == null || definition.getDefinitions() == null) {
        System.out.println("DEBUG: Definition is null or empty!");
      } else {
        System.out.println("DEBUG: Definition keys: " + definition.getDefinitions().keySet());
      }

      // Handle Map target class using the Map-specific merge method
      @SuppressWarnings("unchecked")
      List<LabeledSource<java.util.Map<String, Object>>> mapSources = new ArrayList<>();
      for (LabeledSource<?> source : sources) {
        Object sourceData = source.getSource();
        if (sourceData instanceof java.util.Map) {
          mapSources.add(
              new LabeledSource<>(source.getLabel(), (java.util.Map<String, Object>) sourceData));
        } else {
          // If source is not a map but target is map, try to convert via Gson?
          // Or just error. For now assume sources are maps if target is map.
          String json = gson.toJson(sourceData);
          java.util.Map<String, Object> map = gson.fromJson(json, java.util.Map.class);
          mapSources.add(new LabeledSource<>(source.getLabel(), map));
        }
      }
      return ObjectMerger.merge(definition, mapSources.toArray(new LabeledSource[0]));
    }

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
