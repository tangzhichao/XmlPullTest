package collection.map;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

/**
 * 一个采用LinkedList实现的链表Map，用来替代LinkedHashMap。
 * 此Map效率非常低，不建议在数据量过大的情况下使用。
 * 
 * @author Tang
 * 
 * @param <K>
 * @param <V>
 */
public class LinkedMap<K, V> extends AbstractMap<K, V> implements Cloneable {

	private LinkedList<Entry<K, V>> entrieList;
	private List<Entry<K, V>> unmodifiableEntrieList;
	private AbstractSet<Entry<K, V>> entrieSet;
	private Set<Entry<K, V>> unmodifiableEntrieSet;

	public LinkedMap() {
		entrieList = new LinkedList<Entry<K, V>>();
		unmodifiableEntrieList = Collections.unmodifiableList(entrieList);
		entrieSet = new AbstractSet<Entry<K, V>>() {

			public Iterator<Entry<K, V>> iterator() {
				return entrieList.iterator();
			}

			public int size() {
				return entrieList.size();
			}
		};
		unmodifiableEntrieSet = Collections.unmodifiableSet(entrieSet);
	}

	@Override
	public Set<java.util.Map.Entry<K, V>> entrySet() {
		return unmodifiableEntrieSet;
	}

	public List<java.util.Map.Entry<K, V>> entryList() {
		return unmodifiableEntrieList;
	}

	@Override
	public V put(K key, V value) {
		Entry<K, V> entry = getEntry(key);
		if (entry != null) {
			entry.setValue(value);
		} else {
			entrieList.add(new SimpleEntry<>(key, value));
		}
		return entry != null ? entry.getValue() : null;
	}

	@Override
	public V get(Object key) {
		Entry<K, V> entry = getEntry(key);
		return entry != null ? entry.getValue() : null;
	}

