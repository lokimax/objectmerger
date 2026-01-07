package de.x132;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ObjectMergerTest {

    @Test
    void coalesceReturnsFirstNonNull() {
        assertEquals("a", ObjectMerger.coalesce("a", "b"));
        assertEquals("b", ObjectMerger.coalesce(null, "b"));
        assertNull(ObjectMerger.coalesce(null, null));
    }
}
