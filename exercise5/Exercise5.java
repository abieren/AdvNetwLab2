package advanced_networking_lab.exercise5;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPortDesc;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import advanced_networking_lab.exercise5.utils.Attribute;
import advanced_networking_lab.exercise5.utils.AttributeStore;
import advanced_networking_lab.exercise5.utils.SetHelper;
import javafx.util.Pair;
import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.util.OFMessageUtils;


public class Exercise5
	implements IFloodlightModule, IOFMessageListener
{
	private IFloodlightProviderService floodlightProvider;
	
	
	// stores all flows
	private AttributeStore<FlowOnSwitch> flowTable = new AttributeStore<>();
	// stores all packet in messages
	private AttributeStore<OFPacketIn> packetTable = new AttributeStore<>();
	int currentTimeSlot = 0;
	
	private TopologyManager topologyManager = new TopologyManager();
	
	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleServices() {
		// We don't provide any services, return null
        return null;
	}

	@Override
	public Map<Class<? extends IFloodlightService>, IFloodlightService> getServiceImpls() {
		// We don't provide any services, return null
        return null;
	}

	@Override
	public Collection<Class<? extends IFloodlightService>> getModuleDependencies() {
        Collection<Class<? extends IFloodlightService>> l = 
                new ArrayList<Class<? extends IFloodlightService>>();
        l.add(IFloodlightProviderService.class);
        return l;
	}

	@Override
	public void init(FloodlightModuleContext context) throws FloodlightModuleException {
		System.out.println(String.format("%s init", Exercise5.class.getName()));
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	}
	
	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		System.out.println(String.format("%s startup", Exercise5.class.getName()));
		
		// messages to receive: packet in, port status
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
		floodlightProvider.addOFMessageListener(OFType.PORT_STATUS, this);
	}

	@Override
	public String getName() {
		return Exercise5.class.getPackage().getName();
	}

	@Override
	public boolean isCallbackOrderingPrereq(OFType type, String name) {
		return false;
	}

	@Override
	public boolean isCallbackOrderingPostreq(OFType type, String name) {
		return false;
	}

	
	@Override
	public synchronized Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// messages to receive: packet in, port status
		// distinguish type of message an then handle it
		
		// remember switch
		topologyManager.rememberSwitch(sw);
		
		if (msg instanceof OFPacketIn)
		{
			handlePacketIn(sw, (OFPacketIn)msg, cntx);
		}
		else if (msg instanceof OFPortStatus)
		{
			System.out.println("////////////////////////////////");
			handlePortStatus(sw, (OFPortStatus)msg, cntx);
		}
		else
		{
			System.out.println("////////////////////////////////");
			handleUnexpectedMessage(sw, msg, cntx);
		}
		
		return Command.CONTINUE;
	}
	
	private void handlePacketIn(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		// requirements:
		// allow: ICMP -> create flows with output action for ARP and ICMP
		// deny: every other protocol -> create flows with drop action for any non ARP and ICMP packet		
		
		// distinguish protocol of received packet and handle it
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if (eth.getEtherType() == EthType.IPv6)
		{
			// ignore ipv6 traffic
			return;
		}
		else
		{			
			// TODO add back in
			//updateTimeSlot()
			
			if (eth.getEtherType() == EthType.ARP)
			{
				OutputPrinter.printPacketIn(sw, msg, cntx);
				handlePacketInArp(sw, msg, cntx);
			}
			else if (eth.getEtherType() == EthType.IPv4)
			{	
				IPv4 ip = (IPv4) eth.getPayload();
				if (ip.getProtocol() == IpProtocol.ICMP)
				{
					OutputPrinter.printPacketIn(sw, msg, cntx);
					handlePacketInIcmp(sw, msg, cntx);
				}
				else
				{
					handlePacketInOtherProtocol(sw, msg, cntx);
				}
			}
			else
			{
				handlePacketInOtherProtocol(sw, msg, cntx);
			}
		}
	}
	
	private void updateTimeSlot()
	{
		// update time
		Calendar calendar = GregorianCalendar.getInstance();
		calendar.setTime(new Date());
		currentTimeSlot = calendar.get(Calendar.MINUTE);
		currentTimeSlot -= currentTimeSlot / 5; // round to nearest 5 minutes
		
		// delete old time slots of packetTable
		int previousTimeSlot = (12 + currentTimeSlot - 1) % 12;
		// select all time slots that don't lie within current or previous time slot		
		Set<Attribute<OFPacketIn, Integer>> expiredTimeSlots = SetHelper
				.withOrig(packetTable.getByKey(PacketTableAttributes.TIME_SLOT))
				.subtraction(packetTable.getByKeyValue(PacketTableAttributes.TIME_SLOT, currentTimeSlot))
				.subtraction(packetTable.getByKeyValue(PacketTableAttributes.TIME_SLOT, previousTimeSlot))
				.getSame();
		// delete all attributes of affected flows. A flow without any attribute ceases to exist.
		for (Attribute<OFPacketIn, Integer> expiredSlot : expiredTimeSlots)
		{
			Set<Attribute<OFPacketIn, ?>> expiredFlowAttributes = packetTable.getByObject(expiredSlot.object);
			for (Attribute<OFPacketIn, ?> expiredFlowAttribute: expiredFlowAttributes)
			{
				packetTable.remove(expiredFlowAttribute.object, expiredFlowAttribute.key);
			}
		}
	}
	
	private void handlePacketInArp(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		DatapathId switchId = sw.getId();
		OFPort inPort = OFMessageUtils.getInPort(msg);
		Ethernet eth = IFloodlightProviderService.bcStore.
				get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();
		ARP arp = (ARP)eth.getPayload();
		ArpOpcode arpOpcode = arp.getOpCode();
		IPv4Address arpSenderIp = arp.getSenderProtocolAddress();
		IPv4Address arpTargetIp = arp.getTargetProtocolAddress();
		
		rememberPacketInArp(msg, switchId, inPort, srcMac, dstMac, arpOpcode, arpSenderIp, arpTargetIp);

		// check for expected arp opcodes
		if (arpOpcode.equals(ArpOpcode.REQUEST)) 
		{
			OutputPrinter.println(sw, String.format(">>>>>>>>>> ARP Request: %s -> %s", 
					srcMac.toString(), dstMac.toString()));
		}
		else if (arpOpcode.equals(ArpOpcode.REPLY)) 
		{
			OutputPrinter.println(sw, String.format("<<<<<<<<<< ARP Reply: %s -> %s", 
					srcMac.toString(), dstMac.toString()));
		}
		else
		{
			throw new RuntimeException("Unexpected ARP Operation");
		}
		
		// try to backward learn			
		boolean ableToCreateflow = true;
		
		if (dstMac.isBroadcast()) 
		{
			OutputPrinter.println(sw, "ARP: dstMac is broadcast. No flow creation possible.");
			ableToCreateflow = false;  
		}
		
		OFPacketIn matchingPacket = null;
		if (ableToCreateflow)
		{
			Set<OFPacketIn> packetsWithArp = packetTable
					.getByKeyValue(PacketTableAttributes.ETH_TYPE, EthType.ARP)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsWithDstMacAsSrcMac = packetTable
					.getByKeyValue(PacketTableAttributes.SRC_MAC, dstMac)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS1 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(1))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS2 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(2))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS3 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(3))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> matchingPackets = SetHelper
					.withOrig(packetsWithArp)
					.intersection(packetsWithDstMacAsSrcMac)
					.subtraction(packetsFromS1) // only get packets that originate from hosts
					.subtraction(packetsFromS2) // only get packets that originate from hosts
					.subtraction(packetsFromS3) // only get packets that originate from hosts
					.getSame();
			
			if (matchingPackets.isEmpty()) 
			{
				OutputPrinter.println(sw, "ARP: dont create flow. unknown packet port destination.");
				ableToCreateflow = false;
			}
			else 
			{
				matchingPacket = matchingPackets.iterator().next();
			}
		}
		
		// if backward learning possible: create flow
		if (ableToCreateflow)
		{
			OutputPrinter.println(sw, "ARP: create flow.");
			
			DatapathId matchedPacketSwitchId = packetTable.get(matchingPacket, PacketTableAttributes.SWITCH).value;
			OFPort matchedPacketInPort = packetTable.get(matchingPacket, PacketTableAttributes.IN_PORT).value;
			topologyManager.createARPFlow(sw,
					switchId, inPort, srcMac,
					matchedPacketSwitchId, matchedPacketInPort, dstMac,
					arpOpcode);
			
			// reinsert packet to not lose it
			Pair<OFPacketOut, String> reinsert = MessageBuilderV13.buildPacketOutReinsertPacketIn(sw, msg);
			OutputPrinter.println(sw, reinsert.getValue());
			sw.write(reinsert.getKey());
		}
		else
		{
			// flood
			// before flooding check if this particular message has already been seen on this switch
			// with same arp opcode, src mac, dst mac, arp sender ip, arp target ip
			// and also arrives from a switch.
			Set<OFPacketIn> switches = packetTable
					.getByKeyValue(PacketTableAttributes.SWITCH, switchId)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> arpOpcodes = packetTable
					.getByKeyValue(PacketTableAttributes.ARP_OPCODE, arpOpcode)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
					
			Set<OFPacketIn> srcMacs = packetTable
					.getByKeyValue(PacketTableAttributes.SRC_MAC, srcMac)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> dstMacs = packetTable
					.getByKeyValue(PacketTableAttributes.DST_MAC, dstMac)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> arpSenderIps = packetTable
					.getByKeyValue(PacketTableAttributes.ARP_SENDER_IP, arpSenderIp)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> arpTargetIps = packetTable
					.getByKeyValue(PacketTableAttributes.ARP_TARGET_IP, arpTargetIp)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsWithInPortsIsSwitch = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT_IS_SWITCH, true)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> alreadySeen = SetHelper
					.withOrig(arpOpcodes)
					.intersection(switches)
					.intersection(srcMacs)
					.intersection(dstMacs)
					.intersection(arpSenderIps)
					.intersection(arpTargetIps)
					.intersection(packetsWithInPortsIsSwitch) // get packets that came from one of the switches
					.getSame();			
			
			if (alreadySeen.size() < 2)
			{
				OutputPrinter.println(sw, "ARP: flood.");
				
				Pair<OFPacketOut, String> flood = MessageBuilderV13.buildPacketOut(sw, msg, OFPort.FLOOD);
				OutputPrinter.println(sw, flood.getValue());
				sw.write(flood.getKey());
			}
			else
			{
				OutputPrinter.println(sw, "ARP: dont flood. packet is known.");
				
				synchronized(this){
				System.out.println("[-- ");
				System.out.println(sw.getId().toString());
				System.out.println(alreadySeen.size());
				System.out.println(packetTable.getByKey(PacketTableAttributes.TIME_SLOT).size());
				System.out.println(alreadySeen.iterator().next() == msg);
				System.out.println("--]");}
				
				if (inPort.getPortNumber() > 3 && arpOpcode.getOpcode() == ArpOpcode.REPLY.getOpcode())
				{
					throw new RuntimeException("ARP: UNEXPECTED CASE. PACKET SHOULD NOT BE KNOWN.");
				}
			}
		}
	}
	
	private void rememberPacketInArp(OFPacketIn msg, DatapathId switchId, OFPort inPort, MacAddress srcMac, MacAddress dstMac,
			ArpOpcode arpOpcode, IPv4Address arpSenderIp, IPv4Address arpTargetIp)
	{
		packetTable.put(msg, PacketTableAttributes.TIME_SLOT, currentTimeSlot);
		packetTable.put(msg, PacketTableAttributes.SWITCH, switchId);
		packetTable.put(msg, PacketTableAttributes.IN_PORT, inPort);
		packetTable.put(msg, PacketTableAttributes.SRC_MAC, srcMac);
		packetTable.put(msg, PacketTableAttributes.DST_MAC, dstMac);
		packetTable.put(msg, PacketTableAttributes.ETH_TYPE, EthType.ARP);
		packetTable.put(msg, PacketTableAttributes.ARP_OPCODE, arpOpcode);
		// also remember those two to distinguish different arp requests
		packetTable.put(msg, PacketTableAttributes.ARP_SENDER_IP, arpSenderIp);
		packetTable.put(msg, PacketTableAttributes.ARP_TARGET_IP, arpTargetIp);
		// auxiliary attributes to make querying easier
		packetTable.put(msg, PacketTableAttributes.IN_PORT_IS_SWITCH, switchId.getLong() <= 3);
	}
	
	private void handlePacketInIcmp(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		DatapathId switchId = sw.getId();
		OFPort inPort = OFMessageUtils.getInPort(msg);
		Ethernet eth = IFloodlightProviderService.bcStore.
				get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPv4 ip = (IPv4) eth.getPayload();
		IPv4Address srcIp = ip.getSourceAddress();
		IPv4Address dstIp = ip.getDestinationAddress();
		ICMP icmp = (ICMP) ip.getPayload();
		// TODO maybe wrong since byte and short does not match perfectly
		ICMPv4Type icmpType = ICMPv4Type.of(icmp.getIcmpType()); 
		byte sequenceNumber = icmp.getIcmpCode();
		
		rememberPacketInICMP(msg, switchId, inPort, srcIp, dstIp, icmpType);

		// check for expected icmp types
		if (icmpType.equals(ICMPv4Type.ECHO)) 
		{
			OutputPrinter.println(sw, String.format(">>>>>>>>>> ICMP Request: #%d %s -> %s", 
					sequenceNumber, srcIp.toString(), dstIp.toString()));
		}
		else if (icmpType.equals(ICMPv4Type.ECHO_REPLY)) 
		{
			OutputPrinter.println(sw, String.format("<<<<<<<<<< ICMP Reply: #%d %s -> %s", 
					sequenceNumber, srcIp.toString(), dstIp.toString()));
		}
		else
		{
			throw new RuntimeException("Unexpected ICMP Type");
		}
		
		// try to backward learn			
		boolean ableToCreateflow = true;
		
		/*
		// TODO check again if broadcast checking is necessary
		if (dstIp.isBroadcast() || dstIp.) 
		{
			OutputPrinter.println(sw, "ARP: dstMac is broadcast. No flow creation possible.");
			ableToCreateflow = false; 
		}
		*/
		
		OFPacketIn matchingPacket = null;
		if (ableToCreateflow)
		{
			Set<OFPacketIn> packetsWithIcmp = packetTable
					.getByKeyValue(PacketTableAttributes.IP_PROTOCOL, IpProtocol.ICMP)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsWithDstIpAsSrcIp = packetTable
					.getByKeyValue(PacketTableAttributes.SRC_IP, dstIp)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS1 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(1))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS2 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(2))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsFromS3 = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT, OFPort.of(3))
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> matchingPackets = SetHelper
					.withOrig(packetsWithIcmp)
					.intersection(packetsWithDstIpAsSrcIp)
					.subtraction(packetsFromS1) // only get packets that originate from hosts
					.subtraction(packetsFromS2) // only get packets that originate from hosts
					.subtraction(packetsFromS3) // only get packets that originate from hosts
					.getSame();
			
			if (matchingPackets.isEmpty()) 
			{
				OutputPrinter.println(sw, "ICMP: dont create flow. unknown packet port destination.");
				ableToCreateflow = false;
			}
			else 
			{
				matchingPacket = matchingPackets.iterator().next();
			}
		}
		
		// if backward learning possible: create flow
		if (ableToCreateflow)
		{
			OutputPrinter.println(sw, "ICMP: create flow.");
			
			DatapathId matchedPacketSwitchId = packetTable.get(matchingPacket, PacketTableAttributes.SWITCH).value;
			OFPort matchedPacketInPort = packetTable.get(matchingPacket, PacketTableAttributes.IN_PORT).value;
			topologyManager.createICMPFlow(sw,
					switchId, inPort, srcIp,
					matchedPacketSwitchId, matchedPacketInPort, dstIp,
					icmpType);
			
			// reinsert packet to not lose it
			Pair<OFPacketOut, String> reinsert = MessageBuilderV13.buildPacketOutReinsertPacketIn(sw, msg);
			OutputPrinter.println(sw, reinsert.getValue());
			sw.write(reinsert.getKey());
		}
		else
		{
			// flood
			// before flooding check if this particular message has already been seen on this switch
			// with same icmp type, src ip, dst ip and also arrives from a switch.
			Set<OFPacketIn> switches = packetTable
					.getByKeyValue(PacketTableAttributes.SWITCH, switchId)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> icmpTypes = packetTable
					.getByKeyValue(PacketTableAttributes.ICMP_TYPE, icmpType)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
					
			Set<OFPacketIn> srcIps = packetTable
					.getByKeyValue(PacketTableAttributes.SRC_IP, srcIp)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> dstIps = packetTable
					.getByKeyValue(PacketTableAttributes.DST_IP, dstIp)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> packetsWithInPortsIsSwitch = packetTable
					.getByKeyValue(PacketTableAttributes.IN_PORT_IS_SWITCH, true)
					.stream()
					.map(x->x.object)
					.collect(Collectors.toSet());
			
			Set<OFPacketIn> alreadySeen = SetHelper
					.withOrig(icmpTypes)
					.intersection(switches)
					.intersection(srcIps)
					.intersection(dstIps)
					.intersection(packetsWithInPortsIsSwitch) // get packets that came from one of the switches  
					.getSame();			
			
			if (alreadySeen.size() < 2)
			{
				OutputPrinter.println(sw, "ICMP: flood.");
				
				Pair<OFPacketOut, String> flood = MessageBuilderV13.buildPacketOut(sw, msg, OFPort.FLOOD);
				OutputPrinter.println(sw, flood.getValue());
				sw.write(flood.getKey());
			}
			else
			{
				OutputPrinter.println(sw, "ICMP: dont flood. packet is known.");
			}
		}
	}
	
	private void rememberPacketInICMP(OFPacketIn msg, DatapathId switchId, OFPort inPort, IPv4Address srcIp, IPv4Address dstIp, ICMPv4Type icmpType)
	{
		packetTable.put(msg, PacketTableAttributes.TIME_SLOT, currentTimeSlot);
		packetTable.put(msg, PacketTableAttributes.SWITCH, switchId);
		packetTable.put(msg, PacketTableAttributes.IN_PORT, inPort);
		packetTable.put(msg, PacketTableAttributes.SRC_IP, srcIp);
		packetTable.put(msg, PacketTableAttributes.DST_IP, dstIp);
		packetTable.put(msg, PacketTableAttributes.IP_PROTOCOL, IpProtocol.ICMP);
		packetTable.put(msg, PacketTableAttributes.ICMP_TYPE, icmpType);
		// auxiliary attributes to make querying easier
		packetTable.put(msg, PacketTableAttributes.IN_PORT_IS_SWITCH, switchId.getLong() <= 3);
	}
	
	private void handlePacketInOtherProtocol(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		//ignore for now
		//System.out.println("handlePacketInOtherProtocol");
	}
	
	private void handlePortStatus(IOFSwitch sw, OFPortStatus msg, FloodlightContext cntx)
	{
		System.out.println("###################################");
		OutputPrinter.printPortStatus(sw, msg, cntx);
	}
	
	private void handleUnexpectedMessage(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		throw new RuntimeException("Unexpected OFMessage");
	}
}
