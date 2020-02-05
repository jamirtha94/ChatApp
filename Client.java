package amirtha;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class Client {

	private static final Scanner SCANNER = new Scanner(System.in);
	static Map<String, String> map = new HashMap<>();
	static Map<String, Socket> socketMap = new HashMap<>();
	static ServerSocket serverSocket;

	public static void main(String[] args) {
		 String pattern = "^([0-9]\\.|[0-9][0-9]\\.|[0-1][0-9][0-9]\\.|2[0-4][0-9]\\."
		 		+ "|25[0-5]\\.){3}([0-9]|[0-9][0-9]|[0-1][0-9][0-9]|2[0-4][0-9]|25[0-5])$";

		String serverIp = null;
		if(args.length < 1 || args[0] == null || args[0].isEmpty()) {
            System.out.println("Please run the program by passing <SERVER IP ADDRESS");
        }
		serverIp = args[0];
		if (!serverIp.matches(pattern)){
			System.exit(1);
		}	
		
		try {
			serverSocket = new ServerSocket(3333);
		} catch (Exception e) {
			e.printStackTrace();
		}
		connect(serverIp);
		
		
		
	}

	private static void connect(String serverIp) {

		try {

			Socket s = new Socket(serverIp, 2222);
			InputStream in = s.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			OutputStream out = s.getOutputStream();
			toregister(br, out);
			getUserInfo(out, br);
			acceptClientToChat(out, br);
			System.out.println("you can now do the following");
			System.out.println("Please type \"" + MainServer.UNREGISTERED
					+ "\" to unregister. \nTo Request for clients information please type \"" + MainServer.REQUEST
					+ "\".");
			while (true) {
				String input = readFromConsole();// if input is send as
													// unregistered

				if (MainServer.REQUEST.equals(input)) {

					listUsers(out, br);

					// writeServerOutput("Please enter the user name to initiate
					// chat: ");
					// String userName = readFromConsole();
					// getIpAddr(userName);
				} else if (MainServer.UNREGISTERED.equals(input)) {
					// client reads the user names and enters user name to
					// initiate chatting
					write(out, input);
					write(out, MainServer.DELIMITER);
					s.close();
					Set<String> ips = socketMap.keySet();
					for (String ip : ips) {
						Socket socket = socketMap.get(ip);
						socket.close();
						serverSocket.close();

					}

					break;
				} else if ("CHAT".equals(input.split(" ")[0])) {
					// syntax : CHAT name message
					String[] words = input.split(" ");
					if (words.length > 2) {
						String name = words[1];
						StringBuilder msg = new StringBuilder();

						for (int i = 2; i < words.length; i++) {

							msg.append(words[i] + " ");
						}
						String message = msg.toString();
						sendMessageToUser(name, message);

					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void getUserInfo(OutputStream out, BufferedReader br) throws IOException {
		write(out, MainServer.REQUEST);
		write(out, MainServer.DELIMITER);
		String serverResponse = read(br);
		String input1 = serverResponse.trim().split("END")[0];
		String[] strArr = input1.split("\n");
		map = new HashMap<>();
		for (String str1 : strArr) {
			String name = str1.split(" ")[0];
			String ipAddr = str1.split(" ")[1];
			// System.out.println(name + " "+ipAddr);
			map.put(name, ipAddr);
		}

	}

	private static void listUsers(OutputStream out, BufferedReader br) throws IOException {
		getUserInfo(out, br);
		Set<String> keys = map.keySet();
		for (String key : keys) {
			System.out.println(key);
		}
	}

	private static String getIpAddr(String userName) {

		String userIp = map.get(userName);
		// System.out.println(userIp);
		return userIp;

	}

	private static void acceptClientToChat(OutputStream out, BufferedReader br) throws Exception {

		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					while (!serverSocket.isClosed()) {
						Socket clientSocket = serverSocket.accept();
						String ipAddr = getIpAddress(clientSocket);
						socketMap.put(ipAddr, clientSocket);
						String name = getNameForIp(ipAddr, out, br);
						processClientSocket(clientSocket, name);

					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		t1.start();

	}

	private static String getIpAddress(Socket socket) {
		return socket.getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
	}

	private static String getNameForIp(String ip, OutputStream out, BufferedReader br) throws IOException {
		Set<String> keys = map.keySet();
		for (String name : keys) {
			if (map.get(name).equals(ip)) {

				return name;
			}
		}
		getUserInfo(out, br);
		for (String name : keys) {
			if (map.get(name).equals(ip)) {

				return name;
			}
		}
		return null;
	}

	private static void processClientSocket(Socket clientsocket, String userName) throws Exception {
		InputStream inputStream = clientsocket.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					while (!clientsocket.isClosed()) {
						String message = read(br);
						System.out.println(userName + ": " + message);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		});
		t1.start();

	}

	private static void toregister(BufferedReader br, OutputStream out) throws Exception {

		// String input = read(br); // to read the lines from the server
		// writeServerOutput(input);

		while (true) {
			System.out.println("Register urself");
			System.out.println("Register like \"name <yourname_without_space>\"");
			String str = readFromConsole(); // client enters his user
											// information
			write(out, str);
			write(out, MainServer.DELIMITER);
			String input = read(br);
			/*
			 * if user enters name space hisname then => 1st possible value :
			 * SUCCESS \n "you can now do the following" \n
			 * "Please type \"UNREGISTER\" to unregister. \nTo Request for clients information please type \"REQUEST\"."
			 * else server sends below message =>
			 * " 2nd possible condition :  Register urself \n  "Register like
			 * "name yourname without space"
			 */
			// writeServerOutput(input);
			if (MainServer.SUCCESS.equals(input.split("\n")[0])) {
				return;
			}
		}

	}

	private static void write(OutputStream out, String str) throws IOException {
		out.write((str + "\n").getBytes());
		out.flush();

	}

	private static String readFromConsole() throws Exception {
		String str = SCANNER.nextLine();

		return str;
	}

	private static String read(BufferedReader br) throws IOException {
		StringBuilder input = new StringBuilder();
		String line = br.readLine();
		while (!line.equals(MainServer.DELIMITER)) {
			input.append(line).append(System.lineSeparator());
			line = br.readLine();
		}
		return input.toString().trim();

	}

	private static Socket getSocketForUser(String ipAddr, String userName) throws Exception {
		Socket userSocket = socketMap.get(ipAddr);
		if (userSocket == null) {
			userSocket = new Socket(ipAddr, 33333);
			processClientSocket(userSocket, userName);
			socketMap.put(ipAddr, userSocket);
		}
		return userSocket;

	}

	private static void sendMessageToUser(String userName, String message) throws Exception {
		String ip = getIpAddr(userName);
		Socket socket = getSocketForUser(ip, userName);
		OutputStream outputStream = socket.getOutputStream();
		write(outputStream, message);
		write(outputStream, MainServer.DELIMITER);

	}

}
