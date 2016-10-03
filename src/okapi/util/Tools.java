package okapi.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class Tools {
	public static byte[] transToBytes(Object obj) {
		if(obj == null) {
			return null;
		}
		byte[] bytes = null;

		if(obj instanceof byte[]) {
			bytes = (byte[]) obj;
		} else if(obj instanceof JSONObject) {
			try {
				bytes = JSONObject.fromObject(obj).toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		} else if(obj instanceof JSONArray) {
			try {
				bytes = JSONArray.fromObject(obj).toString().getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		else if(obj instanceof ByteBuffer) {
			// 不一定正确的转换
			ByteBuffer bb = (ByteBuffer) obj;
			bytes = new byte[bb.remaining()];
			bb.get(bytes, 0, bytes.length);
			bb.clear();
			bytes = new byte[bb.capacity()];
			bb.get(bytes, 0, bytes.length);
		}
		else if(obj instanceof String) {
			String str = String.valueOf(obj);
			try {
				bytes = str.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
		else if(obj instanceof Character) {
			char c = ((Character) obj).charValue();
			bytes = new byte[2];
	        bytes[0] = (byte) (c);
	        bytes[1] = (byte) (c >> 8);
		}
		else if(obj instanceof Short) {
			short num = ((Short) obj).shortValue();
			bytes = new byte[2];
	        bytes[0] = (byte) (num & 0xff);
	        bytes[1] = (byte) ((num & 0xff00) >> 8);
		}
		else if(obj instanceof Integer) {
			int num = ((Integer) obj).intValue();
			bytes = new byte[4];
			bytes[0] = (byte) (num & 0xff);
	        bytes[1] = (byte) ((num & 0xff00) >> 8);
	        bytes[2] = (byte) ((num & 0xff0000) >> 16);
	        bytes[3] = (byte) ((num & 0xff000000) >> 24);
		}
		else if(obj instanceof Long) {
			long num = ((Long) obj).longValue();
			bytes = new byte[8];
			bytes[0] = (byte) (num & 0xff);
	        bytes[1] = (byte) ((num >> 8) & 0xff);
	        bytes[2] = (byte) ((num >> 16) & 0xff);
	        bytes[3] = (byte) ((num >> 24) & 0xff);
	        bytes[4] = (byte) ((num >> 32) & 0xff);
	        bytes[5] = (byte) ((num >> 40) & 0xff);
	        bytes[6] = (byte) ((num >> 48) & 0xff);
	        bytes[7] = (byte) ((num >> 56) & 0xff);
		}
		else if(obj instanceof Float) {
			int intBits = Float.floatToIntBits(((Float) obj).floatValue());
			bytes = new byte[4];
			bytes[0] = (byte) (intBits & 0xff);
	        bytes[1] = (byte) ((intBits & 0xff00) >> 8);
	        bytes[2] = (byte) ((intBits & 0xff0000) >> 16);
	        bytes[3] = (byte) ((intBits & 0xff000000) >> 24);
		}
		else if(obj instanceof Double) {
			long longBits = Double.doubleToLongBits(((Double) obj).doubleValue());
			bytes = new byte[8];
			bytes[0] = (byte) (longBits & 0xff);
	        bytes[1] = (byte) ((longBits >> 8) & 0xff);
	        bytes[2] = (byte) ((longBits >> 16) & 0xff);
	        bytes[3] = (byte) ((longBits >> 24) & 0xff);
	        bytes[4] = (byte) ((longBits >> 32) & 0xff);
	        bytes[5] = (byte) ((longBits >> 40) & 0xff);
	        bytes[6] = (byte) ((longBits >> 48) & 0xff);
	        bytes[7] = (byte) ((longBits >> 56) & 0xff);
		}
		else if(obj instanceof Serializable){
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			try {
				ObjectOutputStream oos = new ObjectOutputStream(baos);
				oos.writeObject(obj);
				bytes = baos.toByteArray();
				oos.close();
				baos.close();
			} catch (IOException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return bytes;
	}
	public static ByteBuffer transToByteBuffer(Object obj) {
		if(obj instanceof ByteBuffer) {
			return (ByteBuffer) obj;
		}
		else {
			byte[] bytes = Tools.transToBytes(obj);
			if(bytes == null) {
				return null;
			}
			else {
				return ByteBuffer.wrap(bytes);
			}
		}
	}
	public static String[] separateURI(String uri) {
		StringBuilder sb = new StringBuilder(uri);
		if(uri.startsWith("/")) {
			sb.deleteCharAt(0);
		}
		if(uri.endsWith("/")) {
			sb.deleteCharAt(uri.length() - 1);
		}
		return sb.toString().split("/");
	}
	public static boolean isCompitable(String def, String target) {
		String[] defPart = Tools.separateURI(def);
		String[] targetPart = Tools.separateURI(target);
		if(defPart.length == targetPart.length) {
			for(int i = 0; i < defPart.length; i++) {
				if(defPart[i].length() == 0 || targetPart[i].length() == 0) {
					return false;
				}
				if(defPart[i].matches("<[a-zA-Z][\\w]+:[\\w-?%&={}]+>")) {
					String type = defPart[i].substring(1, defPart[i].indexOf(":"));
					String value = targetPart[i];
					if(type.equals("boolean") && value.matches("true|false")) {
						try {
							Boolean.parseBoolean(value);
							continue;
						}
						catch (NumberFormatException e) {
							System.out.println("boolean类型错误！");
							return false;
						}
					}
					else if(type.equals("String")) {
						continue;
					}
					else if(type.equals("int") && value.matches("\\d+")) {
						try {
							Integer.parseInt(value);
							continue;
						}
						catch (NumberFormatException e) {
							System.out.println("int类型错误！");
							return false;
						}
					}
					else if(type.equals("double") && value.matches("\\d+(.\\d+)?")) {
						try {
							Double.parseDouble(value);
							continue;
						}
						catch (NumberFormatException e) {
							System.out.println("double类型错误！");
							return false;
						}
					}
					else if(type.equals("float") && value.matches("\\d+(.\\d+)?")) {
						try {
							Float.parseFloat(value);
							continue;
						}
						catch (NumberFormatException e) {
							System.out.println("float类型错误！");
							return false;
						}
					}
					else {
						return false;
					}
				}
				else if(!defPart[i].equals(targetPart[i])){
					return false;
				}
			}
			return true;
		}
		else {
			return false;
		}
	}
	public static Object[] getArg( String def, String api_path_last) {
		ArrayList<Object> al = new ArrayList<Object>();
		String[] defPart = Tools.separateURI(def);
		String[] lastPart = Tools.separateURI(api_path_last);
		for(int i = 0; i < defPart.length; i++) {
			if(defPart[i].matches("<[a-zA-Z][\\w]+:[\\w-?%&={}]+>")) {
				String type = defPart[i].substring(1, defPart[i].indexOf(":"));
				String value = lastPart[i];
				if(type.equals("boolean") && value.matches("true|false")) {
					al.add(Boolean.valueOf(value));
				}
				else if(type.equals("String")) {
					al.add(value);
				}
				else if(type.equals("int") && value.matches("\\d+")) {
					al.add(Integer.valueOf(value));
				}
				else if(type.equals("double") && value.matches("\\d+(.\\d+)?")) {
					al.add(Double.valueOf(value));
				}
				else if(type.equals("float") && value.matches("\\d+(.\\d+)?")) {
					al.add(Float.parseFloat(value));
				}
			}
		}
		return al.toArray();
	}
}
