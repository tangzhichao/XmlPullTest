package collection.map;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.Serializable;
import java.util.AbstractCollection;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

public class HashMap<K, V> extends AbstractMap<K, V> implements Map<K, V>, Cloneable, Serializable {
	private static final long serialVersionUID = 362498820763181265L;
	protected static final int DEFAULT_INITIAL_CAPACITY = 16;
	protected static final int MAXIMUM_CAPACITY = 1 << 30;
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
	protected static final int ALTERNATIVE_HASHING_THRESHOLD_DEFAULT = Integer.MAX_VALUE;
	protected transient Entry<K, V>[] table;
	protected transient int size;
	protected int threshold;
	protected final float loadFactor;
	protected transient int modCount;
	protected transient Set<Map.Entry<K, V>> entrySet = null;
	protected transient Set<K> keyset;
	protected transient Collection<V> valuecoll;

	public HashMap(int initialCapacity, float loadFactor) {
		if (initialCapacity < 0)
			throw new IllegalArgumentException("Illegal initial capacity: " + initialCapacity);
		if (initialCapacity > MAXIMUM_CAPACITY)
			initialCapacity = MAXIMUM_CAPACITY;
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new IllegalArgumentException("Illegal load factor: " + loadFactor);
		int capacity = 1;
		while (capacity < initialCapacity)
			capacity <<= 1;
		this.loadFactor = loadFactor;
		threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
		table = new Entry[capacity];
		init();
	}

