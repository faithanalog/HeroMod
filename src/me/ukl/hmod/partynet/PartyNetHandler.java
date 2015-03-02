package me.ukl.hmod.partynet;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import net.minecraft.client.Minecraft;
import me.ukl.hmod.partynet.recv.PartyPacketReceive;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveHealth;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveMana;
import me.ukl.hmod.partynet.recv.PartyPacketReceiveName;
import me.ukl.hmod.partynet.recv.PartyPacketReceivePosition;
import me.ukl.hmod.partynet.send.PartyPacket;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;

public class PartyNetHandler {
	
	private Socket sock;
	private ReadThread reader;
	private WriteThread writer;
	
	
	public PartyNetHandler() {
	}
	
	public void connect(String host, int port) throws IOException {
		sock = new Socket(host, port);
		sock.setTcpNoDelay(true);
		
		reader = new ReadThread(sock.getInputStream());
		writer = new WriteThread(sock.getOutputStream());
		
		new Thread(reader).start();
		new Thread(writer).start();
		
		new Thread() {
			@Override
			public void run() {
				while (true) {
					if (Minecraft.getMinecraft().theWorld == null && PartyNetHandler.this.sock != null) {
						try {
							PartyNetHandler.this.disconnect();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (PartyNetHandler.this.sock == null) {
						return;
					}
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	public void disconnect() throws IOException {
		if (sock != null) {
			reader.running.set(false);
			writer.running.set(false);
			sock.close();
			
			reader = null;
			writer = null;
			sock = null;
		}
	}
	
	public void send(PartyPacket pkt) {
		if (sock != null && !sock.isClosed()) {
			writer.writeQueue.add(pkt);
		}
	}
	
	public PartyPacketReceive poll() {
		return reader == null ? null : reader.readQueue.poll();
	}

}

class ReadThread implements Runnable {

	public Queue<PartyPacketReceive> readQueue = new ConcurrentLinkedQueue<PartyPacketReceive>();
	public AtomicBoolean running = new AtomicBoolean(true);
	private JsonReader reader;
	private Gson gson;
	
	public ReadThread(InputStream in) {
		reader = new JsonReader(new BufferedReader(new InputStreamReader(in)));
		gson = new Gson();
		reader.setLenient(true);
	}
	
	@Override
	public void run() {
		while (running.get()) {
			try {
				JsonObject obj = gson.fromJson(reader, JsonObject.class);
				if (obj == null || !obj.has("name")) {
					continue;
				}
				String name = obj.get("name").getAsString();
				PartyPacketReceive rcv = null;
				if (obj.has("hp")) {
					rcv = new PartyPacketReceiveHealth(obj.get("hp").getAsInt());
				} else if (obj.has("mana")) {
					rcv = new PartyPacketReceiveMana(obj.get("mana").getAsInt());
				} else if (obj.has("fullName")) {
					rcv = new PartyPacketReceiveName(obj.get("fullName").getAsString());
				} else if (obj.has("x") && obj.has("y") && obj.has("z")) {
					int x = obj.get("x").getAsInt();
					int y = obj.get("y").getAsInt();
					int z = obj.get("z").getAsInt();
					rcv = new PartyPacketReceivePosition(x, y, z);
				}
				if (rcv != null) {
					rcv.name = name;
					readQueue.add(rcv);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
}

class WriteThread implements Runnable {
	
	public BlockingQueue<PartyPacket> writeQueue = new LinkedBlockingQueue<PartyPacket>();
	public AtomicBoolean running = new AtomicBoolean(true);
	private Writer writer;
	private Gson gson;
	
	public WriteThread(OutputStream out) {
		writer = new BufferedWriter(new OutputStreamWriter(out));
		gson = new Gson();
	}

	@Override
	public void run() {
		while (running.get()) {
			try {
				PartyPacket pkt = writeQueue.take();
				String json = gson.toJson(pkt);
				writer.write(json);
				writer.flush();
			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}

//class ReconnThread implements Runnable {
//	
//}
