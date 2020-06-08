package advanced_networking_lab.exercise5.utils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BiMapSet<K, V>
{
	private Map<K, HashSet<V>> mapKeyValue = new HashMap<>();
	private Map<V, HashSet<K>> mapValueKey = new HashMap<>();

	private synchronized static <Key, Value> void ensureKeyIsInitialized(Map<Key, HashSet<Value>> map, Key key)
	{
		if (!map.containsKey(key)) 
		{
			map.put(key, new HashSet<Value>());
		}
	}
	
	public synchronized boolean add(K key, V value)
	{
		boolean inserted = false;
		// add forward mapping
		ensureKeyIsInitialized(mapKeyValue, key);
		inserted |= mapKeyValue.get(key).add(value);
		// add backward mapping
		ensureKeyIsInitialized(mapValueKey, value);
		inserted |= mapValueKey.get(value).add(key);
		
		return inserted;
	}
	
	public synchronized boolean contains(K key, V value)
	{
		return mapKeyValue.containsKey(key) && mapKeyValue.get(key).contains(value);
	}
	
	public synchronized boolean containsKey(K key)
	{
		return mapKeyValue.containsKey(key);
	}
	
	public synchronized boolean containsValue(V value)
	{
		return mapValueKey.containsKey(value);
	}
	
	public synchronized boolean remove(K key, V value)
	{
		boolean removed = false;
		if (contains(key, value))
		{
			// remove forward mapping
			removed |= mapKeyValue.get(key).remove(value);
			// remove backward mapping
			removed |= mapValueKey.get(value).remove(key);
		}
		return removed;
	}
	
	public synchronized Set<V> getValuesByKey(K key)
	{
		if (mapKeyValue.containsKey(key)) return new HashSet<>(mapKeyValue.get(key));
		else return new HashSet<>();	
	}
	
	public synchronized Set<K> getKeysByValue(V value)
	{
		if (mapValueKey.containsKey(value)) return new HashSet<>(mapValueKey.get(value));
		else return new HashSet<>();		
	}
	
	public synchronized Set<K> getKeySet()
	{
		return new HashSet<>(mapKeyValue.keySet());
	}
	
	public synchronized Set<V> getValueSet()
	{
		return new HashSet<>(mapValueKey.keySet());
	}	
}