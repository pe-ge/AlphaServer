package com.pege.alpha.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread {

	private DatagramSocket socket;
	private boolean running;
	private Set<Client> clients = new HashSet<Client>();
	
	public Server(int port) {
		super("Alpha Server");
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		running = true;
	}
	
	private void send(final Client client, final byte[] data) {
		DatagramPacket packet = new DatagramPacket(data, data.length, client.getAddress(), client.getPort());
		try {
			socket.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		while (running) {
			byte[] data = new byte[256];
			
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			processPacket(packet);
		}
		
	}
			
	private void processPacket(final DatagramPacket packet) {
		Thread processor = new Thread("Processor") {
			public void run() {
				Client sender = new Client(packet.getAddress(), packet.getPort());
				clients.add(sender);

				if (disconnect(packet.getData())) { //first 4 bytes are 0
					disconnectClient(sender);
				}
				for (Client client : clients) {
					if (!client.equals(sender)) {
						send(client, packet.getData());
					}
				}
				System.out.println("Number of clients: " + clients.size());
			}
		};
		processor.start();
	}
	
	private boolean disconnect(byte[] data) {
		boolean disconnect = true;
		for (int i = 0; i < 4; i++) disconnect &= data[i] == 0;
		return disconnect;
	}
	
	private void disconnectClient(Client clientToDisconnect) {
		clients.remove(clientToDisconnect);
	}
	
	public void closeServer() {
		running = false;
		socket.close();
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Server [port]");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		final Server server = new Server(port);
		server.start();
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
		    public void run() {
		    	server.closeServer();
		    }
		}));
	}
}
