package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import advanced_networking_lab.exercise5.utils.AttributeKey;

public class PacketTableAttributes 
{
	// general
	public final static AttributeKey<Integer> TIME_SLOT = new AttributeKey<>(Integer.class);
	public final static AttributeKey<DatapathId> SWITCH = new AttributeKey<>(DatapathId.class);
	
	public final static AttributeKey<OFPort> IN_PORT = new AttributeKey<>(OFPort.class);
	public final static AttributeKey<OFPort> OUT_PORT = new AttributeKey<>(OFPort.class);
	
	// specific to arp
	public final static AttributeKey<MacAddress> SRC_MAC = new AttributeKey<>(MacAddress.class);
	public final static AttributeKey<MacAddress> DST_MAC = new AttributeKey<>(MacAddress.class);
	public final static AttributeKey<EthType> ETH_TYPE = new AttributeKey<>(EthType.class);
	
	public final static AttributeKey<ArpOpcode> ARP_OPCODE = new AttributeKey<>(ArpOpcode.class);
	
	// specific to icmp
	public final static AttributeKey<IPv4Address> SRC_IP = new AttributeKey<>(IPv4Address.class);
	public final static AttributeKey<IPv4Address> DST_IP = new AttributeKey<>(IPv4Address.class);
	public final static AttributeKey<IpProtocol> IP_PROTOCOL = new AttributeKey<>(IpProtocol.class);
	
	public final static AttributeKey<ICMPv4Type> ICMP_TYPE = new AttributeKey<>(ICMPv4Type.class);
}