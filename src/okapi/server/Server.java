package okapi.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

import net.sf.json.JSONObject;
import okapi.client.Service;

import com.sun.net.httpserver.*;
import com.sun.net.httpserver.spi.HttpServerProvider;

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
		HttpServerProvider httpServerProvider = HttpServerProvider.provider();
		// 设置端口和能同时接受的请求数
		HttpServer httpServer = httpServerProvider.createHttpServer(new InetSocketAddress(port), 100);
//		HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 100);
		httpServer.createContext("/", new HttpServerHandler(this.module));
		httpServer.setExecutor(Executors.newCachedThreadPool());
//		httpServer.setExecutor(null);
		httpServer.start();
		System.out.println("HttpServer Started.");
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
			body.put("message", "Java版本过高，请使用Java1.7编译API");
			body.put("debug", sw.toString());
		} catch (IOException ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));		
			body.put("status", "exited");
			body.put("message", ex.getMessage());
			body.put("debug", sw.toString());
		} catch (Exception ex) {
			ex.printStackTrace();
			StringWriter sw = new StringWriter();
			ex.printStackTrace(new PrintWriter(sw));				
			body.put("status", "exited");
			body.put("message", ex.getMessage());
			body.put("debug", sw.toString());
		} finally {
			System.out.println("process exit");
		}
	}
//	public static void main(String args[]) {
//		System.out.println("UserClassPath:" + System.getProperty("java.class.path"));
//		System.out.println("SystemClassPath:" + System.getProperty("sun.boot.class.path"));
//	}
}