package advanced_networking_lab.exercise5;

import java.util.Collections;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowDelete;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPacketOut.Builder;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.IpProtocol;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;
import org.projectfloodlight.openflow.types.U64;

import javafx.util.Pair;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.util.OFMessageUtils;

public class MessageBuilderV13
{
	public static final OFFactory ofFactory = new OFFactoryVer13();
	// use this cookie to select all flows made by this application
	public static final U64 COOKIE = U64.of(1337);
	
	public static String buildPacketOutLogOutput(IOFSwitch sw, OFPort outPort)
	{
		long switchNumber = sw.getId().getLong();
        String output = String.format("------->Packet Out Operation: switchNumber=%d, outPortNumber=%s",
        		switchNumber, outPort.toString());
        return output;
	}
	
	public static Pair<OFPacketOut, String> buildPacketOut(IOFSwitch sw, OFPacketIn packetIn, OFPort outPort)
	{		
		Builder packetOutBuilder = ofFactory.buildPacketOut()        
				.setXid(packetIn.getXid())
				.setBufferId(packetIn.getBufferId())
				.setInPort(OFMessageUtils.getInPort(packetIn))
				.setActions(Collections.singletonList(ActionBuilderV13.buildOutput(outPort)));
        
        // set data if it is included in the packetin
        //if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
            byte[] packetData = packetIn.getData();
            packetOutBuilder.setData(packetData);
        //}
            
        String output = buildPacketOutLogOutput(sw, outPort);
        
        return new Pair<>(packetOutBuilder.build(), output);	
	}
	
	public static Pair<OFPacketOut, String> buildPacketOutReinsertPacketIn(IOFSwitch sw, OFPacketIn packetIn)
	{
		OFPort outPort = OFPort.TABLE;
		OFPacketOut packetOut = ofFactory.buildPacketOut()
				.setBufferId(packetIn.getBufferId())
				.setInPort(OFMessageUtils.getInPort(packetIn))
				.setData(packetIn.getData())
				// this action causes the packet to be processed again
				.setActions(Collections.singletonList(ActionBuilderV13.buildOutput(outPort)))
				.build();
		
		String output = buildPacketOutLogOutput(sw, outPort);
		
		return new Pair<>(packetOut, output);
	}
	
	private static String buildFlowModLogOutput(IOFSwitch sw, OFPort outPort, OFFlowMod flow)
	{
		long switchNumber = sw.getId().getLong();
		
		StringBuilder sb = new StringBuilder();
		Match match = flow.getMatch();
		for (MatchField<?> matchField : flow.getMatch().getMatchFields())
		{
			sb.append(matchField.getName());
			sb.append(":");
			sb.append(match.get(matchField).toString());
			sb.append(" ");
		}

		String output = String.format("------->Flow Mod Operation: switchNumber=%d, outPortNumber=%s, matchFields=%s",
				switchNumber, outPort.toString(), sb.toString());
		return output;
	}

	public static Pair<OFFlowAdd, String> buildFlowAddArp(IOFSwitch sw, 
			OFPort inPort, OFPort outPort,
			MacAddress srcMac, MacAddress dstMac,
			ArpOpcode arpOpcode) 
	{
		Match match = ofFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.ARP)
				.setExact(MatchField.IN_PORT, inPort)
				.setExact(MatchField.ETH_SRC, srcMac)
				.setExact(MatchField.ETH_DST, dstMac)
				.setExact(MatchField.ARP_OP, arpOpcode)
				.build();
		
		List<OFAction> actions = Collections.singletonList(
				ActionBuilderV13.buildOutput(outPort));
		
		OFFlowAdd flowAdd = ofFactory.buildFlowAdd()
				.setCookie(COOKIE) // use this cookie to select this flow
				.setIdleTimeout(0) // never delete through timeout
				.setHardTimeout(0) // never delete through timeout
				.setPriority(10)
				.setMatch(match)
				.setActions(actions)
				.build();
		
		String output = buildFlowModLogOutput(sw, outPort, flowAdd);
		
		return new Pair<>(flowAdd, output);
	}

	public static Pair<OFFlowAdd, String> buildFlowAddIcmp(IOFSwitch sw,
			OFPort inPort, OFPort outPort, 
			IPv4Address srcIp, IPv4Address dstIp, 
			ICMPv4Type icmpType) 
	{
		Match match = ofFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.IPv4)
				.setExact(MatchField.IP_PROTO, IpProtocol.ICMP)
				.setExact(MatchField.IN_PORT, inPort)
				.setExact(MatchField.IPV4_SRC, srcIp)
				.setExact(MatchField.IPV4_DST, dstIp)
				.setExact(MatchField.ICMPV4_TYPE, icmpType)
				.build();
		
		List<OFAction> actions = Collections.singletonList(
				ActionBuilderV13.buildOutput(outPort));
		
		OFFlowAdd flowAdd = ofFactory.buildFlowAdd()
				.setCookie(COOKIE) // use this cookie to select this flow
				.setIdleTimeout(0) // never delete through timeout
				.setHardTimeout(0) // never delete through timeout
				.setPriority(10)
				.setMatch(match)
				.setActions(actions)
				.build();
		
		String output = buildFlowModLogOutput(sw, outPort, flowAdd);
		
		return new Pair<>(flowAdd, output);
	}
	
	private static String buildFlowDeleteLogOutput(IOFSwitch sw, OFFlowDelete flow)
	{
		long switchNumber = sw.getId().getLong();
		
		StringBuilder sb = new StringBuilder();
		Match match = flow.getMatch();
		for (MatchField<?> matchField : flow.getMatch().getMatchFields())
		{
			sb.append(matchField.getName());
			sb.append(":");
			sb.append(match.get(matchField).toString());
			sb.append(" ");
		}

		String output = String.format("------->Flow Del Operation: switchNumber=%d, matchFields=%s",
				switchNumber, sb.toString());
		return output;
	}
	
	public static Pair<OFFlowDelete, String> buildFlowDeleteAllFlows(IOFSwitch sw) 
	{
		
		OFFlowDelete flowDelete = ofFactory.buildFlowDelete()
				.setCookie(COOKIE)
				.setCookieMask(COOKIE)
				.build();
		
		String output = buildFlowDeleteLogOutput(sw, flowDelete);
		
		return new Pair<>(flowDelete, output);
	}
}
