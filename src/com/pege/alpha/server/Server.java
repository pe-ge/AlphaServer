package com.pege.alpha.server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class Server extends Thread {

	private DatagramSocket socket;
	private Set<Client> clients = new HashSet<Client>();
	
	public Server(int port) {
		super("Alpha Server");
		
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
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
		while (true) {
			byte[] data = new byte[256];
			
			DatagramPacket packet = new DatagramPacket(data, data.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendToAll(packet);
		}
	}
			
	private void sendToAll(final DatagramPacket packet) {
		Thread sender = new Thread("Sender") {
			public void run() {
				Client sender = new Client(packet.getAddress(), packet.getPort());
				clients.add(sender);
				for (Client client : clients) {
					if (!client.equals(sender)) {
						send(client, packet.getData());
					}
				}
			}
		};
		sender.start();
	}
	
	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("Usage: java Server [port]");
			return;
		}
		
		int port = Integer.parseInt(args[0]);
		Server server = new Server(port);
		server.start();
	}
}
