package advanced_networking_lab.exercise5;

import org.projectfloodlight.openflow.types.MacAddress;

public class Test {
	public static void main(String[] args) {
		MacAddress mac = MacAddress.of("FF:FF:FF:FF:FF:FF");
		
		MacAddress mac2 = MacAddress.of("FF:FF:FF:FF:FF:FF");
		
		System.out.println(mac == mac2);
	}

}
