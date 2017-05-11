package okapi.server;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okapi.annotation.DELETE;
import okapi.annotation.GET;
import okapi.annotation.POST;
import okapi.annotation.PUT;
import okapi.client.Response;
import okapi.client.Service;
import okapi.util.Tools;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class ApiRule{
	private String action;
	private String rule;
	private Method method;
	
	public String getAction() {
		return action;
	}
	public Method getMethod() {
		return method;
	}
	public String getRule(){
		return rule;
	}
	public ApiRule(String action, String rule, Method method) {
		super();
		this.action = action;
		this.rule = rule;
		this.method = method;
	}
	public boolean isMatched(String method, String api_path){
		if(!method.equalsIgnoreCase(action)){
			return false;
		}
		return Tools.isCompitable(rule, api_path);
	}
}

public class HttpServerHandler implements HttpHandler {
	private List<ApiRule> api = new ArrayList<ApiRule>();
	private Class<?> module;
	private Response rsp;
	public HttpServerHandler(Class<?> module) {
		super();
		this.module = module;
		for(Method m : module.getDeclaredMethods()) {
			if(m.getAnnotation(GET.class) != null){
				GET get = m.getAnnotation(GET.class);
				api.add(new ApiRule("GET", get.value(), m));
			}
			else if(m.getAnnotation(POST.class) != null) {
				POST post = m.getAnnotation(POST.class);
				api.add(new ApiRule("POST", post.value(), m));
			}
			else if(m.getAnnotation(PUT.class) != null) {
				PUT put = m.getAnnotation(PUT.class);
				api.add(new ApiRule("PUT", put.value(), m));
			}
			else if(m.getAnnotation(DELETE.class) != null) {
				DELETE delete = m.getAnnotation(DELETE.class);
				api.add(new ApiRule("DELETE", delete.value(), m));
			}
		}
	}
	@Override
	public void handle(HttpExchange httpExchange) throws IOException  {
		System.out.println("Starting to handle ...");
		try {
			// URI参数
			Map<String, String> arg = new HashMap<String, String>();
			if(httpExchange.getRequestURI().getQuery() != null) {
				for(String item : httpExchange.getRequestURI().getQuery().split("&")) {
					arg.put(item.substring(0, item.indexOf("=")), item.substring(item.indexOf("=") + 1, item.length()));
				}
			}
			// URI中问号前面的部分
			String api_path = httpExchange.getRequestURI().getPath();
			// 获取Headers
			Map<String ,String> headers = new HashMap<String, String>();
			Headers headersRaw = httpExchange.getRequestHeaders();
			if(!headersRaw.isEmpty()) {
				Iterator<String> it = headersRaw.keySet().iterator();
				while(it.hasNext()) {
					String key = it.next();
					headers.put(key, headersRaw.get(key).get(0));
				}
			}
			// HTTP方法
			String method = httpExchange.getRequestMethod();
			// 输入流
			ByteBuffer body = null;
			InputStream is = httpExchange.getRequestBody();
			if(is != null) {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] b = new byte[4096];
				int n = 0;
				while((n = is.read(b)) != -1) {
					baos.write(b, 0, n);
				}
				body = ByteBuffer.wrap(baos.toByteArray());
			}
			// 得到调用结果
			this.rsp = InvokeAPI(api_path, method, arg, headers, body);
			// 设置响应头信息
			if(rsp.getHeaders() != null && !rsp.getHeaders().isEmpty()) {
				Iterator<Entry<String, String>> iterator = rsp.getHeaders().entrySet().iterator();
				while(iterator.hasNext()) {
					Entry<String, String> entry = iterator.next();
					String key = entry.getKey();
					String value = entry.getValue();
					httpExchange.getResponseHeaders().add(key, value);
				}
			}
			// 设置响应头属性（状态码）及响应信息的长度
			httpExchange.sendResponseHeaders(rsp.getCode(), rsp.getBodyByBytes().length);
			// 设置输出流
			OutputStream outputStream = httpExchange.getResponseBody();
			outputStream.write(rsp.getBodyByBytes());
			outputStream.flush();
			outputStream.close();
			is.close();
			httpExchange.close();
			System.out.println("Exiting handling ...");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public Response InvokeAPI(String api_path, String method, Map<String, String> arg, Map<String, String> headers, ByteBuffer body) {
		if(!api_path.startsWith("/")) {
			api_path = "/" + api_path;
		}
		System.out.println("Invoke is incoming with api_path : " + api_path + " ...");
		Object obj = null;
		int code = 200;
		Map<String, String> h = new HashMap<String, String>();
		for(ApiRule  rule : this.api) {
			if(rule.isMatched(method, api_path)){
				Method m = rule.getMethod();
				System.out.println("Calling Method : "+ m.getName() + " ...");
				m.setAccessible(true);
				Object[] uriArg = Tools.getArg(rule.getRule(), api_path);
				if(uriArg.length == m.getParameterTypes().length) {
					try {
						Service svc = (Service) (module.newInstance());
						svc.Args = arg;
						svc.Headers = headers;
						svc.Body = body;
						// 调用函数
						obj = m.invoke(svc, uriArg);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException | InstantiationException e) {
						e.printStackTrace();
						StringWriter sw = new StringWriter();
						e.printStackTrace(new PrintWriter(sw));
						code = 501;
						obj = e.getMessage() + sw.toString();
					}
				}
				else {
					code = 502;
					obj =  "URI Param Quantity Error!";
					for(int i = 0; i < uriArg.length; i ++) {
						obj += i + ":" + uriArg.getClass().getName()+ "," + uriArg.toString() + "|";
					}
					for(int i = 0; i < m.getParameterTypes().length; i ++) {
						obj +=i + ":"  + m.getParameterTypes()[i].toString() + "|";
					}
				}
				// 本地测试结果不进行ByteBuffer包装
				if(obj instanceof Response) {
					return (Response) obj;
				}
				else if(obj instanceof JSONObject) {
					h.put("Content-Type", "application/json");
					return new Response(code, h, ((JSONObject) obj).toString());
				}
				else if (obj instanceof JSONArray) {
					h.put("Content-Type", "application/json");
					return new Response(code, h, ((JSONArray) obj).toString());
				}
				else if(obj instanceof byte[]) {
					h.put("Content-Type", "application/octet-stream");
					return new Response(code, h, (byte[]) obj);
				}
				else if(obj instanceof ByteBuffer) {
					h.put("Content-Type", "application/octet-stream");
					ByteBuffer bb = Tools.transToByteBuffer(obj);
					return new Response(code, h, bb);
				}
				else if(obj instanceof Integer) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, ((Integer) obj).toString());
				}
				else if(obj instanceof Long) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, ((Long) obj).toString());
				}
				else if(obj instanceof Short) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, ((Short) obj).toString());
				}
				else if(obj instanceof Double) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, ((Double) obj).toString());
				}
				else if(obj instanceof Float) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, ((Float) obj).toString());
				}
				else if(obj instanceof String) {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, (String) obj);
				}
				else {
					h.put("Content-Type", "text/plain");
					return new Response(code, h, obj);
				}
			}
		}
		code = 404;
		return new Response(code, h, "Matched Rule Not Found!");
	}
}