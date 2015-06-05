package collection.map;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * 
 * @author Tang
 * 
 * @param <K>
 * @param <V>
 */
public class LinkedHashMap<K, V> extends HashMap<K, V> implements Cloneable {

	private static final long serialVersionUID = 1L;
	private transient Entry<K, V> header;
	private final boolean accessOrder;
	private transient Entry<K, V> createEntryAtBefore;// 新创建的Entry需要添加在此Entry的前面，如果为空，则表示添加在header的前面，在header的前面就以为的是最后一个Entry

	public LinkedHashMap(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
		accessOrder = false;
	}

	public LinkedHashMap(int initialCapacity) {
		super(initialCapacity);
		accessOrder = false;
	}

	public LinkedHashMap() {
		super();
		accessOrder = false;
	}

	public LinkedHashMap(Map<? extends K, ? extends V> m) {
		super(m);
		accessOrder = false;
	}

	public LinkedHashMap(int initialCapacity, float loadFactor, boolean accessOrder) {
		super(initialCapacity, loadFactor);
		this.accessOrder = accessOrder;
	}

	@Override
	protected void init() {
		header = new Entry<>(-1, null, null, null);
		header.before = header.after = header;
	}

	@Override
	protected void transfer(HashMap.Entry[] newTable, boolean rehash) {
		int newCapacity = newTable.length;
		for (Entry<K, V> e = header.after; e != header; e = e.after) {
			if (rehash)
				e.hash = (e.getKey() == null) ? 0 : hash(e.getKey());
			int index = indexFor(e.hash, newCapacity);
			e.next = newTable[index];
			newTable[index] = e;
		}
	}

	public boolean containsValue(Object value) {
		if (value == null) {
			for (Entry e = header.after; e != header; e = e.after)
				if (e.getValue() == null)
					return true;
		} else {
			for (Entry e = header.after; e != header; e = e.after)
				if (value.equals(e.getValue()))
					return true;
		}
		return false;
	}

	public V get(Object key) {
		Entry<K, V> e = (Entry<K, V>) getEntry(key);
		if (e == null)
			return null;
		e.recordAccess(this);
		return e.getValue();
	}

	public void clear() {
		super.clear();
		header.before = header.after = header;
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
		createEntryAtBefore = header.after;
		put(key, value);
		createEntryAtBefore = null;
	}

	public void putLast(K key, V value) {
		createEntryAtBefore = header;
		put(key, value);
		createEntryAtBefore = null;
	}

	public Entry<K, V> getFirstEntry() {
		return header != null ? header.after : null;
	}

	public Entry<K, V> getLastEntry() {
		return header != null ? header.before : null;
	}

	public Entry<K, V> removeFirstEntry() {
		Entry<K, V> firstEntry = getFirstEntry();
		removeMapping(firstEntry);
		firstEntry.remove();
		firstEntry.before = firstEntry.after = null;
		return firstEntry;
	}

	public Entry<K, V> removeLastEntry() {
		Entry<K, V> lastEntry = getLastEntry();
		removeMapping(lastEntry);
		lastEntry.remove();
		lastEntry.before = lastEntry.after = null;
		return lastEntry;
	}

	protected static class Entry<K, V> extends HashMap.Entry<K, V> {
		protected Entry<K, V> before, after;

		protected Entry(int hash, K key, V value, HashMap.Entry<K, V> next) {
			super(hash, key, value, next);
		}

		protected void remove() {
			before.after = after;
			after.before = before;
		}

		protected void addBefore(Entry<K, V> existingEntry) {
			after = existingEntry;
			before = existingEntry.before;
			before.after = this;
			after.before = this;
		}

		protected void addAfter(Entry<K, V> existingEntry) {
			before = existingEntry;
			after = existingEntry.after;
			after.before = this;
			before.after = this;
		}

		protected void recordAccess(HashMap<K, V> m) {
			LinkedHashMap<K, V> lm = (LinkedHashMap<K, V>) m;
			if (lm.accessOrder) {
				lm.modCount++;
				remove();
				addBefore(lm.header);
			}
		}

		protected void recordRemoval(HashMap<K, V> m) {
			remove();
		}

		public Entry<K, V> getBefore() {
			return before;
		}

		public void setBefore(Entry<K, V> before) {
			this.before = before;
		}

		public Entry<K, V> getAfter() {
			return after;
		}

		public void setAfter(Entry<K, V> after) {
			this.after = after;
		}
	}

	protected abstract class LinkedHashIterator<T> implements Iterator<T> {
		protected Entry<K, V> nextEntry = header.after;
		protected Entry<K, V> lastReturned = null;
		protected int expectedModCount = modCount;

		public boolean hasNext() {
			return nextEntry != header;
		}

		public void remove() {
			if (lastReturned == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			LinkedHashMap.this.remove(lastReturned.getKey());
			lastReturned = null;
			expectedModCount = modCount;
		}

		protected Entry<K, V> nextEntry() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			if (nextEntry == header)
				throw new NoSuchElementException();
			Entry<K, V> e = lastReturned = nextEntry;
			nextEntry = e.after;
			return e;
		}
	}

	protected class KeyIterator extends LinkedHashIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}

	protected class ValueIterator extends LinkedHashIterator<V> {
		public V next() {
			return nextEntry().getValue();
		}
	}

	protected class EntryIterator extends LinkedHashIterator<Map.Entry<K, V>> {
		public Map.Entry<K, V> next() {
			return nextEntry();
		}
	}

	protected Iterator<K> newKeyIterator() {
		return new KeyIterator();
	}

	protected Iterator<V> newValueIterator() {
		return new ValueIterator();
	}

	protected Iterator<Map.Entry<K, V>> newEntryIterator() {
		return new EntryIterator();
	}

	protected void addEntry(int hash, K key, V value, int bucketIndex) {
		super.addEntry(hash, key, value, bucketIndex);
		Entry<K, V> first = header.after;
		if (removeFirstEntry(first)) {
			removeEntryForKey(first.getKey());
		}
	}

	protected void createEntry(int hash, K key, V value, int bucketIndex) {
		HashMap.Entry<K, V> old = table[bucketIndex];
		Entry<K, V> e = new Entry<>(hash, key, value, old);
		table[bucketIndex] = e;
		e.addBefore(createEntryAtBefore == null ? header : createEntryAtBefore);
		size++;
	}

	protected boolean removeFirstEntry(Map.Entry<K, V> eldest) {
		return false;
	}

}
