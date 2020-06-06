package advanced_networking_lab.exercise5;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.management.RuntimeErrorException;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.PacketType;

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
import net.floodlightcontroller.packet.IPv4;


public class Exercise5
	implements IFloodlightModule, IOFMessageListener
{
	private IFloodlightProviderService floodlightProvider;
	
	// stores all flows
	private ObjectAttributeStore<FlowOnSwitch> flowTable = new ObjectAttributeStore<>();
	// stores all packet in messages
	private ObjectAttributeStore<OFPacketIn> packetTable = new ObjectAttributeStore<>();
	int currentTimeSlot;
	
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
	public Command receive(IOFSwitch sw, OFMessage msg, FloodlightContext cntx) {
		// messages to receive: packet in, port status
		// distinguish type of message an then handle it
		
		System.out.println(sw.getOFFactory().getClass().getCanonicalName());
		
		if (msg instanceof OFPacketIn)
		{
			handlePacketIn(sw, (OFPacketIn)msg, cntx);
		}
		else if (msg instanceof OFPortStatus)
		{
			handlePortStatus(sw, (OFPortStatus)msg, cntx);
		}
		else
		{
			handleUnexpectedMessage(sw, msg, cntx);
		}
		
		return Command.CONTINUE;
	}
	
	private void handlePacketIn(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		// requirements:
		// allow: ICMP -> create flows with output action for ARP and ICMP
		// deny: every other protocol -> create flows with drop action for any non ARP and ICMP packet
		
		OutputPrinter.printPacketIn(sw, msg, cntx);
		
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
		
		// distinguish protocol of received packet and handle it
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		if (eth.getEtherType() == EthType.ARP)
		{
			handlePacketInArp(sw, msg, cntx);
		}
		else if (eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ip = (IPv4) eth.getPayload();
			if (ip.getProtocol() == IpProtocol.ICMP)
			{
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
	
	private void handlePacketInArp(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		DatapathId switchId = sw.getId();
		OFPort inPort = msg.getInPort();
		Ethernet eth = IFloodlightProviderService.bcStore.
				get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		MacAddress srcMac = eth.getSourceMACAddress();
		MacAddress dstMac = eth.getDestinationMACAddress();
		ARP arp = (ARP)eth.getPayload();
		ArpOpcode opcode = arp.getOpCode();		
		
		if (opcode.equals(ArpOpcode.REQUEST))
		{
			boolean createflow = true; 
			// try to backward learn			
			if (dstMac.isBroadcast()) 
			{
				OutputPrinter.println("ARP: dstMac is broadcast. No flow creation possible.");
				createflow = false; 
			}
			
			OFPacketIn matchingPacket = null;
			if (createflow)
			{
				Set<OFPacketIn> packetsWithArp = packetTable
						.getByKeyValue(PacketTableAttributes.PROTOCOL, Protocol.ARP)
						.stream()
						.map(x->x.object)
						.collect(Collectors.toSet());
				
				Set<OFPacketIn> packetsWithBroadcast = packetTable
						.getByKeyValue(PacketTableAttributes.SRC_MAC, dstMac)
						.stream()
						.map(x->x.object)
						.collect(Collectors.toSet());
				
				Set<OFPacketIn> matchingPackets = SetHelper
						.withOrig(packetsWithArp)
						.intersection(packetsWithBroadcast)
						.getSame();
				
				if (matchingPackets.isEmpty()) createflow = false;
				else matchingPacket = matchingPackets.iterator().next();
			}
			
			if (createflow)
			{
				// if backward learning possible
				// TODO create flow
				OFPort flowInPort = inPort;
				OFPort flowOutPort = packetTable.get(matchingPacket, PacketTableAttributes.IN_PORT).value;
				MacAddress flowSrcMac = srcMac;
				MacAddress flowDstMac = packetTable.get(matchingPacket, PacketTableAttributes.SRC_MAC).value;
				Pair<OFFlowAdd, String> flow = MessageBuilderV13.buildFlowAddArpRequest(
						sw, flowInPort, flowOutPort, flowSrcMac, flowDstMac);
				OutputPrinter.println(flow.getValue());
				// TODO print
				sw.write(flow.getKey());
				// reinsert packet
				OFMessage reinsert = MessageBuilderV13.buildPacketOutReinsertPacketIn(sw, msg);
				// TODO print
				sw.write(reinsert);
			}
			else
			{
				// if no backward learning possible
				// flood
				
				OFPacketOut flood = MessageBuilderV13.buildPacketOutFlood(msg);
				sw.write(flood);
			}
			
			
			
			
			// save to packet table
			
		}
		else if (opcode.equals(ArpOpcode.REPLY))
		{
			// try to backward learn
			
			// if backward learning possible
			// create flow
			// reinsert packet
			
			// if no backward learning possible
			// flood
			
			// save to packet table
		}
		else
		{
			throw new RuntimeException("Unexpected ARP Operation");			
		}
				
		System.out.println("handlePacketInARP");
	}
	
	private void handlePacketInARPRequest(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		
	}
	
	private void handlePacketInARPReply(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		
	}
	
	private void rememberPacketInARP()
	{
		
	}
	
	private void handlePacketInIcmp(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		System.out.println("handlePacketInICMP");
	}
	
	private void handlePacketInOtherProtocol(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		//ignore for now
		//System.out.println("handlePacketInOtherProtocol");
	}
	
	private void handlePortStatus(IOFSwitch sw, OFPortStatus msg, FloodlightContext cntx)
	{
		OutputPrinter.printPortStatus(sw, msg, cntx);
	}
	
	private void handleUnexpectedMessage(IOFSwitch sw, OFMessage msg, FloodlightContext cntx)
	{
		throw new RuntimeException("Unexpected OFMessage");
	}
	
	private void handleNewFlows(IOFSwitch sw, OFMessage msg, OFPacketIn packetIn)
	{
		// tcp
		sw.write(createFlowTCP(sw, msg));
		// arp
		sw.write(createFlowARP(sw, msg));
		// icmp
		sw.write(createFlowARP(sw, msg));
		// default
		sw.write(createFlowDropOnDefault(sw, msg));
		
		// reinsert packet, such that switch processes it again
		List<OFAction> actions = new ArrayList<>();
		actions.add(sw.getOFFactory().actions().buildOutput().setPort(OFPort.TABLE).build());
		
		OFPort inPort = packetIn.getVersion().compareTo(OFVersion.OF_12) < 0 ? packetIn.getInPort() : packetIn.getMatch().get(MatchField.IN_PORT);
		
		sw.write(sw.getOFFactory().buildPacketOut()
				.setBufferId(packetIn.getBufferId())
				.setData(packetIn.getData())
				.setActions(actions)
				.setInPort(inPort)
				.build()
		);
		
		System.out.println("\nreinsert #####################\n");
	}
	
	private OFMessage createFlowTCP(IOFSwitch sw, OFMessage msg)
	{
		OFFactory factory = sw.getOFFactory();
		
		Match match = factory.buildMatchV3()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.TCP)
				.build();
		
		List<OFAction> actions = new ArrayList<>();
		// flood
		actions.add(factory.actions().buildOutput().setPort(OFPort.FLOOD).build());
		
		OFFlowAdd flow = factory.buildFlowAdd()
				.setActions(actions)
				.setPriority(1)
				.build();
		
		return flow;
	}
	
	private OFMessage createFlowARP(IOFSwitch sw, OFMessage msg)
	{
		OFFactory factory = sw.getOFFactory();
		
		Match match = factory.buildMatchV3()
				.setExact(MatchField.ETH_TYPE, EthType.ARP)
				.build();
		
		List<OFAction> actions = new ArrayList<>();
		// flood
		actions.add(factory.actions().buildOutput().setPort(OFPort.FLOOD).build());
		
		OFFlowAdd flow = factory.buildFlowAdd()
				.setActions(actions)
				.setPriority(1)
				.build();
		
		return flow;
	}
	
	private OFMessage createFlowICMP(IOFSwitch sw, OFMessage msg)
	{
		OFFactory factory = sw.getOFFactory();
		
		Match match = factory.buildMatchV3()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
				.build();
		
		List<OFAction> actions = new ArrayList<>();
		// flood
		actions.add(factory.actions().buildOutput().setPort(OFPort.FLOOD).build());
		
		OFFlowAdd flow = factory.buildFlowAdd()
				.setActions(actions)
				.setPriority(1)
				.build();
		
		return flow;
	}

	private OFMessage createFlowDropOnDefault(IOFSwitch sw, OFMessage msg)
	{
		OFFactory factory = sw.getOFFactory();
		
		List<OFAction> actions = new ArrayList<>();
		
		// flood
		actions.add(factory.actions().buildOutput().setPort(OFPort.FLOOD).build());
		
		OFFlowAdd flow = factory.buildFlowAdd()
				.setActions(actions)
				.setPriority(0)
				.build();
		
		return flow;
	}
	
	private OFMessage createHubPacketOut(IOFSwitch sw, OFMessage msg)
	{
		OFPacketIn pi = (OFPacketIn) msg;
        OFPacketOut.Builder pob = sw.getOFFactory().buildPacketOut();
        
        // get in port
        OFPort inPort = pi.getVersion().compareTo(OFVersion.OF_12) < 0 ? pi.getInPort() : pi.getMatch().get(MatchField.IN_PORT);
        
        pob.setBufferId(pi.getBufferId())
        	.setXid(pi.getXid())
        	.setInPort(inPort);
        
        // set actions
        OFActionOutput.Builder actionBuilder = sw.getOFFactory().actions().buildOutput();
        actionBuilder.setPort(OFPort.FLOOD);
        pob.setActions(Collections.singletonList(actionBuilder.build()));

        // set data if it is included in the packetin
        //if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
            byte[] packetData = pi.getData();
            pob.setData(packetData);
        //}
        return pob.build();
	}
}
