package advanced_networking_lab.exercise5.utils;

public class AttributeKey<V>
{
	public final Class<V> clazz;
	
	public AttributeKey(Class<V> clazz)
	{
		this.clazz = clazz;
	}
}