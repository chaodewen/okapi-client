package okapi.client;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import okapi.util.Tools;

public class Service {
	public Map<String, String> Args;
	public Map<String, String> Headers;
	public Object Body;
	
	public JSONObject getJSONObject() {
		return JSONObject.fromObject(this.Body);
	}
	public JSONArray getJSONArray() {
		return JSONArray.fromObject(this.Body);
	}
	public String getString() {
		try {
			return new String(getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return "";
		}
	}
	public char getChar() {
		byte[] bytes = getBytes();
		return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	public short getShort() {
		byte[] bytes = getBytes();
		return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
	}
	public int getInt() {
		byte[] bytes = getBytes();
		return (0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)) 
				| (0xff0000 & (bytes[2] << 16)) | (0xff000000 & (bytes[3] << 24));
	}
	public long getLong() {
		byte[] bytes = getBytes();
		return (0xffL & (long)bytes[0]) 
				| (0xff00L & ((long)bytes[1] << 8)) 
				| (0xff0000L & ((long)bytes[2] << 16)) 
				| (0xff000000L & ((long)bytes[3] << 24))
		        | (0xff00000000L & ((long)bytes[4] << 32)) 
		        | (0xff0000000000L & ((long)bytes[5] << 40)) 
		        | (0xff000000000000L & ((long)bytes[6] << 48)) 
		        | (0xff00000000000000L & ((long)bytes[7] << 56));
	}
	public float getFloat() {
		byte[] bytes = getBytes();
		return Float.intBitsToFloat((0xff & bytes[0]) 
				| (0xff00 & (bytes[1] << 8)) 
				| (0xff0000 & (bytes[2] << 16)) 
				| (0xff000000 & (bytes[3] << 24)));
	}
	public double getDouble() {
		byte[] bytes = getBytes();
		return Double.longBitsToDouble((0xffL & (long)bytes[0]) 
				| (0xff00L & ((long)bytes[1] << 8)) 
				| (0xff0000L & ((long)bytes[2] << 16)) 
				| (0xff000000L & ((long)bytes[3] << 24))
		        | (0xff00000000L & ((long)bytes[4] << 32)) 
		        | (0xff0000000000L & ((long)bytes[5] << 40)) 
		        | (0xff000000000000L & ((long)bytes[6] << 48)) 
		        | (0xff00000000000000L & ((long)bytes[7] << 56)));
	}
	public byte[] getBytes() {
		return Tools.transToBytes(this.Body);
	}

}
