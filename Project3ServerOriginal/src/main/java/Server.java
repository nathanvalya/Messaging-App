import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.function.Consumer;

import javafx.application.Platform;
import javafx.scene.control.ListView;
/*
 * Clicker: A: I really get it    B: No idea what you are talking about
 * C: kind of following
 */

public class Server{

	String userID;
	int count = 1;
	ArrayList<ClientThread> clients = new ArrayList<ClientThread>();

	ArrayList<String> liveChatStorage = new ArrayList<String>();
	TheServer server;
	private Consumer<Serializable> callback;


	Server(Consumer<Serializable> call){

		callback = call;
		server = new TheServer();
		server.start();
	}


	public class TheServer extends Thread{

		public void run() {

			try(ServerSocket mysocket = new ServerSocket(5555);){
				System.out.println("Server is waiting for a client!");


				while(true) {

					ClientThread c = new ClientThread(mysocket.accept(), count, "temp");
					callback.accept("New client has entered server.");
					clients.add(c);
					c.start();

					count++;

				}
			}//end of try
			catch(Exception e) {
				callback.accept("Server socket did not launch");
			}
		}//end of while
	}


	class ClientThread extends Thread{


		String userID;
		Socket connection;
		int count;
		ObjectInputStream in;
		ObjectOutputStream out;

		ClientThread(Socket s, int count, String newUserID){
			this.connection = s;
			this.count = count;
			this.userID = newUserID;
		}

		public void updateClients(int purpose, String removeUser) {
			if(purpose == 4){
				ArrayList<String> userNames = new ArrayList<>();
				for(int i = 0; i < clients.size(); i++){
					userNames.add(clients.get(i).userID);
				}

//				liveChatStorage.add("New connected user: " + removeUser);

				Message update = new Message(purpose, userNames, liveChatStorage);

				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
						t.out.writeObject(update);
					}
					catch(Exception e) {

					}
				}
			}

			if(purpose == 5){
				Message update = new Message(purpose, removeUser);

				for(int i = 0; i < clients.size(); i++) {
					ClientThread t = clients.get(i);
					try {
						t.out.writeObject(update);
					}
					catch(Exception e) {

					}
				}
			}

		}

		public int findUserID(String ID){
			for(int i = 0; i < clients.size(); i++){
				if(clients.get(i).userID.equals(ID)){
					return i;
				}
			}

			return -1;
		}

		public void run(){

			try {
				in = new ObjectInputStream(connection.getInputStream());
				out = new ObjectOutputStream(connection.getOutputStream());
				connection.setTcpNoDelay(true);
			}
			catch(Exception e) {
				System.out.println("Streams not open");
			}

//				updateClients("new client on server: client #"+count);

			while(true) {
				try {

					Message data = (Message) in.readObject();


					if(data.purpose == 1){
						String userName = data.userId;

//								System.out.println("received");
						boolean uniqueID;

						int index = findUserID(data.userId);
//						System.out.println(index);

						Message sendBack = new Message(1, data.userId, liveChatStorage);
						System.out.println(data.userId);

						if(index == -1){
//									System.out.println("Unique ID");
							sendBack.newIDAccepted = true;
							uniqueID = true;

							index = findUserID("temp");
							clients.get(index).userID = data.userId;

							callback.accept("New ID accepted: " + data.userId + ".");
							callback.accept("Sending confirmation to client: "+ data.userId + ".");
						}

						else{
							uniqueID = false;
							sendBack.newIDAccepted = false;
							callback.accept("New ID already exists: " + data.userId + ".");
							callback.accept("Sending rejection to client: "+ data.userId + ".");
						}

						this.out.writeObject(sendBack);

						if(uniqueID == true){
							updateClients(4, data.userId);
							callback.accept("Sending update to connected clients...");
						}
					}

					if(data.purpose == 2){
//
						if(data.toID.equals("all")){
							for(int i = 0; i < clients.size(); i++){
								if(!(clients.get(i).userID.equals(data.fromID))){
									Message sendMessage = new Message(2, data.fromID, "all", data.message);
									(clients.get(i)).out.writeObject(sendMessage);
								}
							}
							liveChatStorage.add(data.fromID + ": " + data.message);
							callback.accept(data.fromID + " sending message to all connected user,");
						}
						else{
							Message sendMessage;
							int index = findUserID(data.toID);

							sendMessage = new Message(2, data.fromID, data.toID, data.message);
							ClientThread t = clients.get(index);
							t.out.writeObject(sendMessage);

							callback.accept(data.fromID + " sending message to " + data.toID + ".");

						}
					}
					if(data.purpose == 5){
						callback.accept(this.userID + " has disconected.");
						callback.accept("Sending update to connected clients...");
						updateClients(5, this.userID);
						liveChatStorage.add(this.userID);
						clients.remove(this);
						break;
					}

				}
				catch(Exception e) {
					callback.accept(this.userID + " has disconected.");
					callback.accept("Sending update to connected clients...");
					updateClients(5, this.userID);

//					    	updateClients(this.userID + "has left the server!");
					clients.remove(this);
					break;
				}
			}
		}//end of run

	}//end of client thread
}

	
	

	
