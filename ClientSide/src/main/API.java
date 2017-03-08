package main;

import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;;

public class API {

	private KeyStore keyStore;
	private String password;
	private InterfaceRMI stub;
	
	public void init(KeyStore key, String id, String pass){
		keyStore = key;
		password = pass;
				
		try{
			keyStore.load(new FileInputStream("./keystore_" + id +".jks"), password.toCharArray());
			Registry registry = LocateRegistry.getRegistry(8000);
	    	stub = (InterfaceRMI) registry.lookup("Interface");
		}
		catch (Exception e) {
        	System.err.println("Client exception: " + e.toString());
        	e.printStackTrace();
    	}
	}
	
	public void register_user(){
		try{
			stub.register(getPublicKey(), signData("Integrity".getBytes()));
		}
		catch(Exception e){
			System.err.println("Register user exception: " + e.toString());
        	e.printStackTrace();
		}
	}
	
	public void save_password(byte[] domain, byte[] username, byte[] password){
		try{
			stub.put(getPublicKey(), domain, username, password);
		}
		catch(Exception e){
			System.err.println("Save password exception: " + e.toString());
        	e.printStackTrace();
		}
	}
	
	public byte[] retrieve_password(byte[] domain, byte[] username){
		try{
			return stub.get(getPublicKey(), domain, username);
		}
		catch(Exception e){
			System.err.println("Retrieve password exception: " + e.toString());
        	e.printStackTrace();
		}
		return null;
	}
	
	public void close(){
		System.exit(0);
	}
	
	private PublicKey getPublicKey(){
		try{
			return keyStore.getCertificate("clientkeystore").getPublicKey();
		}
		catch(Exception e){
			System.err.println("Public Key exception: " + e.toString());
        	e.printStackTrace();
		}
		return null;
	}
	
	private PrivateKey getPrivateKey(){
		try{
			return (PrivateKey)keyStore.getKey("clientkeystore", password.toCharArray());
		}
		catch(Exception e){
			System.err.println("Private key exception: " + e.toString());
        	e.printStackTrace();
		}
		return null;
	}
	
	private byte[] signData(byte[] data){
		try{
			// generating a signature
			Signature dsaForSign = Signature.getInstance("SHA1withDSA");
			dsaForSign.initSign(getPrivateKey());
			dsaForSign.update(data);
			return dsaForSign.sign();
		}
		catch(Exception e){
			System.err.println("Signature exception: " + e.toString());
        	e.printStackTrace();
		}
		return null;
	}
	
}
