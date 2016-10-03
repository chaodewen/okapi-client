package okapi.client;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.Request;
import com.ning.http.client.RequestBuilder;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okapi.util.Tools;

public class ServiceClient {
	/**
	 * 发送HTTP GET请求，URI参数为空，头信息为默认值。
	 * @param api_path 调用API的URI路径
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path) {
		return invokeAPI(api_path, "GET", null, null, null);
	}
	/**
	 * 发送HTTP GET请求，头信息为默认值。
	 * @param api_path 调用API的URI路径
	 * @param arg URI参数
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path,
			Map<String, String> arg) {
		return invokeAPI(api_path, "GET", arg, null, null);
	}
	/**
	 * 发送HTTP请求，URI参数和body为空，头信息为默认值。
	 * @param api_path 调用API的URI路径
	 * @param method HTTP方法
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path, String method) {
		return invokeAPI(api_path, method, null, null, null);
	}
	/**
	 * 发送HTTP请求，URI参数为空，头信息为默认值。
	 * @param api_path 调用API的URI路径
	 * @param method HTTP方法
	 * @param body 当请求为GET时可能不支持该参数
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path, String method,
			Object body) {
		return invokeAPI(api_path, method, null, null, body);
	}
	/**
	 * 发送HTTP请求，头信息为默认值。
	 * @param api_path 调用API的URI路径
	 * @param method HTTP方法
	 * @param arg URI参数
	 * @param body 当请求为GET时可能不支持该参数
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path, String method,
			Map<String, String> arg, Object body) {
		return invokeAPI(api_path, method, arg, null, body);
	}
	/**
	 * 发送HTTP请求。
	 * @param api_path 调用API的URI路径
	 * @param method HTTP方法
	 * @param arg URI参数
	 * @param headers 头信息
	 * @param body 当请求为GET时可能不支持该参数
	 * @return 调用结果。
	 */
	public static InvokeFuture invokeAPI(String api_path, String method,
			Map<String, String> arg, Map<String, String> headers, Object body) {
		if (!api_path.matches("https?://[^\\s]+")) {
			if (!api_path.startsWith("/")) {
				api_path = "/" + api_path;
			}
			if(System.getenv().containsKey("OKAPI_JAVA_SERVER")) {
				String address = System.getenv("OKAPI_JAVA_SERVER");
				if(address.endsWith("/")) {
					address = address.substring(0, address.lastIndexOf("/"));
				}
				api_path = address + api_path;
			}
			else {
				// Default Setting
//				api_path = "http://api.okapi.pub" + api_path;
				api_path = "http://localhost:6666" + api_path;
			}
		}
		return forwardHttp(method, api_path, arg, headers, body);
	}
	private static InvokeFuture forwardHttp(String method, String url,
			Map<String, String> arg, Map<String, String> hs, Object body) {
		method = method.toUpperCase();
		ByteBuffer bb = Tools.transToByteBuffer(body);
		byte[] bs = null;
		if (bb != null) {
			bs = bb.array();
		}
		
//		if(body instanceof JSONObject || body instanceof JSONArray) {
//			if(hs == null){
//				hs = new HashMap<String, String>();
//			}
//			hs.put("Content-Type", "application/json");
//		}
		
		AsyncHttpClient client = new AsyncHttpClient(new AsyncHttpClientConfig.Builder().setAcceptAnyCertificate(true).build());
		RequestBuilder builder = new RequestBuilder(method);
		builder.setUrl(url);
		if (arg != null && !arg.isEmpty()) {
			for (Entry<String, String> set : arg.entrySet()) {
				builder.addQueryParam(set.getKey(),  set.getValue());
			}
		}
		if (hs != null) {
			for (Entry<String, String> set : hs.entrySet()) {
				builder.setHeader(set.getKey(), set.getValue());
			}
		}
		if (!method.equals("GET")) {
			builder.setBody(bs);
		}
		
		Request req =  builder.build();
		System.out.println("HTTP:" + req.getUrl());

		Future<com.ning.http.client.Response> f = client.executeRequest(req);
		return new HttpInvokeFuture(f, client);
	}
}