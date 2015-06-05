package collection.map;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.NoSuchElementException;

public class LinkedMapUtils {

	public static <T> T get(Object obj, String property) throws Exception {
		if (obj != null) {
			Class class1 = obj.getClass();
			Field field = class1.getDeclaredField(property);
			field.setAccessible(true);
			return (T) field.get(obj);
		}
		return null;
	}

	/**
	 * Returns the first element in this list.
	 * 
	 * @return the first element in this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> Entry<K, V> getFirstEntry(LinkedHashMap<K, V> linkedHashMap) {
		if (linkedHashMap.isEmpty()) {
			throw new NoSuchElementException();
		}
		try {
			return get(get(linkedHashMap, "header"), "after");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the first element in this list.
	 * 
	 * @return the first element in this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> V getFirst(LinkedHashMap<K, V> linkedHashMap) {
		return getFirstEntry(linkedHashMap).getValue();
	}

	/**
	 * Returns the last element in this list.
	 * 
	 * @return the last element in this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> Entry<K, V> getLastEntry(LinkedHashMap<K, V> linkedHashMap) {
		if (linkedHashMap.isEmpty()) {
			throw new NoSuchElementException();
		}
		try {
			return get(get(linkedHashMap, "header"), "before");
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns the last element in this list.
	 * 
	 * @return the last element in this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> V getLast(LinkedHashMap<K, V> linkedHashMap) {
		return getLastEntry(linkedHashMap).getValue();
	}

	/**
	 * Removes and returns the first element from this list.
	 * 
	 * @return the first element from this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> V removeFirst(LinkedHashMap<K, V> linkedHashMap) {
		if (linkedHashMap.isEmpty()) {
			throw new NoSuchElementException();
		}
		return linkedHashMap.remove(getFirstEntry(linkedHashMap).getKey());
	}

	/**
	 * Removes and returns the last element from this list.
	 * 
	 * @return the last element from this list
	 * @throws NoSuchElementException
	 *             if this list is empty
	 */
	public static <K, V> V removeLast(LinkedHashMap<K, V> linkedHashMap) {
		return linkedHashMap.remove(getLastEntry(linkedHashMap).getKey());
	}

	/**
	 * Inserts the specified element at the beginning of this list.
	 * 
	 * @param e
	 *            the element to add
	 */
	public static <K, V> void putFirst(LinkedHashMap<K, V> linkedHashMap, K key, V value) {
		if (linkedHashMap.containsKey(key)) {
			linkedHashMap.remove(key);
		}
		LinkedHashMap<K, V> linkedHashMapCopy = new LinkedHashMap<>(linkedHashMap);
		linkedHashMap.clear();
		linkedHashMap.put(key, value);
		linkedHashMap.putAll(linkedHashMapCopy);
	}

	/**
	 * Appends the specified element to the end of this list.
	 * 
	 * <p>
	 * This method is equivalent to {@link #add}.
	 * 
	 * @param e
	 *            the element to add
	 */
	public static <K, V> void putLast(LinkedHashMap<K, V> linkedHashMap, K key, V value) {
		if (linkedHashMap.containsKey(key)) {
			linkedHashMap.remove(key);
		}
		if (!linkedHashMap.containsKey(key)) {
			linkedHashMap.put(key, value);
		}
	}
}
