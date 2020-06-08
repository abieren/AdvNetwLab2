package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.match.Match;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.util.OFMessageUtils;

@SuppressWarnings("unused")
public class OutputPrinter 
{
	private static boolean printWithSwitchNumber = true;
	
	public static void println(IOFSwitch sw, String output)
	{
		if (printWithSwitchNumber) System.out.println(String.format("[%d] %s", sw.getId().getLong(),output));
		else System.out.println(String.format("%s", sw.getId().getLong(),output));
	}
	
	public static void printPacketIn(IOFSwitch sw, OFPacketIn msg, FloodlightContext cntx)
	{
		long switchNumber = sw.getId().getLong();
		int inPortNumber = OFMessageUtils.getInPort(msg).getPortNumber();
		Ethernet eth = IFloodlightProviderService.bcStore.
				get(cntx, IFloodlightProviderService.CONTEXT_PI_PAYLOAD);
		String ethType = eth.getEtherType().toString();
		String srcMAC = eth.getSourceMACAddress().toString();
		String dstMAC = eth.getDestinationMACAddress().toString();
		
		// TODO entfernen
		StringBuilder sb = new StringBuilder();
		Match match = msg.getMatch();
		for (MatchField<?> matchField : msg.getMatch().getMatchFields())
		{
			sb.append(matchField.getName());
			sb.append(":");
			sb.append(match.get(matchField).toString());
			sb.append(" ");
		}
		
		String output = String.format("------->Packet In Event: switchNumber=%s, inPortNumber=%d, ethType=%s, srcMAC=%s, dstMAC=%s, matchFields=%s", 
				switchNumber, inPortNumber, ethType, srcMAC, dstMAC, sb.toString());
		
		println(sw, output);
	}
	
	public static void printPortStatus(IOFSwitch sw, OFPortStatus msg, FloodlightContext cntx)
	{
		long switchNumber = sw.getId().getLong();
		int portNumber = msg.getDesc().getPortNo().getPortNumber();
		String portStatus = msg.getReason().toString();
		
		String output = String.format("------->Port Status Event: switchNumber=%s, portNumber=%d, portStatus=%s",
				switchNumber, portNumber, portStatus);
		
		println(sw, output);
	}
}
