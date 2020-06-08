package advanced_networking_lab.exercise3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.projectfloodlight.openflow.protocol.OFMessage;
import org.projectfloodlight.openflow.protocol.OFPacketIn;
import org.projectfloodlight.openflow.protocol.OFPacketOut;
import org.projectfloodlight.openflow.protocol.OFType;
import org.projectfloodlight.openflow.protocol.OFVersion;
import org.projectfloodlight.openflow.protocol.action.OFAction;
import org.projectfloodlight.openflow.protocol.action.OFActionOutput;
import org.projectfloodlight.openflow.protocol.match.MatchField;
import org.projectfloodlight.openflow.protocol.ver13.OFFactoryVer13;
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


public class Exercise3Part1 
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
		System.out.println("Exercise3Part1 init");
		floodlightProvider = context.getServiceImpl(IFloodlightProviderService.class);
	}

	@Override
	public void startUp(FloodlightModuleContext context) throws FloodlightModuleException {
		System.out.println("Exercise3Part1 startup");
		floodlightProvider.addOFMessageListener(OFType.PACKET_IN, this);
	}

	@Override
	public String getName() {
		return Exercise3Part1.class.getPackage().getName();
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
		System.out.println("Exercise3Part1 receive");
		OFMessage outMessage = createPacketOutFlood(sw, msg);
		sw.write(outMessage);		
		return Command.CONTINUE;
	}

	private OFMessage createPacketOutFlood(IOFSwitch sw, OFMessage msg)
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
