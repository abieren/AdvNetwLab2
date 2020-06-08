package advanced_networking_lab.exercise5.utils;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class SetHelper<T> 
{
	private Set<Object> set;
	
	private SetHelper(Set<T> set) 
	{
		this.set = (Set<Object>) set;
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
		return (Set<T>) set;
	}
	
	public Set<T> getCopy()
	{
		return new HashSet<>((Set<T>) set);
	}
	
	public SetHelper<T> union(Set<T> other)
	{
		set.addAll(other);
		return this;
	}
	
	public SetHelper<Object> union2(Set<? extends Object> other)
	{
		set.addAll(other);
		return (SetHelper<Object>) this;
	}
	
	public SetHelper<T> intersection(Set<? extends Object> other)
	{
		set.retainAll(other);
		return this;
	}
	
	public SetHelper<T> subtraction(Set<? extends Object> other)
	{
		set.removeAll(other);
		return this;
	}
	
	public SetHelper<T> symmdiff(Set<T> other)
	{
		Set<Object> intersection = new HashSet<>(set);
		intersection.retainAll(other);
		set.addAll(other);
		set.removeAll(intersection);
		return this;
	}
	
	public SetHelper<Object> symmdiff2(Set<? extends Object> other)
	{
		Set<Object> intersection = new HashSet<>(set);
		intersection.retainAll(other);
		set.addAll(other);
		set.removeAll(intersection);
		return (SetHelper<Object>)this;
	}
}
