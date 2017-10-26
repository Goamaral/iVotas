import java.net.*;
import java.io.*;
import java.util.*;

public class Terminal {
  static int portServer = 7001;
	static String addressServer = "localhost";
  static Socket socket;
  static DataInputStream in;
  static DataOutputStream out;
	static String username;
	static String password;

  public static void main(String args[]) {
		HashMap<String, String> response;
		boolean loginRequired;
		boolean loginSucessful;
		boolean loginFailed;

    getOptions(args);

    connectSocket();
		createStreams();

		System.out.println("Terminal de voto ativo");

		while (true) {
			if (newInput()) {
				response = parseResponse(readSocket());
				if (response != null) {
					loginRequired = response.get("type").equals("status")
														&& response.get("login").equals("required");

					loginSucessful = response.get("type").equals("status")
														&& response.get("login").equals("sucessful");

					loginFailed = response.get("type").equals("status")
														&& response.get("login").equals("failed");

					if (loginRequired) {
						auth();
					} else if (loginSucessful) {
						System.out.println(response.get("msg"));
					} else if (loginFailed) {
						System.out.println("Credenciais invalidas");
						auth();
					}
				} else {
					System.out.printf("NULL RESPONSE");
				}
			}
		}
  }

	public static boolean newInput() {
		try {
			return in.available() != 0;
		} catch (IOException e) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
			return false;
		}
	}

	public static void createStreams() {
		try {
			in = new DataInputStream(socket.getInputStream());
			out = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			System.out.println("Falha na criacao de streams");
			System.exit(0);
		}
	}

	public static void connectSocket() {
		try {
			socket = new Socket(addressServer, portServer);
		} catch (UnknownHostException uhe) {
			System.out.println("Host desconhecido");
			System.exit(0);
		}
		catch (IOException ioe) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
		}
	}

	public static void nap() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {
			System.exit(0);
		}
	}

	public static void auth() {
		Scanner scanner = new Scanner(System.in);

		System.out.print("Numero estudante: ");
		username = scanner.nextLine();

		System.out.print("Password: ");
		password = scanner.nextLine();

		writeSocket("type|login;username|" + username + ";password|" + password);
	}

	public static void writeSocket(String query) {
		try {
			out.writeUTF(query);
		} catch (IOException ioe) {
			System.out.println("Falha na ligacao a mesa de voto");
			System.exit(0);
		}
	}

	public static String readSocket() {
		try {
			return in.readUTF();
		} catch (IOException e) {
			return null;
		}
	}

	public static HashMap<String, String> parseResponse(String response) {
		HashMap<String, String> out = new HashMap<String, String>();

		String[] pairs = response.split(";");
		String[] pairParts;

		for (String pair : pairs) {
			pairParts = pair.split("\\|");
			out.put(pairParts[0], pairParts[1]);
		}

		return out;
	}

  public static void getOptions(String[] args) {
		for (int i=0; i<args.length; ++i) {
			switch (args[i]) {
        case "-sp":
				case "--serverport":
					try {
						portServer = Integer.parseInt(args[i+1]);
					} catch (Exception e) {
						System.out.println("No valid server port provided, using default: 7001");
					}
					break;
				case "-sa":
				case "--serveraddress":
					try {
						addressServer = args[i+1];
					} catch (Exception e) {
						System.out.println("No valid server address provided, using default: localhost");
					}
					break;
				case "-h":
				case "--help":
					System.out.println("java -lp 3000 -sp 7001");
					System.exit(0);
					break;
			}
		}
	}
}
