package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.protocol.OFFactory;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
import org.projectfloodlight.openflow.types.OFPort;

public class ActionBuilderV13 
{
	public static final OFFactory ofFactory = new OFFactoryVer13();
	
	public static OFActionOutput buildOutput(OFPort port)
	{
		return ofFactory.actions().buildOutput()
				.setPort(port)
				.build();
	}
}