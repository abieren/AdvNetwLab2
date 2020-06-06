package advanced_networking_lab.exercise5;

import java.util.Collections;
import java.util.List;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFPacketOut.Builder;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.EthType;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import javafx.util.Pair;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.util.OFMessageUtils;

public class MessageBuilderV13
{
	public static final OFFactory ofFactory = new OFFactoryVer13();
	
	public static Pair<OFPacketOut, String> buildPacketOutFlood(IOFSwitch sw, OFPacketIn packetIn)
	{		
		OFPort outPort = OFPort.FLOOD;
		Builder packetOutBuilder = ofFactory.buildPacketOut()        
				.setXid(packetIn.getXid())
				.setBufferId(packetIn.getBufferId())
				.setInPort(OFMessageUtils.getInPort(packetIn))
				.setActions(Collections.singletonList(ActionBuilderV13.buildOutput(OFPort.FLOOD)));
        
        // set data if it is included in the packetin
        //if (pi.getBufferId() == OFBufferId.NO_BUFFER) {
            byte[] packetData = packetIn.getData();
            packetOutBuilder.setData(packetData);
        //}
            
        String output = buildPacketOutOutput(sw, outPort);
        
        return new Pair<OFPacketOut, String>(packetOutBuilder.build(), output);	
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
		
		String output = buildPacketOutOutput(sw, outPort);
		
		return new Pair<OFPacketOut, String>(packetOut, output);
	}
	
	public static String buildPacketOutOutput(IOFSwitch sw, OFPort outPort)
	{
		long switchNumber = sw.getId().getLong();
        String output = String.format("------->Packet Out Operation: switchNumber=%d, outPortNumber=%s",
        		switchNumber, "OFPort.TABLE");
        return output;
	}

	public static Pair<OFFlowAdd, String> buildFlowAddArpRequest(IOFSwitch sw, OFPort flowInPort,
			OFPort flowOutPort, MacAddress flowSrcMac, MacAddress flowDstMac) 
	{
		Match match = ofFactory.buildMatch()
				.setExact(MatchField.ETH_TYPE, EthType.ARP)
				.setExact(MatchField.IN_PORT, flowInPort)
				.setExact(MatchField.ETH_SRC, flowSrcMac)
				.setExact(MatchField.ETH_DST, flowDstMac)
				.build();
		
		List<OFAction> actions = Collections.singletonList(
				ActionBuilderV13.buildOutput(flowOutPort));
		
		OFFlowAdd flowAdd = ofFactory.buildFlowAdd()
				// delete automatically after 60 seconds of idle time
				.setIdleTimeout(60)	
				.setMatch(match)
				.setActions(actions)
				.build();
		
		String output = buildFlowModOutput(sw, flowAdd);
		
		return new Pair<OFFlowAdd, String>(flowAdd, output);
	}
	
	private static String buildFlowModOutput(IOFSwitch sw, OFFlowMod flow)
	{
		long switchNumber = sw.getId().getLong();
		int portOutNumber = flow.getOutPort().getPortNumber();
		
		StringBuilder sb = new StringBuilder();
		Match match = flow.getMatch();
		for (MatchField<?> matchField : flow.getMatch().getMatchFields())
		{
			sb.append(matchField.getName());
			sb.append(":");
			sb.append(match.get(matchField).toString());
			sb.append(" ");
		}

		String output = String.format("------->Flow Mod Operation: switchNumber=%d, outPortNumber=%d, matchFields=%s",
				switchNumber, portOutNumber, sb.toString());
		return output;
	}
}
