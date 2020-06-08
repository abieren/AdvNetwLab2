package advanced_networking_lab.exercise5;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.projectfloodlight.openflow.protocol.OFFlowAdd;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.types.ArpOpcode;
import org.projectfloodlight.openflow.types.DatapathId;
import org.projectfloodlight.openflow.types.ICMPv4Type;
import org.projectfloodlight.openflow.types.IPv4Address;
import org.projectfloodlight.openflow.types.MacAddress;
import org.projectfloodlight.openflow.types.OFPort;

import javafx.util.Pair;
import net.floodlightcontroller.core.IOFSwitch;

public class TopologyManager
{
	private HashMap<DatapathId, IOFSwitch> switches = new HashMap<>();
	private boolean[][] switchPortStateTable = new boolean[3][3];
	
	public TopologyManager() 
	{		
		for (int sw = 0; sw < 3; sw++)
		{
			for (int port = 0; port < 3; port++)		
			{
				if (sw == port) continue;
				switchPortStateTable[sw][port] = true;
			}
		}
	}
	
	public void rememberSwitch(IOFSwitch sw)
	{
		switches.put(sw.getId(), sw);
	}
	
	public void setPort(DatapathId sw, OFPort port, boolean state)
	{
		switchPortStateTable[(int) sw.getLong() - 1][port.getPortNumber() - 1] = state;
	}
	
	public boolean isPortUp(DatapathId sw, OFPort port)
	{
		return switchPortStateTable[(int) sw.getLong() - 1][port.getPortNumber() - 1];
	}
	
	public boolean isLinkUp(DatapathId srcSwitch, DatapathId dstSwitch)
	{
		return isPortUp(srcSwitch, OFPort.of((int) dstSwitch.getLong())) &&
				isPortUp(dstSwitch, OFPort.of((int) srcSwitch.getLong()));
	}
	
	
	public void createARPFlow(IOFSwitch sw, DatapathId srcSwitch, OFPort srcPort, MacAddress srcMac, 
			DatapathId dstSwitch, OFPort dstPort, MacAddress dstMac, ArpOpcode arpOpcode)
	{
		if (srcSwitch.equals(dstSwitch))
		{
			// create flow: host -> switch -> host
			IOFSwitch switch1 = switches.get(srcSwitch);
			Pair<OFFlowAdd, String> flow = MessageBuilderV13.buildFlowAddArp(
					switch1, srcPort, dstPort, srcMac, dstMac, arpOpcode);
			OutputPrinter.println(sw, flow.getValue());
			switch1.write(flow.getKey());
		}
		else
		{
			// find link
			// try shortest
			if (isLinkUp(srcSwitch, dstSwitch))
			{
				// create flow: host -> switch -> switch -> host
				IOFSwitch switch1 = switches.get(srcSwitch);
				IOFSwitch switch2 = switches.get(dstSwitch);
				OFPort portToSwitch1 = OFPort.of((int) srcSwitch.getLong());
				OFPort portToSwitch2 = OFPort.of((int) dstSwitch.getLong());

				// flow for first switch
				Pair<OFFlowAdd, String> flow1 = MessageBuilderV13.buildFlowAddArp(
						switch1, srcPort, portToSwitch2, srcMac, dstMac, arpOpcode);
				OutputPrinter.println(sw, flow1.getValue());
				switch1.write(flow1.getKey());
				
				// flow for second switch
				Pair<OFFlowAdd, String> flow2 = MessageBuilderV13.buildFlowAddArp(
						switch2, portToSwitch1, dstPort, srcMac, dstMac, arpOpcode);
				OutputPrinter.println(sw, flow2.getValue());
				switch2.write(flow2.getKey());
			}
			else
			{
				// try with auxiliary Switch
				DatapathId auxSwitch = DatapathId.of(6L - srcSwitch.getLong() - dstSwitch.getLong());
				if (isLinkUp(srcSwitch, auxSwitch) && isLinkUp(auxSwitch, dstSwitch))
				{
					// create flow: host -> switch -> switch -> switch -> host
					IOFSwitch switch1 = switches.get(srcSwitch);
					IOFSwitch switch2 = switches.get(auxSwitch);
					IOFSwitch switch3 = switches.get(dstSwitch);
					OFPort portToSwitch1 = OFPort.of((int) srcSwitch.getLong());
					OFPort portToSwitch2 = OFPort.of((int) auxSwitch.getLong());
					OFPort portToSwitch3 = OFPort.of((int) dstSwitch.getLong());

					// flow for first switch
					Pair<OFFlowAdd, String> flow1 = MessageBuilderV13.buildFlowAddArp(
							switch1, srcPort, portToSwitch2, srcMac, dstMac, arpOpcode);
					OutputPrinter.println(sw, flow1.getValue());
					switch1.write(flow1.getKey());
					
					// flow for second switch
					Pair<OFFlowAdd, String> flow2 = MessageBuilderV13.buildFlowAddArp(
							switch2, portToSwitch1, portToSwitch3, srcMac, dstMac, arpOpcode);
					OutputPrinter.println(sw, flow2.getValue());
					switch2.write(flow2.getKey());
					
					// flow for third switch
					Pair<OFFlowAdd, String> flow3 = MessageBuilderV13.buildFlowAddArp(
							switch3, portToSwitch2, dstPort, srcMac, dstMac, arpOpcode);
					OutputPrinter.println(sw, flow3.getValue());
					switch3.write(flow3.getKey());
				}
				else
				{
					throw new RuntimeException("Cannot find a route of links");
				}
			}
		}
	}

