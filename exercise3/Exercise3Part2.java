package advanced_networking_lab.exercise3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFMatchV3.Builder;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.OFBufferId;
import org.projectfloodlight.openflow.types.OFPort;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFMessageListener;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.core.module.FloodlightModuleContext;
import net.floodlightcontroller.core.module.FloodlightModuleException;
import net.floodlightcontroller.core.module.IFloodlightModule;
import net.floodlightcontroller.core.module.IFloodlightService;
import net.floodlightcontroller.packet.ARP;
import net.floodlightcontroller.packet.Data;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.packet.ICMP;
import net.floodlightcontroller.packet.IPacket;
import net.floodlightcontroller.packet.IPv4;
import net.floodlightcontroller.packet.TCP;
import net.floodlightcontroller.util.FlowModUtils;


public class Exercise3Part2 
	implements IFloodlightModule, IOFMessageListener
{
	private IFloodlightProviderService floodlightProvider;
	
	
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
		System.out.println(String.format("%s init", Exercise3Part2.class.getName()));
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		System.out.println(String.format("%s startup", Exercise3Part2.class.getName()));
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public String getName() {
		return Exercise3Part2.class.getPackage().getName();
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
	public Command receive(IOFSwitch sw, OFMessage msg,
			FloodlightContext cntx) {
		// hier kommt der Hauptteil der Implementierug
		OFPacketIn packetIn = (OFPacketIn) msg;
		Ethernet eth = IFloodlightProviderService.bcStore.get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		IPacket packetL3 = eth.getPayload();
		
		System.out.println(packetL3.getClass().getName());
		if (packetL3.getPayload() != null) System.out.println(packetL3.getPayload().getClass().getName());
		
		if (eth.getEtherType() == EthType.ARP)
		{
			createNewFlows(sw, msg, packetIn);
		}
		else if (eth.getEtherType() == EthType.IPv4)
		{	
			IPv4 ip = (IPv4) eth.getPayload();
			if (ip.getProtocol() == IpProtocol.ICMP)
			{
				createNewFlows(sw, msg, packetIn);
			}
			else if (ip.getProtocol() == IpProtocol.TCP)
			{
				createNewFlows(sw, msg, packetIn);
			}
			else
			{
				System.out.println("do nothing");
			}
		}
		else
		{
			System.out.println("do nothing");
		}
		
		
		
		System.out.println(String.format("%s receive", Exercise3Part2.class.getName()));
		return Command.CONTINUE;
	}
	
	private void createNewFlows(IOFSwitch sw, OFMessage msg, OFPacketIn packetIn)
	{
		// tcp
		sw.write(createFlowTCP(sw, msg));
		// arp
		sw.write(createFlowARP(sw, msg));
		// icmp
		sw.write(createFlowICMP(sw, msg));
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
}
