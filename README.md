# okapi-client

项目okapi是一套服务组合开源框架，能够实现不同语言API间的相互调用，并简单地生成新的组合API。本项目是okapi的组成部分，作为okapi的Java语言SDK，它主要用于辅助开发者进行服务组合，生成新的符合okapi规则的API。

#### 在Java中创建OKAPI服务的过程如下：

1. 在 Eclipse 中创建工程，下载库文件 [okapiclient.jar](http://cmsci.net/xiaomishu/okapi_demo/blob/master/okapiclientpublic.jar)，并在引入 okapiclient.jar 库文件。

2. 新建一个继承 okapi.client.Service 的类，继承了 Service 的类可以被 OKAPI 平台识别为服务文件的入口类，在网站上传文件时 module_name 填写为该类的全名（包含包名，如 com.example.WeatherService）。

3. 在类中添加方法，正确添加注解的方法会被系统识别为 API，注解名称包括 okapi.annotaion 包下的 GET、POST、PUT 和 DELETE 四种，表示访问 API 的四种 HTTP 动作；还包含一个表示 RESTful 资源的路径参数，表示访问 API 的路径，路径中可通过“/\<类型:参数名\>”添加方法参数（如“/\<String:str\>”），也可以通过路径中问号后的部分传入 URI 参数（如“/adder?a=3&b=4”）。

4. 完成后的类在需要打包为不含 okapiclient.jar 的 Jar 包，再压缩为 ZIP 文件（入口 Jar 文件需在 ZIP 根目录下）上传，一种简单的做法是在 eclipse 中输出工程为 Jar 文件（不是可执行的 Jar 文件），再将引用的包（除 okapiclient.jar 外）放在 Jar 的根目录下，然后将这些输出的包和引用的包压缩为一个 ZIP 文件（Jar 全在该 ZIP 根目录下），即可上传。

5. 在 http://www.okapi.pub 的用户个人中心创建服务并上传 ZIP 包即可。

### 简单示例：

```java

@GET("/add/<int:a>/<int:b>")
public int add(int a, int b){
    return a + b;
}

```
 > 表示使用 HTTP GET 方法访问路径“/add/2/3”（实际访问时路径中还要加入用户名、服务名等信息，个人中心将会显示）时将匹配到该方法，此时 a=2，b=3 返回结果为 5。

### 完整示例

二维码服务（包括一个二维码识别 API）：

```java

// 代码中的注释为简单说明，详细介绍下文
// Response 为 OKAPI 提供的包装类型
// 包括 int code、Map<String,String> headers 和 Object body 三个私有数据域
// Response 中含有操作这些域的函数
import okapi.client.Response;
import okapi.client.Service;
import okapi.client.ServiceClient;

... ...
... ...

public class QRCode extends Service {
	/**
     * 生成二维码(QRCode)图片的API，不带path参数时返回byte[]，带path参数（UTF-8编码字符串）在路径下创建图片文件
     * @param content：存储内容
     * @param fileName：带图片文件后缀的文件名，如“test.png”
     * @param path：存储路径，中间用“.”分隔，如“Users.Cc.Desktop”（非必填）
     * @param size：二维码尺寸，范围1~40，默认值为5（非必填）
     * @return 200：byte[]流形式的图片文件 201：创建图片文件成功，有路径 其它：出错
     */
	@POST("/qrcode/content/<String:content>")
	public Response createQRCode(String content) {
		// Service 中包括 Map<String,String> Args、Map<String,String> Headers 和 Object Body 三个数据域
		// 其中 Args 存储 URI 路径中的参数
		// 如“/qrcode/content/H*lloW**ld?fileName=test.png”（非完整路径）
		if(this.Args.containsKey("fileName")) {
			String fileName = this.Args.get("fileName");
			int size = 5;
			if(this.Args.containsKey("size")) {
				size = Integer.parseInt(this.Args.get("size"));
				if(size < 1 || size > 40) {
					return new Response(400, 
							"createQRPic:Paramater \"size\" Must Be among 1 to 40!");
				}
			}
			
			... ...
			... ...

			// 例如这里用到了用户 Turing 的 Transportation 服务二号版本中名为 decryption 的 API
			// 那么可以通过 ServiceClient 中的 invokeAPI 函数调用
			// arg 对应的参数为 URI 参数，与上文中的 fileName 为同一类型参数的两种不同传入方法
			// 返回值为 InvokeFuture 类型，该类型对用户而言意义不大
			// 直接调用 get() 获得 Response 类型对象即可
			Map<String, String> arg = new HashMap<String, String>();
			arg.put("content", content);
			Map<String, String> headers = new HashMap<String, String>();
			headers.put("User-Agent", "Mozilla/5.0");
			Response rsp = ServiceClient.invokeAPI(
				"/Turing/Transportation/2/decryption", "GET", arg, headers, null).get();

			// 检查 rsp.getCode() 值是否代表正常返回
			if(rsp.isOK) {
				if(rsp.getHeaders().get("Content-Type").equals("text/plain")) {
					String realContent = rsp.getBodyByString();

					... ...
					... ...

				}
			}
			else {

				... ...
				... ...

				// 上述 API 返回值异常时调用网络中的解码 API（例中地址虚构，如有雷同，纯属巧合）
				Response rspHTTP = (
					"http://api.turing.com/transportation/decryption?content=" + content).get();
				
				... ...
				... ...

			}

			... ...
			... ...

			try {

				... ...
				... ...

			} catch (UnsupportedEncodingException e) {
				System.out.println("createQRPic:" + e.getMessage());
				return new Response(400, "createQRPic:" + e.getMessage());
			}
			
			if(this.Args.containsKey("path")) {
				String path = this.Args.get("path");

				... ...
				... ...

				try {

					... ...
					... ...

				} catch (IOException e) {
					System.out.println("createQRPic:" + e.getMessage());
					return new Response(500, "createQRPic:" + e.getMessage());
				}
				return new Response(201, "createQRPic:" + path);
			}
			else {

				... ...
				... ...

			}
		}
		else {

			... ...
			... ...

		}
    }
    public static void main(String[] args) {
        MethodChecker.checkMethod();
    }
}

```

### 说明&注意事项

1. 继承 Service类的同时继承了 Map<String,String> Args、Map<String,String> Headers 和 Object Body 三个私有数据域。Args 存储 URI 参数（如上述示例中的 fileName），Headers 存储收到 HTTP 请求的头信息（通过 this.Headers 获取），Body 为 POST、PUT、DELETE 等方法发送请求中包含的请求正文（通过 this.Body 获取）。

2. OKAPI 的 Java 框架定义了一种 okapi.client.Response 类型，包括 int code、Map<String,String> headers、Object body 三个私有数据域和若干对这些域的操作方法（如上述isOK()、getHeaders()、getBodyByString()等）。

3. API 方法推荐以 Response 类型返回（如上述 new Response()方法）；同时OKAPI框架提供机制支持 API 方法以任意类型（如基本数据类型、JSONObject 等）作为返回值，框架内部会将返回值转换为 Response 类型提供给调用者。在该 Response 中，code 值为 200，body 包含返回数据。当返回 Map 时，body 的实际类型（这里称为实际类型是因为用户正常得到的 body 是 Object 类型）为 JSONObject；当返回 List 时，body 为 JSONArray，其他类型 body 为返回值调用 toString() 方法的结果。

4. API 方法中可以调用 OKAPI 平台中的其它任何有权限的服务（包括不同代码编写的服务）或者外部 HTTP 服务，方便进行包括非 OKAPI 服务的服务组合，调用方法为 InvokeAPIClient.invokeAPI(String api_path, String method, Map<String, String> arg, Map<String, String> headers, Object body)，此方法为异步调用，获取最终的返回值需要执行 get() 方法得到 Response 类型的结果。其中 api_path 传入接收请求的 URI 路径（可以带 URI 参数）；method 的传入请求的 HTTP 方法（支持任意大小写）；arg 是传入 URI 参数的另一种方式；headers 传入 HTTP 头信息；body 以 Object 类型传入 HTTP 响应正文。
 
5. 可以在 Service 中添加如例子所示的 main() 方法，调用 MethodChecker.checkMethod() 检查上传的 Service 是否符合要求。该方法执行后会开启本地服务器以方便用户测试，用户可以在本地调用上述 invokeAPI() 函数对服务进行访问。
