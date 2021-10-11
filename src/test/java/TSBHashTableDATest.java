import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;
import soporte.TSBHashTableDA;

public class TSBHashTableDATest {

    private TSBHashTableDA<String, Integer> table;

    @Before
    public void setup() {
        table = new TSBHashTableDA<>();
    }

    private void addTestData() {
        addTestData(table);
    }

    private void addTestData(Map<String, Integer> map) {
        map.put("a", 1);
        map.put("b", 2);
        map.put("c", 3);
        map.put("d", 4);
        map.put("e", 5);
    }

    @Test
    public void whenElementAreAddedRemoved_sizeChanges() {
        assertEquals(0, table.size());
        table.put("a", 1);
        assertEquals(1, table.size());
        table.put("b", 1);
        assertEquals(2, table.size());
        table.remove("b", 1);
        assertEquals(1, table.size());

    }

    @Test
    public void whenElementAreAddedRemoved_isEmptyChanges() {
        assertTrue(table.isEmpty());
        table.put("a", 1);
        assertFalse(table.isEmpty());
        table.put("b", 1);
        assertFalse(table.isEmpty());
        table.remove("a");
        table.remove("b");
        assertTrue(table.isEmpty());
    }

    @Test
    public void testContainsKey() {
        String key1 = "a";
        String key2 = "b";
        table.put(key1, 1);
        assertTrue(table.containsKey(key1));
        assertFalse(table.containsKey(key2));
        table.put(key2, 1);
        assertTrue(table.containsKey(key2));
        table.remove(key1);
        assertFalse(table.containsKey(key1));
    }

    @Test
    public void testContainsValue() {
        Integer value1 = 1;
        Integer value2 = 2;
        table.put("a", value1);
        assertTrue(table.containsValue(value1));
        assertFalse(table.containsValue(value2));
        table.put("b", value2);
        assertTrue(table.containsValue(value2));
        table.remove("a");
        assertFalse(table.containsValue(value1));
    }

    @Test(expected = NullPointerException.class)
    public void whenGetCalledWithNull_throwException() {
        table.get(null);
    }

    @Test
    public void testGet() {
        String key = "key";
        assertNull(table.get(key));
        table.put(key, 1);
        assertNotNull(table.get(key));
        assertEquals(Integer.valueOf(1), table.get(key));
    }

    @Test(expected = NullPointerException.class)
    public void whenPutCalledWithNullKey_throwException() {
        table.put(null, 1);
    }

    @Test(expected = NullPointerException.class)
    public void whenPutCalledWithNullValue_throwException() {
        table.put("a", null);
    }

    @Test
    public void testPut() {
        String key = "key";
        Integer result;
        result = table.put(key, 1);
        assertNull(result);
        result = table.put(key, 2);
        assertEquals("Put debería devolver el valor previo", Integer.valueOf(1), result);
        assertEquals(1, table.size());
    }

    @Test(expected = NullPointerException.class)
    public void whenRemoveCalledWithNullValue_throwException() {
        table.remove(null);
    }

    @Test
    public void testRemove() {
        String key = "key";
        Integer result = table.remove(key);
        assertNull(result);
        table.put(key, 2);
        result = table.remove(key);
        assertNotNull(result);
        assertEquals("remove debería devolver el valor eliminado", Integer.valueOf(2), result);
    }

    @Test(expected = NullPointerException.class)
    public void whenPutAllCalledWithNull_thenThrow() {
        table.putAll(null);
    }

    @Test(expected = NullPointerException.class)
    public void whenPutAllCalledWithMapWithNullKeys_thenThrow() {
        Map<String, Integer> m = Map.of("a", 1, null, 2);
        table.putAll(m);
    }

    @Test(expected = NullPointerException.class)
    public void whenPutAllCalledWithMapWithNullValues_thenThrow() {
        Map<String, Integer> m = Map.of("a", 1, "b", null);
        table.putAll(m);
    }

    @Test
    public void testPutAll() {
        Map<String, Integer> m = Map.of("a", 1, "b", 2, "c", 3);
        table.putAll(m);
        assertEquals(3, table.size());
        assertEquals(Integer.valueOf(1), table.get("a"));
        assertEquals(Integer.valueOf(2), table.get("b"));
        assertEquals(Integer.valueOf(3), table.get("c"));
    }