	public HashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR);
	}

	public HashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR);
	}

	public HashMap(Map<? extends K, ? extends V> m) {
		this(Math.max((int) (m.size() / DEFAULT_LOAD_FACTOR) + 1, DEFAULT_INITIAL_CAPACITY), DEFAULT_LOAD_FACTOR);
		putAllForCreate(m);
	}

	protected void init() {
	}

	protected final int hash(Object k) {
		int h = 0;
		h ^= k.hashCode();
		h ^= (h >>> 20) ^ (h >>> 12);
		return h ^ (h >>> 7) ^ (h >>> 4);
	}

	protected static int indexFor(int h, int length) {
		return h & (length - 1);
	}

	public int size() {
		return size;
	}

	public boolean isEmpty() {
		return size == 0;
	}

	public V get(Object key) {
		if (key == null)
			return getForNullKey();
		Entry<K, V> entry = getEntry(key);

		return null == entry ? null : entry.getValue();
	}

	protected V getForNullKey() {
		for (Entry<K, V> e = table[0]; e != null; e = e.next) {
			if (e.getKey() == null)
				return e.getValue();
		}
		return null;
	}

	public boolean containsKey(Object key) {
		return getEntry(key) != null;
	}

	protected final Entry<K, V> getEntry(Object key) {
		int hash = (key == null) ? 0 : hash(key);
		for (Entry<K, V> e = table[indexFor(hash, table.length)]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.getKey()) == key || (key != null && key.equals(k))))
				return e;
		}
		return null;
	}

	public V put(K key, V value) {
		if (key == null)
			return putForNullKey(value);
		int hash = hash(key);
		int i = indexFor(hash, table.length);
		for (Entry<K, V> e = table[i]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.getKey()) == key || key.equals(k))) {
				V oldValue = e.getValue();
				e.setValue(value);
				e.recordAccess(this);
				return oldValue;
			}
		}
		modCount++;
		addEntry(hash, key, value, i);
		return null;
	}

	public V putForNullKey(V value) {
		for (Entry<K, V> e = table[0]; e != null; e = e.next) {
			if (e.getKey() == null) {
				V oldValue = e.getValue();
				e.setValue(value);
				e.recordAccess(this);
				return oldValue;
			}
		}
		modCount++;
		addEntry(0, null, value, 0);
		return null;
	}

	protected void putForCreate(K key, V value) {
		int hash = null == key ? 0 : hash(key);
		int i = indexFor(hash, table.length);
		for (Entry<K, V> e = table[i]; e != null; e = e.next) {
			Object k;
			if (e.hash == hash && ((k = e.getKey()) == key || (key != null && key.equals(k)))) {
				e.setValue(value);
				return;
			}
		}
		createEntry(hash, key, value, i);
	}

	protected void putAllForCreate(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			putForCreate(e.getKey(), e.getValue());
	}

	protected void resize(int newCapacity) {
		Entry[] oldTable = table;
		int oldCapacity = oldTable.length;
		if (oldCapacity == MAXIMUM_CAPACITY) {
			threshold = Integer.MAX_VALUE;
			return;
		}
		Entry[] newTable = new Entry[newCapacity];
		transfer(newTable, false);
		table = newTable;
		threshold = (int) Math.min(newCapacity * loadFactor, MAXIMUM_CAPACITY + 1);
	}

	protected void transfer(Entry[] newTable, boolean rehash) {
		int newCapacity = newTable.length;
		for (Entry<K, V> e : table) {
			while (null != e) {
				Entry<K, V> next = e.next;
				if (rehash) {
					e.hash = null == e.getKey() ? 0 : hash(e.getKey());
				}
				int i = indexFor(e.hash, newCapacity);
				e.next = newTable[i];
				newTable[i] = e;
				e = next;
			}
		}
	}

	public void putAll(Map<? extends K, ? extends V> m) {
		int numKeysToBeAdded = m.size();
		if (numKeysToBeAdded == 0)
			return;
		if (numKeysToBeAdded > threshold) {
			int targetCapacity = (int) (numKeysToBeAdded / loadFactor + 1);
			if (targetCapacity > MAXIMUM_CAPACITY)
				targetCapacity = MAXIMUM_CAPACITY;
			int newCapacity = table.length;
			while (newCapacity < targetCapacity)
				newCapacity <<= 1;
			if (newCapacity > table.length)
				resize(newCapacity);
		}
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet())
			put(e.getKey(), e.getValue());
	}

	public V remove(Object key) {
		Entry<K, V> e = removeEntryForKey(key);
		return (e == null ? null : e.getValue());
	}

	protected final Entry<K, V> removeEntryForKey(Object key) {
		int hash = (key == null) ? 0 : hash(key);
		int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> e = prev;
		while (e != null) {
			Entry<K, V> next = e.next;
			Object k;
			if (e.hash == hash && ((k = e.getKey()) == key || (key != null && key.equals(k)))) {
				modCount++;
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;
				e.recordRemoval(this);
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	protected final Entry<K, V> removeMapping(Object o) {
		if (!(o instanceof Map.Entry))
			return null;
		Map.Entry<K, V> entry = (Map.Entry<K, V>) o;
		Object key = entry.getKey();
		int hash = (key == null) ? 0 : hash(key);
		int i = indexFor(hash, table.length);
		Entry<K, V> prev = table[i];
		Entry<K, V> e = prev;
		while (e != null) {
			Entry<K, V> next = e.next;
			if (e.hash == hash && e.equals(entry)) {
				modCount++;
				size--;
				if (prev == e)
					table[i] = next;
				else
					prev.next = next;
				e.recordRemoval(this);
				return e;
			}
			prev = e;
			e = next;
		}
		return e;
	}

	public void clear() {
		modCount++;
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			tab[i] = null;
		size = 0;
	}

	public boolean containsValue(Object value) {
		if (value == null)
			return containsNullValue();
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			for (Entry e = tab[i]; e != null; e = e.next)
				if (value.equals(e.getValue()))
					return true;
		return false;
	}

	protected boolean containsNullValue() {
		Entry[] tab = table;
		for (int i = 0; i < tab.length; i++)
			for (Entry e = tab[i]; e != null; e = e.next)
				if (e.getValue() == null)
					return true;
		return false;
	}

	public Object clone() {
		HashMap<K, V> result = null;
		try {
			result = (HashMap<K, V>) super.clone();
		} catch (CloneNotSupportedException e) {
		}
		result.table = new Entry[table.length];
		result.entrySet = null;
		result.modCount = 0;
		result.size = 0;
		result.init();
		result.putAllForCreate(this);
		return result;
	}

	public static class Entry<K, V> extends SimpleEntry<K, V> implements Map.Entry<K, V> {

		protected Entry<K, V> next;
		protected int hash;

		protected Entry(int h, K k, V v, Entry<K, V> n) {
			super(k, v);
			next = n;
			hash = h;
		}

		public final boolean equals(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry e = (Map.Entry) o;
			Object k1 = getKey();
			Object k2 = e.getKey();
			if (k1 == k2 || (k1 != null && k1.equals(k2))) {
				Object v1 = getValue();
				Object v2 = e.getValue();
				if (v1 == v2 || (v1 != null && v1.equals(v2)))
					return true;
			}
			return false;
		}

		public final int hashCode() {
			K key = getKey();
			V value = getValue();
			return (key == null ? 0 : key.hashCode()) ^ (value == null ? 0 : value.hashCode());
		}

		protected void recordAccess(HashMap<K, V> m) {
		}

		protected void recordRemoval(HashMap<K, V> m) {
		}

		public Entry<K, V> getNext() {
			return next;
		}

		public void setNext(Entry<K, V> next) {
			this.next = next;
		}

		public int getHash() {
			return hash;
		}

		public void setHash(int hash) {
			this.hash = hash;
		}
	}

	protected void addEntry(int hash, K key, V value, int bucketIndex) {
		if ((size >= threshold) && (null != table[bucketIndex])) {
			resize(2 * table.length);
			hash = (null != key) ? hash(key) : 0;
			bucketIndex = indexFor(hash, table.length);
		}

		createEntry(hash, key, value, bucketIndex);
	}

	protected void createEntry(int hash, K key, V value, int bucketIndex) {
		Entry<K, V> e = table[bucketIndex];
		table[bucketIndex] = new Entry<>(hash, key, value, e);
		size++;
	}

	protected abstract class HashIterator<E> implements Iterator<E> {
		protected Entry<K, V> next; // next entry to return
		protected int expectedModCount; // For fast-fail
		protected int index; // current slot
		protected Entry<K, V> current; // current entry

		protected HashIterator() {
			expectedModCount = modCount;
			if (size > 0) { // advance to first entry
				Entry[] t = table;
				while (index < t.length && (next = t[index++]) == null)
					;
			}
		}

		public final boolean hasNext() {
			return next != null;
		}

		protected final Entry<K, V> nextEntry() {
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			Entry<K, V> e = next;
			if (e == null)
				throw new NoSuchElementException();

			if ((next = e.next) == null) {
				Entry[] t = table;
				while (index < t.length && (next = t[index++]) == null)
					;
			}
			current = e;
			return e;
		}

		public void remove() {
			if (current == null)
				throw new IllegalStateException();
			if (modCount != expectedModCount)
				throw new ConcurrentModificationException();
			Object k = current.getKey();
			current = null;
			HashMap.this.removeEntryForKey(k);
			expectedModCount = modCount;
		}
	}

	protected final class ValueIterator extends HashIterator<V> {
		public V next() {
			return nextEntry().getValue();
		}
	}

	protected final class KeyIterator extends HashIterator<K> {
		public K next() {
			return nextEntry().getKey();
		}
	}

	protected final class EntryIterator extends HashIterator<Map.Entry<K, V>> {
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

	public Set<K> keySet() {
		Set<K> ks = keyset;
		return (ks != null ? ks : (keyset = new KeySet()));
	}

	protected class KeySet extends AbstractSet<K> {
		public Iterator<K> iterator() {
			return newKeyIterator();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsKey(o);
		}

		public boolean remove(Object o) {
			return HashMap.this.removeEntryForKey(o) != null;
		}

		public void clear() {
			HashMap.this.clear();
		}
	}

	public Collection<V> values() {
		Collection<V> vs = valuecoll;
		return (vs != null ? vs : (valuecoll = new Values()));
	}

	protected final class Values extends AbstractCollection<V> {
		public Iterator<V> iterator() {
			return newValueIterator();
		}

		public int size() {
			return size;
		}

		public boolean contains(Object o) {
			return containsValue(o);
		}

		public void clear() {
			HashMap.this.clear();
		}
	}

	public Set<Map.Entry<K, V>> entrySet() {
		return entrySet0();
	}

	protected Set<Map.Entry<K, V>> entrySet0() {
		Set<Map.Entry<K, V>> es = entrySet;
		return es != null ? es : (entrySet = new EntrySet());
	}

	protected final class EntrySet extends AbstractSet<Map.Entry<K, V>> {
		public Iterator<Map.Entry<K, V>> iterator() {
			return newEntryIterator();
		}

		public boolean contains(Object o) {
			if (!(o instanceof Map.Entry))
				return false;
			Map.Entry<K, V> e = (Map.Entry<K, V>) o;
			Entry<K, V> candidate = getEntry(e.getKey());
			return candidate != null && candidate.equals(e);
		}

		public boolean remove(Object o) {
			return removeMapping(o) != null;
		}

		public int size() {
			return size;
		}

		public void clear() {
			HashMap.this.clear();
		}
	}

	private void writeObject(java.io.ObjectOutputStream s) throws IOException {
		Iterator<Map.Entry<K, V>> i = (size > 0) ? entrySet0().iterator() : null;
		s.defaultWriteObject();
		s.writeInt(table.length);
		s.writeInt(size);
		if (size > 0) {
			for (Map.Entry<K, V> e : entrySet0()) {
				s.writeObject(e.getKey());
				s.writeObject(e.getValue());
			}
		}
	}

	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException {
		s.defaultReadObject();
		if (loadFactor <= 0 || Float.isNaN(loadFactor))
			throw new InvalidObjectException("Illegal load factor: " + loadFactor);
		s.readInt(); // ignored
		int mappings = s.readInt();
		if (mappings < 0)
			throw new InvalidObjectException("Illegal mappings count: " + mappings);

		int initialCapacity = (int) Math.min(mappings * Math.min(1 / loadFactor, 4.0f), HashMap.MAXIMUM_CAPACITY);
		int capacity = 1;
		while (capacity < initialCapacity) {
			capacity <<= 1;
		}

		table = new Entry[capacity];
		threshold = (int) Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);

		init(); // Give subclass a chance to do its thing.
		for (int i = 0; i < mappings; i++) {
			K key = (K) s.readObject();
			V value = (V) s.readObject();
			putForCreate(key, value);
		}
	}

	protected int capacity() {
		return table.length;
	}

	protected float loadFactor() {
		return loadFactor;
	}
}