	public void createICMPFlow(IOFSwitch sw, DatapathId srcSwitch, OFPort srcPort, IPv4Address srcIp, 
			DatapathId dstSwitch, OFPort dstPort, IPv4Address dstIp, ICMPv4Type icmpType) 
	{
		if (srcSwitch.equals(dstSwitch))
		{
			// create flow: host -> switch -> host
			IOFSwitch switch1 = switches.get(srcSwitch);
			Pair<OFFlowAdd, String> flow = MessageBuilderV13.buildFlowAddIcmp(
					switch1, srcPort, dstPort, srcIp, dstIp, icmpType);
			OutputPrinter.println(sw, flow.getValue());
			switch1.write(flow.getKey());
		}
		else
		{
			// find link
			// try shortest
			if (isLinkUp(srcSwitch, dstSwitch))
			{
				// create flow: host -> switch -> switch -> host
				IOFSwitch switch1 = switches.get(srcSwitch);
				IOFSwitch switch2 = switches.get(dstSwitch);
				OFPort portToSwitch1 = OFPort.of((int) srcSwitch.getLong());
				OFPort portToSwitch2 = OFPort.of((int) dstSwitch.getLong());

				// flow for first switch
				Pair<OFFlowAdd, String> flow1 = MessageBuilderV13.buildFlowAddIcmp(
						switch1, srcPort, portToSwitch2, srcIp, dstIp, icmpType);
				OutputPrinter.println(sw, flow1.getValue());
				switch1.write(flow1.getKey());
				
				// flow for second switch
				Pair<OFFlowAdd, String> flow2 = MessageBuilderV13.buildFlowAddIcmp(
						switch2, portToSwitch1, dstPort, srcIp, dstIp, icmpType);
				OutputPrinter.println(sw, flow2.getValue());
				switch2.write(flow2.getKey());
			}
			else
			{
				// try with auxiliary Switch
				DatapathId auxSwitch = DatapathId.of(6L - srcSwitch.getLong() - dstSwitch.getLong());
				if (isLinkUp(srcSwitch, auxSwitch) && isLinkUp(auxSwitch, dstSwitch))
				{
					// create flow: host -> switch -> switch -> switch -> host
					IOFSwitch switch1 = switches.get(srcSwitch);
					IOFSwitch switch2 = switches.get(auxSwitch);
					IOFSwitch switch3 = switches.get(dstSwitch);
					OFPort portToSwitch1 = OFPort.of((int) srcSwitch.getLong());
					OFPort portToSwitch2 = OFPort.of((int) auxSwitch.getLong());
					OFPort portToSwitch3 = OFPort.of((int) dstSwitch.getLong());

					// flow for first switch
					Pair<OFFlowAdd, String> flow1 = MessageBuilderV13.buildFlowAddIcmp(
							switch1, srcPort, portToSwitch2, srcIp, dstIp, icmpType);
					OutputPrinter.println(sw, flow1.getValue());
					switch1.write(flow1.getKey());
					
					// flow for second switch
					Pair<OFFlowAdd, String> flow2 = MessageBuilderV13.buildFlowAddIcmp(
							switch2, portToSwitch1, portToSwitch3, srcIp, dstIp, icmpType);
					OutputPrinter.println(sw, flow2.getValue());
					switch2.write(flow2.getKey());
					
					// flow for third switch
					Pair<OFFlowAdd, String> flow3 = MessageBuilderV13.buildFlowAddIcmp(
							switch3, portToSwitch2, dstPort, srcIp, dstIp, icmpType);
					OutputPrinter.println(sw, flow3.getValue());
					switch3.write(flow3.getKey());
				}
				else
				{
					throw new RuntimeException("Cannot find a route of links");
				}
			}
		}		
	}
}
