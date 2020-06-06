package advanced_networking_lab.exercise5;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.projectfloodlight.openflow.types.OFPort;

public class PortTable<T> 
{
	private HashMap<OFPort, List<T>> mapPortToValues = new HashMap<>();
	private HashMap<T, OFPort> mapValueToPort = new HashMap<>();
	
	public void set(OFPort port, T value)
	{		
		// insert into mapPortToValue
		if (!mapPortToValues.containsKey(port))
		{
			mapPortToValues.put(port, new ArrayList<T>());
		}
		List<T> entryList = mapPortToValues.get(port);
		if (!entryList.contains(value))
		{
			entryList.add(value);
		}
		// insert into mapValueToPort
		if (!mapValueToPort.containsKey(value)) 
		{
			mapValueToPort.put(value, port);
		}
	}
	
	public OFPort getPortByValue(T value)
	{
		return mapValueToPort.getOrDefault(value, null);
	}
	
	public List<T> getValuesByPort(OFPort port)
	{
		return mapPortToValues.getOrDefault(port, new ArrayList<>());
	}
}
