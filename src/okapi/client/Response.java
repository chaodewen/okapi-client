package okapi.client;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okapi.util.Tools;

public class Response {
	private int code;
	private Map<String,String> headers;
	private Object body;
	public Response() {
		this.code = 200;
	}
	public Response(Object obj) {
		this.code = 200;
		this.body = obj;
	}
	public Response(int code, Object obj) {
		this.code = code;
		this.body = obj;
	}
	public Response(int code, Map<String, String> headers, Object obj) {
		this.code = code;
		this.body = obj;
		this.headers = headers;
	}
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public void setHeaders(Map<String, String> headers) {
		this.headers = headers;
	}
	public Object getBody() {
		return this.body;
	}
	public void setBody(Object obj) {
		this.body = obj;
	}
	public boolean isOK() {
		return this.code >= 200 && this.code < 300;
	}
	public JSONObject getBodyByJSONObject() {
		return JSONObject.fromObject(this.body);
	}
	public JSONArray getBodyByJSONArray() {
		return JSONArray.fromObject(this.body);
	}
	public String getBodyByString() {
		try {
			return new String(getBodyByBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	public char getBodyByChar() {
		byte[] bytes = getBodyByBytes();
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	public short getBodyByShort() {
		byte[] bytes = getBodyByBytes();
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	public int getBodyByInt() {
		byte[] bytes = getBodyByBytes();
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) 
				| (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
	}
	public long getBodyByLong() {
		byte[] bytes = getBodyByBytes();
		return (0xffL & (long)bytes[0]) 
				| (0xff00L & ((long)bytes[1] << 8)) 
				| (0xff0000L & ((long)bytes[2] << 16)) 
				| (0xff000000L & ((long)bytes[3] << 24))
		        | (0xff00000000L & ((long)bytes[4] << 32)) 
		        | (0xff0000000000L & ((long)bytes[5] << 40)) 
		        | (0xff000000000000L & ((long)bytes[6] << 48)) 
		        | (0xff00000000000000L & ((long)bytes[7] << 56));
	}
	public float getBodyByFloat() {
		byte[] bytes = getBodyByBytes();
		return Float.intBitsToFloat((0xff & bytes[0]) 
				| (0xff00 & (bytes[1] << 8)) 
				| (0xff0000 & (bytes[2] << 16)) 
				| (0xff000000 & (bytes[3] << 24)));
	}
	public double getBodyByDouble() {
		byte[] bytes = getBodyByBytes();
		return Double.longBitsToDouble((0xffL & (long)bytes[0]) 
				| (0xff00L & ((long)bytes[1] << 8)) 
				| (0xff0000L & ((long)bytes[2] << 16)) 
				| (0xff000000L & ((long)bytes[3] << 24))
		        | (0xff00000000L & ((long)bytes[4] << 32)) 
		        | (0xff0000000000L & ((long)bytes[5] << 40)) 
		        | (0xff000000000000L & ((long)bytes[6] << 48)) 
		        | (0xff00000000000000L & ((long)bytes[7] << 56)));
	}
	public byte[] getBodyByBytes() {
		return Tools.transToBytes(this.body);
	}
	@Override
	public String toString(){
		return "code: " + code + ", body: " + body.toString();
	}
}
