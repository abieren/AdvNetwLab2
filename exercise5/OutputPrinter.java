package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPortStatus;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.types.DatapathId;

import net.floodlightcontroller.core.FloodlightContext;
import net.floodlightcontroller.core.IFloodlightProviderService;
import net.floodlightcontroller.core.IOFSwitch;
import net.floodlightcontroller.packet.Ethernet;
import net.floodlightcontroller.util.OFMessageUtils;

public class OutputPrinter 
{
	public static void println(String output)
	{
		System.out.println(output);
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
		
		String output = String.format("------->Packet In Event: switchNumber=%s, inPortNumber=%d, ethType=%s, srcMAC=%s, dstMAC=%s", 
				switchNumber, inPortNumber, ethType, srcMAC, dstMAC);
		
		println(output);
	}
	
	public static void printPortStatus(IOFSwitch sw, OFPortStatus msg, FloodlightContext cntx)
	{
		long switchNumber = sw.getId().getLong();
		int portNumber = msg.getDesc().getPortNo().getPortNumber();
		String portStatus = msg.getReason().toString();
		
		String output = String.format("------->Port Status Event: switchNumber=%s, portNumber=%d, portStatus=%s",
				switchNumber, portNumber, portStatus);
		
		println(output);
	}
	
	
	public static void printFlowDel()
	{
		int switchNumber;
		int outPortNumber;
		Object matchingFields; //???, gegebenenfalls
		
		String output = String.format("------->Flow Del Operation: ");
	}
}
