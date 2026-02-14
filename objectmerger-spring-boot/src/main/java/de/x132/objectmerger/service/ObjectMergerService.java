package de.x132.objectmerger.service;

import com.google.gson.Gson;
import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.MergeDefinition;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.security.ClassLoadingGuard;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ObjectMergerService {

  private final Gson gson = new Gson();
  private final ClassLoadingGuard classLoadingGuard;

  public ObjectMergerService(ClassLoadingGuard classLoadingGuard) {
    this.classLoadingGuard = classLoadingGuard;
  }

  @SuppressWarnings("unchecked")
  public Object merge(
      String targetClass, MergeDefinition definition, List<LabeledSource<?>> sources)
      throws ClassNotFoundException {

    Class<Object> clazz = (Class<Object>) classLoadingGuard.loadClassSafely(targetClass);

    if (Map.class.isAssignableFrom(clazz)) {
      return mergeAsMaps(definition, sources);
    }

    return mergeAsPojos(clazz, definition, sources);
  }

  private Object mergeAsMaps(MergeDefinition definition, List<LabeledSource<?>> sources) {
    List<LabeledSource<Map<String, Object>>> mapSources = new ArrayList<>();
    for (LabeledSource<?> source : sources) {
      Object sourceData = source.getSource();
      if (sourceData instanceof Map) {
        mapSources.add(new LabeledSource<>(source.getLabel(), (Map<String, Object>) sourceData));
      } else {
        String json = gson.toJson(sourceData);
        Map<String, Object> map = gson.fromJson(json, Map.class);
        mapSources.add(new LabeledSource<>(source.getLabel(), map));
      }
    }

    @SuppressWarnings("unchecked")
    Object mergedMap = ObjectMerger.merge(definition, mapSources.toArray(new LabeledSource[0]));
    return mergedMap;
  }

  private Object mergeAsPojos(
      Class<Object> clazz, MergeDefinition definition, List<LabeledSource<?>> sources) {
    List<LabeledSource<Object>> convertedSources = new ArrayList<>();
    for (LabeledSource<?> source : sources) {
      Object sourceData = source.getSource();

      if (sourceData instanceof Map) {
        String json = gson.toJson(sourceData);
        Object pojo = gson.fromJson(json, clazz);
        convertedSources.add(new LabeledSource<>(source.getLabel(), pojo));
      } else {
        convertedSources.add(new LabeledSource<>(source.getLabel(), sourceData));
      }
    }

    @SuppressWarnings("unchecked")
    Object merged =
        ObjectMerger.merge(clazz, definition, convertedSources.toArray(new LabeledSource[0]));
    return merged;
  }
}
