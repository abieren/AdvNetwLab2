package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

public class PacketTableAttributes 
{
	// general
	public final static AttributeKey<DatapathId> SWITCH = new AttributeKey<>(DatapathId.class);
	public final static AttributeKey<Integer> TIME_SLOT = new AttributeKey<>(Integer.class);
	public final static AttributeKey<Protocol> PROTOCOL = new AttributeKey<>(Protocol.class);
	
	// physical
	public final static AttributeKey<OFPort> IN_PORT = new AttributeKey<>(OFPort.class);
	public final static AttributeKey<OFPort> OUT_PORT = new AttributeKey<>(OFPort.class);
	
	// ethernet
	public final static AttributeKey<MacAddress> SRC_MAC = new AttributeKey<>(MacAddress.class);
	public final static AttributeKey<MacAddress> DST_MAC = new AttributeKey<>(MacAddress.class);
	
	// arp
	public final static AttributeKey<ArpOp> ARP_OP = new AttributeKey<>(ArpOp.class);
	
	// ip
	public final static AttributeKey<IPv4Address> SRC_IP = new AttributeKey<>(IPv4Address.class);
	public final static AttributeKey<IPv4Address> DST_IP = new AttributeKey<>(IPv4Address.class);
	
	// icmp
	public final static AttributeKey<IcmpOp> ICMP_OP = new AttributeKey<>(IcmpOp.class);
}