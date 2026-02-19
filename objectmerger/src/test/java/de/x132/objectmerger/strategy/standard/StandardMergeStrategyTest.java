package de.x132.objectmerger.strategy.standard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.x132.objectmerger.LabeledSource;
import de.x132.objectmerger.ObjectMerger;
import de.x132.objectmerger.strategy.FieldDefinition;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

@SuppressWarnings("rawtypes")
@DisplayName("StandardMergeStrategy Tests")
class StandardMergeStrategyTest {

    private StandardMergeStrategy strategy;
    private FieldDefinition fieldDef;

    @BeforeEach
    void setUp() {
        strategy = new StandardMergeStrategy();
        fieldDef = mock(FieldDefinition.class);
    }

    @Test
    @DisplayName("Should return first non-null value")
    void testMergeFirstNonNull() {
        String fieldName = "name";
        Object source1 = new Object();
        Object source2 = new Object();
        Object source3 = new Object();

        List<LabeledSource<?>> sources =
                Arrays.asList(
                        new LabeledSource<>("source1", source1),
                        new LabeledSource<>("source2", source2),
                        new LabeledSource<>("source3", source3));

        when(fieldDef.getDefaultValue()).thenReturn(null);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn(null);
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source2, fieldName))
                    .thenReturn("John");
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source3, fieldName))
                    .thenReturn("Jane");

            Object result = strategy.merge(sources, fieldDef, fieldName);

            assertEquals("John", result);
        }
    }

    @Test
    @DisplayName("Should return default value when all are null")
    void testMergeAllNull() {
        String fieldName = "age";
        Object source1 = new Object();
        List<LabeledSource<?>> sources = Arrays.asList(new LabeledSource<>("source1", source1));

        when(fieldDef.getDefaultValue()).thenReturn(0);

        try (MockedStatic<ObjectMerger> mockedObjectMerger = mockStatic(ObjectMerger.class)) {
            mockedObjectMerger
                    .when(() -> ObjectMerger.getFieldValue(source1, fieldName))
                    .thenReturn(null);
            Object result = strategy.merge(sources, fieldDef, fieldName);
            assertEquals(0, result);
        }
    }
}
