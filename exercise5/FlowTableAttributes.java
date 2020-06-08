package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.types.DatapathId;

import advanced_networking_lab.exercise5.utils.AttributeKey;

public class FlowTableAttributes 
{
	public static AttributeKey<DatapathId> SWITCH = new AttributeKey<>(DatapathId.class);
	public static AttributeKey<FlowOnSwitch> FLOW = new AttributeKey<>(FlowOnSwitch.class);
}
