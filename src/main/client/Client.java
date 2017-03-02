package main.client;

import java.rmi.registry.Registry;
import main.interfaces.InterfaceRMI;
import java.rmi.registry.LocateRegistry;

public class Client {

	public static void main(String[] args) {
		
		try {
        	Registry registry = LocateRegistry.getRegistry("localhost");
        	InterfaceRMI stub = (InterfaceRMI) registry.lookup("Interface");
        	stub.register(null);
        	
        } catch (Exception e) {
        	System.err.println("Client exception: " + e.toString());
        	e.printStackTrace();
    	}
	}
}