package okapi.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import com.ning.http.client.AsyncHttpClient;

public class HttpInvokeFuture extends InvokeFuture {
	Future<com.ning.http.client.Response> future;
	private AsyncHttpClient client;
	public HttpInvokeFuture(Future<com.ning.http.client.Response> future, AsyncHttpClient client) {
		this.future = future;
		this.client = client;
	}
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return this.future.cancel(mayInterruptIfRunning);
	}
	@Override
	public boolean isCancelled() {
		return this.future.isCancelled();
	}
	@Override
	public boolean isDone() {
		return this.future.isDone();
	}
	@Override
	public Response get() {
		try {
			return this.get(30000, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			return new Response(502, e.getMessage());
		}
	}
	@Override
	public Response get(long timeout, TimeUnit unit) {
		try {
			com.ning.http.client.Response r = this.future.get(timeout, unit);
			Map<String, String> hs = new HashMap<String, String>();
			Iterator<Entry<String, List<String>>> it = r.getHeaders().iterator();
			while(it.hasNext()) {
				Entry<String, List<String>> val = it.next();
				hs.put(val.getKey(), val.getValue().get(0));
			}
			return new Response(r.getStatusCode(), hs, r.getResponseBodyAsBytes());
		} catch (Exception e) { 
			e.printStackTrace();
			return new Response(502,  e.getMessage());
		}finally{
			client.close();
		}
	}
}
