package advanced_networking_lab.exercise5;

import java.util.Set;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

public class OldFlowTable
{
	// TODO work with attributes instead: Attribute<T>
	// switch gets identified by its datapath id
	private final BiMapSet<DatapathId, FlowOnSwitch> mapSwitchFlow = new BiMapSet<>();
	private final BiMapSet<OFPort, FlowOnSwitch> mapSrcPortFlow = new BiMapSet<>();
	private final BiMapSet<OFPort, FlowOnSwitch> mapDstPortFlow = new BiMapSet<>();
	private final BiMapSet<MacAddress, FlowOnSwitch> mapSrcMacFlow = new BiMapSet<>();
	private final BiMapSet<MacAddress, FlowOnSwitch> mapDstMacFlow = new BiMapSet<>();
	private final BiMapSet<IPv4Address, FlowOnSwitch> mapSrcIpFlow = new BiMapSet<>();
	private final BiMapSet<IPv4Address, FlowOnSwitch> mapDstIpFlow = new BiMapSet<>();
	private final BiMapSet<Protocol, FlowOnSwitch> mapProtocolFlow = new BiMapSet<>();
	
	public Set<FlowOnSwitch> getFlowsBySwitch(DatapathId sw)
	{
		return mapSwitchFlow.getValuesByKey(sw);
	}
	
	public Set<DatapathId> getSwitchesByFlow(FlowOnSwitch flow)
	{
		return mapSwitchFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsBySrcPort(OFPort port)
	{
		return mapSrcPortFlow.getValuesByKey(port);
	}
	
	public Set<OFPort> getSrcPortsByFlow(FlowOnSwitch flow)
	{
		return mapSrcPortFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsByDstPort(OFPort port)
	{
		return mapDstPortFlow.getValuesByKey(port);
	}
	
	public Set<OFPort> getDstPortsByFlow(FlowOnSwitch flow)
	{
		return mapDstPortFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsBySrcMac(MacAddress mac)
	{
		return mapSrcMacFlow.getValuesByKey(mac);
	}
	
	public Set<MacAddress> getSrcMacByFlow(FlowOnSwitch flow)
	{
		return mapSrcMacFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsByDstMac(MacAddress mac)
	{
		return mapDstMacFlow.getValuesByKey(mac);
	}
	
	public Set<MacAddress> getDstMacByFlow(FlowOnSwitch flow)
	{
		return mapDstMacFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsBySrcIp(IPv4Address ip)
	{
		return mapSrcIpFlow.getValuesByKey(ip);
	}
	
	public Set<IPv4Address> getSrcIpByFlow(FlowOnSwitch flow)
	{
		return mapSrcIpFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsByDstIp(IPv4Address ip)
	{
		return mapDstIpFlow.getValuesByKey(ip);
	}
	
	public Set<IPv4Address> getDstIpByFlow(FlowOnSwitch flow)
	{
		return mapDstIpFlow.getKeysByValue(flow);
	}
	
	public Set<FlowOnSwitch> getFlowsByProtocol(Protocol protocol)
	{
		return mapProtocolFlow.getValuesByKey(protocol);
	}
	
	public Set<Protocol> getProtocolByFlow(FlowOnSwitch flow)
	{
		return mapProtocolFlow.getKeysByValue(flow);
	}
}