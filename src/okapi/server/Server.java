package okapi.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;
import okapi.client.Service;

import com.sun.net.httpserver.*;

public class Server {
	private  Class<?> module;
	public Server(Class<?> module) {
		super();
		this.module = module;
	}
	private void inspect() throws Exception{
		if(!Service.class.isAssignableFrom(this.module)) {
			throw new Exception("module_name " + this.module + " not inherite from okapi.client.Service");
		}
	}
	private void start(int port) throws IOException {
		HttpServer server = HttpServer.create(new InetSocketAddress(port), 100);
		server.createContext("/", new HttpServerHandler(this.module));
		server.setExecutor(Executors.newCachedThreadPool()); // creates a default executor
		server.start();

		System.out.println("Http server is working in : " + "http://localhost:" + port);
	}
	public void localRun(int port) {
		JSONObject body = new JSONObject();
		try {
			inspect();
			start(port);
		} catch(UnsupportedClassVersionError ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));		
			body.put("status", "exited");
			body.put("message", "Java版本过高，请使用Java 1.7编译.");
			body.put("debug", sw.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));				
			body.put("status", "exited");
			body.put("message", ex.getMessage());
			body.put("debug", sw.toString());
		}
	}
//	public static void main(String args[]) {
//		System.out.println("UserClassPath:" + System.getProperty("java.class.path"));
//		System.out.println("SystemClassPath:" + System.getProperty("sun.boot.class.path"));
//	}
}