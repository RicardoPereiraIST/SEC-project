package main;

import java.io.FileInputStream;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

public class API {

	private KeyStore keyStore;
	private String password;
	private InterfaceRMI stub;
	private PublicKey serverKey;
	private PublicKey publicKey;
	private PrivateKey privateKey;
	
	public void init(KeyStore key, String id, String pass){
		keyStore = key;
		password = pass;
				
		try{
			keyStore.load(new FileInputStream("bin/main/keystore_" + id +".jks"), password.toCharArray());
			Registry registry = LocateRegistry.getRegistry(8000);
	    	stub = (InterfaceRMI) registry.lookup("Interface");
	    	
	    	CertificateFactory f = CertificateFactory.getInstance("X.509");
	    	X509Certificate certificate = (X509Certificate)f.generateCertificate(new FileInputStream("bin/main/server.cer"));
	    	serverKey = certificate.getPublicKey();
	    	
	    	privateKey = (PrivateKey)keyStore.getKey("clientkeystore", password.toCharArray());
	    	publicKey = keyStore.getCertificate("clientkeystore").getPublicKey();
		}
		catch (Exception e) {
        	System.err.println("Client exception: " + e.toString());
        	e.printStackTrace();
    	}
	}
	
	public void register_user(){
		try{
			byte[][] bytes = stub.getChallenge(publicKey);
			if(Crypto.verifySignature(serverKey, bytes[0], bytes[1])){
				byte[] t = Crypto.decrypt(privateKey, Crypto.decodeBase64(bytes[0]));
				byte[] token = Crypto.encodeBase64(Crypto.encrypt(serverKey, Crypto.nextToken(t)));
				stub.register(publicKey,
						token,
					      Crypto.signData(privateKey, Crypto.concatenateBytes(publicKey.getEncoded(), token)));
			}
		}
		catch(Exception e){
			System.err.println("Register user exception: " + e.toString());
        	e.printStackTrace();
		}
	}
	
	public void save_password(byte[] domain, byte[] username, byte[] password){
		try{
			byte[] d = Crypto.encodeBase64(Crypto.encrypt(serverKey, domain));
			byte[] u = Crypto.encodeBase64(Crypto.encrypt(serverKey, username));
			byte[] p = Crypto.encodeBase64(Crypto.encrypt(publicKey, password));
		
			stub.put(publicKey, 
					 d, 
					 u, 
					 p, 
					 Crypto.signData(privateKey, Crypto.concatenateBytes(d,u,p)));
		}
		catch(Exception e){
			System.err.println("Save password exception: " + e.toString());
        	e.printStackTrace();
		}
	}
	
	public byte[] retrieve_password(byte[] domain, byte[] username){
		try{
			byte[] d = Crypto.encodeBase64(Crypto.encrypt(serverKey, domain));
			byte[] u = Crypto.encodeBase64(Crypto.encrypt(serverKey, username));
			byte[] password = stub.get(publicKey, 
					                   d, 
					                   u, 
					                   Crypto.signData(privateKey, Crypto.concatenateBytes(d,u)));
			if(password != null){
				return Crypto.decrypt(privateKey, Crypto.decodeBase64(password));
			}
			else{
				return null;
			}
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
}
