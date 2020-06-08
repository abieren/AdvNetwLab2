package advanced_networking_lab.exercise5;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.projectfloodlight.openflow.types.MacAddress;

import advanced_networking_lab.exercise5.utils.AttributeStore;
import advanced_networking_lab.exercise5.utils.SetHelper;

public class Test {
	public static void main(String[] args) {
		MacAddress mac = MacAddress.of("FF:FF:FF:FF:FF:FF");
		
		MacAddress mac2 = MacAddress.of("FF:FF:FF:FF:FF:FF");
		
		System.out.println(mac == mac2);
		
		Set inters = SetHelper
				.withOrig(new HashSet<>(Arrays.asList(1,2,3,4)))
				.intersection(new HashSet(Arrays.asList(4,5,6,7)))
				.getSame();
		
		System.out.println(inters.size());
	}

}
