package advanced_networking_lab.exercise5;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetHelper<T> 
{
	private Set<T> set;
	
	private SetHelper(Set<T> set) 
	{
		this.set = set;
	}
	
	public static <T> SetHelper<T> withOrig(Set<T> set)
	{
		return new SetHelper<>(set);
	}
	
	public static <T> SetHelper<T> withCopy(Collection<T> collection)
	{
		return new SetHelper<>(new HashSet<>(collection));
	}
	
	public Set<T> getSame()
	{
		return set;
	}
	
	public Set<T> getCopy()
	{
		return new HashSet<>(set);
	}
	
	public SetHelper<?> union(Set<T> other)
	{
		set.addAll(other);
		return this;
	}
	
	public SetHelper<T> intersection(Set<T> other)
	{
		set.retainAll(other);
		return this;
	}
	
	public SetHelper<T> subtraction(Set<?> other)
	{
		set.removeAll(other);
		return this;
	}
	
	public SetHelper<?> symmdiff(Set<T> other)
	{
		Set<T> intersection = new HashSet<>(set);
		intersection.retainAll(other);
		set.addAll(other);
		set.removeAll(intersection);
		return this;
	}
}