    @Test
    public void testClear() {
        addTestData();
        assertEquals(5, table.size());
        table.clear();
        assertEquals(0, table.size());
        assertNull(table.get("a"));
    }

    @Test
    public void testKeySet() {
        EntryVerifier verifier = new EntryVerifier(List.of("a", "b"));
        Set<String> keys = table.keySet();
        assertEquals(0, keys.size());
        assertFalse(keys.contains("a"));
        table.put("a", 1);
        assertEquals(1, keys.size());
        assertTrue(keys.contains("a"));
        table.put("b", 1);
        assertEquals(2, keys.size());
        assertTrue(keys.contains("b"));
        for (String key : keys) {
            verifier.check(key);
        }
        verifier.assertAllChecked();
        table.clear();
        assertEquals(0, keys.size());
        assertFalse(keys.contains("a"));

    }

    @Test
    public void testValuesSet() {
        EntryVerifier verifier = new EntryVerifier(List.of(1, 2));
        Collection<Integer> values = table.values();
        assertEquals(0, values.size());
        assertFalse(values.contains(1));
        table.put("a", 1);
        assertEquals(1, values.size());
        assertTrue(values.contains(1));
        table.put("b", 2);
        assertEquals(2, values.size());
        assertTrue(values.contains(2));
        for (Integer value : values) {
            verifier.check(value);
        }
        verifier.assertAllChecked();
        table.clear();
        assertEquals(0, values.size());
        assertFalse(values.contains(1));
    }

    @Test
    public void testEntrySet() {

        EntryVerifier keyVerifier = new EntryVerifier(List.of("a", "b", "c", "d", "e"));
        EntryVerifier valuesVerifier = new EntryVerifier(List.of(1, 2, 3, 4, 5));

        Set<Map.Entry<String, Integer>> entries = table.entrySet();
        assertEquals(0, entries.size());
        addTestData();
        assertEquals(5, entries.size());

        for (var entry : entries) {
            keyVerifier.check(entry.getKey());
            valuesVerifier.check(entry.getValue());
        }
        keyVerifier.assertAllChecked();
        valuesVerifier.assertAllChecked();
        table.clear();
        assertEquals(0, entries.size());
    }

    @Test
    public void testEqualsAndHashcode() {
        TSBHashTableDA<String, Integer> other = new TSBHashTableDA<>();
        assertTrue(other.equals(table));
        assertEquals(table.hashCode(), other.hashCode());
        table.put("z", 42);
        assertFalse(other.equals(table));
        assertNotEquals(table.hashCode(), other.hashCode());
        other.put("z", 11);
        assertFalse(other.equals(table));
        assertNotEquals(table.hashCode(), other.hashCode());
        other.put("z", 42);
        assertTrue(other.equals(table));
        assertEquals(table.hashCode(), other.hashCode());
        addTestData();
        assertFalse(other.equals(table));
        assertNotEquals(table.hashCode(), other.hashCode());
        addTestData(other);
        assertTrue(other.equals(table));
        assertEquals(table.hashCode(), other.hashCode());
        table.remove("c");
        assertFalse(other.equals(table));
        assertNotEquals(table.hashCode(), other.hashCode());
    }

    @Test
    public void testClone() throws CloneNotSupportedException {
        addTestData();
        TSBHashTableDA<String, Integer> other = (TSBHashTableDA<String, Integer>) table.clone();
        assertEquals(table, other);
        other.put("z", 42);
        assertNotEquals(table, other);
        assertEquals(5, table.size());
        assertEquals(6, other.size());
        assertFalse(table.values().contains(42));
    }


//    Object clone()


    class EntryVerifier {
        List<Entry> entries;

        public EntryVerifier(List<Object> values) {
            entries = new ArrayList<>(values.size());
            for (Object value: values) {
                entries.add(new Entry(value));
            }
        }

        public void check(Object entry) {
            for (Entry e: entries) {
                if (e.entry.equals(entry)) {
                    if (e.checked) {
                        fail("Entry duplicated");
                    }
                    e.checked = true;
                    return;
                }
            }
            fail("Entry not found: " + entry);
        }

        public void assertAllChecked() {
            for (Entry e: entries) {
                if (!e.checked) {
                    fail("Entry not checked: " + e.entry);
                }
            }
        }
    }

    class Entry {
        Object entry;
        boolean checked;

        public Entry(Object entry) {
            this.entry = entry;
        }
    }
}