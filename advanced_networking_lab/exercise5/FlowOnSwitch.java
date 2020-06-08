package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.protocol.OFFlowMod;
import org.projectfloodlight.openflow.types.DatapathId;

public class FlowOnSwitch
{
	public final DatapathId sw;
	public final OFFlowMod flow;
	
	public FlowOnSwitch(DatapathId sw, OFFlowMod flow) 
	{
		this.sw = sw;
		this.flow = flow;
	}
}

