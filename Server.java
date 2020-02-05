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
import java.util.Set;

public class Server {
	static final String DELIMITER = "!@#$%^&*()";
	static final String TABLE_END = "END";
	static final String SUCCESS = "Registered Successfully";
	static final String UNREGISTERED = "UNREGISTER";
	static final String UNREGISTER_MESSAGE = "Unregistered Successfully";
	static final String REQUEST = "REQUEST";
//	static ArrayList<User> userList = new ArrayList<>();
	static Map<String, User> map = new HashMap<>();

	private static ServerSocket ss;
	// private static Socket s;

	private static String getIpAddress(Socket socket) {
		return socket.getRemoteSocketAddress().toString().split("/")[1].split(":")[0];
	}
	
	private static void processUser(Socket s) throws IOException {
		InputStream inputStream = s.getInputStream();
		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		OutputStream out = s.getOutputStream();
		Thread t1 = new Thread(new Runnable() {
			public void run() {
				try {
					String ipAddr = getIpAddress(s);
					System.out.println("Thread Started for " + ipAddr);
					String name = getUserRegistration(out, br, ipAddr);
					User user = new User(name, ipAddr);
					
//					userList.add(user);
					map.put(ipAddr, user);
					write(out, SUCCESS);
					write(out, DELIMITER);
//					write(out, "you can now do the following");
					while (true) {
//						write(out,
//								"Please type \"" + UNREGISTERED
//										+ "\" to unregister. \nTo Request for clients information please type \""
//										+ REQUEST + "\".");
//						write(out, DELIMITER);
						String data = read(br);
						if (UNREGISTERED.equals(data)) {
							int index = 0;
							for (int i = 0; i < map.size(); i++) {
								if (map.get(i).getName().equals(user.getName())) {
									index = i;
									break;
								}
							}
							map.remove(index);
							write(out, UNREGISTER_MESSAGE);
							write(out, DELIMITER);
							break;
						}

						if (REQUEST.equals(data)) {
							sendUserInfor(out);
							

						}

					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				System.out.println("Thread finished");
			}
		});

		t1.start();
	}

	private static void sendUserInfor(OutputStream out) throws Exception {
		Set<String> ipAddr_List = map.keySet();
		for (String ipAddr : ipAddr_List) {
			User info = map.get(ipAddr);
			write(out, info.toString());
		}
		write(out, TABLE_END);
		write(out, "\n\n");
		write(out, DELIMITER);

	}

	private static void connectClients() throws Exception {

		ss = new ServerSocket(2222);
		while (true) {
			Socket s = ss.accept();
			processUser(s);

		}
	}

	private static String read(BufferedReader br) throws Exception {
		StringBuilder input = new StringBuilder();
		String line = br.readLine();
		while (!line.equals(DELIMITER)) {
			input.append(line).append(System.lineSeparator());
			line = br.readLine();
		}
		return input.toString().trim();

	}

	private static void write(OutputStream out, String str) throws Exception {
		out.write((str + "\n").getBytes());
		out.flush();
	}

	private static boolean isUserExists(String ipAddr, String name) {
//		for (User user : userList) {
//			if (user.getName().equals(name)) {
//				return true;
//			}
//		}
		
		Set<String> ipAddr_List = map.keySet();
		for (String ip : ipAddr_List) {
		//	User info = map.get(ip);
			if(!ip.equals(ipAddr)){
				if(name.equals(map.get(ip).getName())){
				return true;
				}
			}
		}
		return false;
	}

	private static String getUserRegistration(OutputStream out, BufferedReader br, String ipAddr ) throws Exception {
		while (true) {
//			write(out, "Register urself");
//			write(out, "Register like \"name yourname without space\"");
//			write(out, DELIMITER);
			String[] input = read(br).split(" ");
			// System.out.println(input); name space input typed by the client
			// before calling split method.
			if ("name".equals(input[0])) {
				String name = input[1];
				if (name != null && !name.isEmpty()) {
					if (!isUserExists(ipAddr,name)) {
						// System.out.println(name); user name entered by client
						// after calling split method
						return name;
					}
					write(out, "User already exists");
					
				}
			}

		}
	}

	public static void main(String[] args) {

		try {
			connectClients();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}
}

class User {
	String name, ipaddr;

	User(String name, String ipaddr) {
		this.name = name;
		this.ipaddr = ipaddr;
	}

	public String getIpAddr() {
		return ipaddr;
	}

	public String getName() {
		return name;
	}

	public String toString() {
		return name + " " + ipaddr;
	}

}
