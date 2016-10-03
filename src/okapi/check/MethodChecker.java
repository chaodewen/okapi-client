package okapi.check;

import java.lang.reflect.Method;

import okapi.annotation.DELETE;
import okapi.annotation.GET;
import okapi.annotation.POST;
import okapi.annotation.PUT;
import okapi.server.Server;

public class MethodChecker {
	public static void checkMethod() {
		StackTraceElement stack[] = Thread.currentThread().getStackTrace();
		String className = stack[2].getClassName();
		try {
			for(Method m : Class.forName(className).getDeclaredMethods()) {
				String mtd = m.getName();
				String value = "";
				
				if(m.getAnnotation(GET.class) != null) {
					value = m.getAnnotation(GET.class).value();
				}else if(m.getAnnotation(POST.class) != null) {
					value = m.getAnnotation(POST.class).value();
				}else if(m.getAnnotation(PUT.class) != null) {
					value = m.getAnnotation(PUT.class).value();
				}else if(m.getAnnotation(DELETE.class) != null) {
					value = m.getAnnotation(PUT.class).value();
				}else {
					System.out.println("方法" + m.getName() + "未定义调用方法.");				
					continue;
				}
				if(value.matches("^(/([^/]+))+$")) {
					System.out.println("方法"+mtd+"通过语法检查，URI定义为：" + value + ".");
				}
				else {
					System.out.println("方法" + m.getName() + "的URI值" + value + "不符合语法规则.");
					continue;
				}
			}
			Server server = new Server(Class.forName(className));
			server.localRun(6666);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static void main(String[] args) {
		System.out.println("UserClassPath:" + System.getProperty("java.class.path"));
		System.out.println("SystemClassPath:" + System.getProperty("sun.boot.class.path"));
	}
}