	public Entry<K, V> getEntry(Object key) {
		Iterator<Entry<K, V>> i = entrySet().iterator();
		if (key == null) {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (e.getKey() == null)
					return e;
			}
		} else {
			while (i.hasNext()) {
				Entry<K, V> e = i.next();
				if (key == e.getKey() || key.equals(e.getKey()))
					return e;
			}
		}
		return null;
	}

	// ///////////////////////////

	public boolean add(Entry<K, V> e) {
		return addEntry(e);
	}

	public V getFirst() {
		return getFirstEntry().getValue();
	}

	public V getLast() {
		return getLastEntry().getValue();
	}

	public V removeFirst() {
		return removeFirstEntry().getValue();
	}

	public V removeLast() {
		return removeLastEntry().getValue();
	}

	public void putFirst(K key, V value) {
		addFirstEntry(new SimpleEntry<>(key, value));
	}

	public void putLast(K key, V value) {
		addLastEntry(new SimpleEntry<>(key, value));
	}

	public Entry<K, V> getFirstEntry() {
		return entrieList.getFirst();
	}

	public Entry<K, V> getLastEntry() {
		return entrieList.getLast();
	}

	public Entry<K, V> removeFirstEntry() {
		return entrieList.removeFirst();
	}

	public Entry<K, V> removeLastEntry() {
		return entrieList.removeLast();
	}

	public void addFirstEntry(Entry<K, V> entry) {
		entrieList.addFirst(entry);
	}

	public void addLastEntry(Entry<K, V> entry) {
		entrieList.addLast(entry);
	}

	public boolean containsEntry(Entry<K, V> o) {
		return entrieList.contains(o);
	}

	public int size() {
		return entrieList.size();
	}

	public boolean addEntry(Entry<K, V> e) {
		return entrieList.add(e);
	}

	public boolean removeEntry(Entry<K, V> o) {
		return entrieList.remove(o);
	}

	public boolean addAll(Collection<? extends Entry<K, V>> c) {
		return entrieList.addAll(c);
	}

	public boolean addAll(int index, Collection<? extends Entry<K, V>> c) {
		return entrieList.addAll(index, c);
	}

	public void clear() {
		entrieList.clear();
	}

	// Positional Access Operations

	public V get(int index) {
		return getEntry(index).getValue();
	}

	public Entry<K, V> getEntry(int index) {
		return entrieList.get(index);
	}

	public V set(int index, K key, V value) {
		return setEntry(index, new SimpleEntry<K, V>(key, value)).getValue();
	}

	public Entry<K, V> setEntry(int index, Entry<K, V> element) {
		return entrieList.set(index, element);
	}

	public void add(int index, K key, V value) {
		addEntry(index, new SimpleEntry<>(key, value));
	}

	public void addEntry(int index, Entry<K, V> element) {
		entrieList.add(index, element);
	}

	public V remove(int index) {
		return removeEntry(index).getValue();
	}

	public Entry<K, V> removeEntry(int index) {
		return entrieList.remove(index);
	}

	// Search Operations

	public int indexOf(K k) {
		return indexOfEntry(getEntry(k));
	}

	public int indexOfEntry(Entry<K, V> entry) {
		return entrieList.indexOf(entry);
	}

	public int lastIndexOf(K k) {
		return lastIndexOfEntry(getEntry(k));
	}

	public int lastIndexOfEntry(Entry<K, V> entry) {
		return entrieList.lastIndexOf(entry);
	}

	// Queue operations.

	public V peek() {
		return peekEntry().getValue();
	}

	public Entry<K, V> peekEntry() {
		return entrieList.peek();
	}

	public V element() {
		return elementEntry().getValue();
	}

	public Entry<K, V> elementEntry() {
		return entrieList.element();
	}

	public V poll() {
		return pollEntry().getValue();
	}

	public Entry<K, V> pollEntry() {
		return entrieList.poll();
	}

	public V remove() {
		return removeEntry().getValue();
	}

	public Entry<K, V> removeEntry() {
		return entrieList.remove();
	}

	public boolean offer(K key, V value) {
		return offerEntry(new SimpleEntry<>(key, value));
	}

	public boolean offerEntry(Entry<K, V> e) {
		return entrieList.offer(e);
	}

	public boolean offerFirst(K key, V value) {
		return offerFirstEntry(new SimpleEntry<>(key, value));
	}

	public boolean offerFirstEntry(Entry<K, V> e) {
		return entrieList.offerFirst(e);
	}

	public boolean offerLast(K key, V value) {
		return offerLastEntry(new SimpleEntry<>(key, value));
	}

	public boolean offerLastEntry(Entry<K, V> e) {
		return entrieList.offerLast(e);
	}

	public V peekFirst() {
		return peekFirstEntry().getValue();
	}

	public Entry<K, V> peekFirstEntry() {
		return entrieList.peekFirst();
	}

	public V peekLast() {
		return peekLastEntry().getValue();
	}

	public Entry<K, V> peekLastEntry() {
		return entrieList.peekLast();
	}

	public V pollFirst() {
		return pollFirstEntry().getValue();
	}

	public Entry<K, V> pollFirstEntry() {
		return entrieList.pollFirst();
	}

	public V pollLast() {
		return pollLastEntry().getValue();
	}

	public Entry<K, V> pollLastEntry() {
		return entrieList.pollLast();
	}

	public void push(K key, V value) {
		push(new SimpleEntry<K, V>(key, value));
	}

	public void push(Entry<K, V> e) {
		entrieList.push(e);
	}

	public V pop() {
		return popEntry().getValue();
	}

	public Entry<K, V> popEntry() {
		return entrieList.pop();
	}

	// ////////////////

	public boolean removeFirstOccurrence(K k) {
		return removeFirstOccurrenceEntry(getEntry(k));
	}

	public boolean removeFirstOccurrenceEntry(Object o) {
		return entrieList.removeFirstOccurrence(o);
	}

	public boolean removeLastOccurrence(K k) {
		return removeLastOccurrenceEntry(getEntry(k));
	}

	public boolean removeLastOccurrenceEntry(Entry<K, V> o) {
		return entrieList.removeLastOccurrence(o);
	}

	public ListIterator<Entry<K, V>> listIterator(int index) {
		return entrieList.listIterator(index);
	}

	public Iterator<Entry<K, V>> descendingIterator() {
		return entrieList.descendingIterator();
	}

	public LinkedMap<K, V> clone() throws CloneNotSupportedException {
		final LinkedMap<K, V> clone = (LinkedMap<K, V>) super.clone();
		clone.entrieList = (LinkedList<Entry<K, V>>) entrieList.clone();
		clone.entrieList.clear();
		for (int i = 0; i < entrieList.size(); i++) {
			Entry<K, V> entry = entrieList.get(i);
			K key = entry.getKey();
			K cloneKey = null;
			try {
				cloneKey = clone(key);
			} catch (Exception e) {
				cloneKey = null;
			}
			V value = entry.getValue();
			V cloneValue = null;
			try {
				cloneValue = clone(value);
			} catch (Exception e) {
				cloneValue = null;
			}
			SimpleEntry<K, V> newEntry = new SimpleEntry<>(cloneKey != null ? cloneKey : entry.getKey(), cloneValue != null ? cloneValue : entry.getValue());
			clone.entrieList.add(newEntry);
		}
		clone.unmodifiableEntrieList = Collections.unmodifiableList(clone.entrieList);

		clone.entrieSet = new AbstractSet<Entry<K, V>>() {
			public Iterator<Entry<K, V>> iterator() {
				return clone.entrieList.iterator();
			}

			public int size() {
				return clone.entrieList.size();
			}
		};
		clone.unmodifiableEntrieSet = Collections.unmodifiableSet(clone.entrieSet);

		return clone;
	}

	public Entry<K, V>[] toArray() {
		return (Entry<K, V>[]) entrieList.toArray();
	}

	public Entry<K, V>[] toArray(Entry<K, V>[] a) {
		return entrieList.toArray(a);
	}

	// ///////////////

	public static <T> T clone(T t) throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		if (t instanceof Cloneable) {
			Method method = t.getClass().getMethod("clone");
			method.setAccessible(true);
			T clone = (T) method.invoke(t);
			return clone;
		}
		return null;
	}
}
