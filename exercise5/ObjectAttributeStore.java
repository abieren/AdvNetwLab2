package advanced_networking_lab.exercise5;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public class ObjectAttributeStore<O>
{	
	private BiMapSet<O, Attribute<O, ?>> mapObjectAttributes = new BiMapSet<>();
	private BiMapSet<AttributeKey<?>, Attribute<O, ?>> mapKeyAttributes = new BiMapSet<>();
	private BiMapSet<Object, Attribute<O, ?>> mapValueAttributes = new BiMapSet<>();
	
	public <V> boolean contains(O object, AttributeKey<V> key)
	{
		Set<Attribute<O, ?>> intersectionObjectKey 
				= mapObjectAttributes.getValuesByKey(object);
		intersectionObjectKey.retainAll(mapKeyAttributes.getValuesByKey(key));
		
		return !intersectionObjectKey.isEmpty();
	}
	
	public <V> boolean contains(O object, AttributeKey<V> key, V value)
	{
		Attribute<O, V> triple = 
				new Attribute<>(object, key, value);
		return mapObjectAttributes.contains(object, triple);
	}
	
	public <V> Attribute<O, V> get(O object, AttributeKey<V> key)
	{	
		Set<Attribute<O, ?>> intersectionObjectKey 
				= mapObjectAttributes.getValuesByKey(object);
		intersectionObjectKey.retainAll(mapKeyAttributes.getValuesByKey(key));

		if (intersectionObjectKey.isEmpty()) return null;
		else return (Attribute<O, V>) intersectionObjectKey.iterator().next();
	}
	
	public <V> Attribute<O, V> remove(O object, AttributeKey<V> key)
	{
		Set<Attribute<O, ?>> intersectionObjectKey 
				= mapObjectAttributes.getValuesByKey(object);
		intersectionObjectKey.retainAll(mapKeyAttributes.getValuesByKey(key));
		
		if (intersectionObjectKey.isEmpty())
		{
			// removed no existing attribute
			return null;
		}
		else
		{
			// remove existing attribute
			Attribute<O, ?> attribute = intersectionObjectKey.iterator().next();
			mapObjectAttributes.remove(attribute.object, attribute);
			mapKeyAttributes.remove(attribute.key, attribute);
			mapValueAttributes.remove(attribute.value, attribute);
			// return removed attribute
			return (Attribute<O, V>) intersectionObjectKey.iterator().next();
		}
	}
	
	public <V> Attribute<O, V> put(O object, AttributeKey<V> key, V value)
	{
		Attribute<O, V> triple = new Attribute<>(object, key, value);
		// first remove existent attribute before adding attribute with new value
		Attribute<O, V> removed = remove(object, key);
		// now add the triple
		mapObjectAttributes.add(object, triple);
		mapKeyAttributes.add(key, triple);
		mapValueAttributes.add(value, triple);
		// return old attribute value
		return removed;
	}
	
	public <V> Set<Attribute<O, ?>> getByObject(O object)
	{
		return mapObjectAttributes.getValuesByKey(object);
	}
	
	private <V> Set<Attribute<O, V>> castAttributeSetToValueType(Set<Attribute<O, ?>> attributes)
	{
		// perform element-wise casting and form a new set
				return attributes.stream().map((attr)->(Attribute<O, V>) attr)
					.collect(Collectors.toCollection(HashSet::new));
	}
	
	public <V> Set<Attribute<O, V>> getByKey(AttributeKey<V> key)
	{
		Set<Attribute<O, ?>> attributes = mapKeyAttributes.getValuesByKey(key);
		return castAttributeSetToValueType(attributes);
	}
	
	public <V> Set<Attribute<O, V>> getByValue(V value)
	{
		Set<Attribute<O, ?>> attributes = mapValueAttributes.getValuesByKey(value);
		return castAttributeSetToValueType(attributes);
	}
	
	public <V> Set<Attribute<O, V>> getByKeyValue(AttributeKey<V> key, V value)
	{
		return SetHelper.
				withOrig(getByKey(key))
				.intersection(getByValue(value))
				.getSame();
	}
	
	public Set<Attribute<O, ?>> getAll()
	{
		return mapObjectAttributes.getValueSet();
	}
}
