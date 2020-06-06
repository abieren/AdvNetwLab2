package advanced_networking_lab.exercise5;

import java.util.Objects;

public class Attribute<O, V>
{
	public final O object;
	public final AttributeKey<V> key;
	public final V value;
	
	public Attribute(O object, AttributeKey<V> key, V value) 
	{
		this.object = object;
		this.key = key;
		this.value = value;
	}
	
	@Override
	public int hashCode() 
	{
		return Objects.hash(object, key, value);
	}
	
	@Override
	public boolean equals(Object obj) 
	{
		if (obj == this) return true;
		if (!(obj instanceof Attribute)) return false;
		Attribute o = (Attribute) obj;
		return Objects.equals(object, o.object) && 
				Objects.equals(key, o.key) &&
				Objects.equals(value, o.value);
	}
}
