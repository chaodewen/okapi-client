package okapi.client;

import java.util.concurrent.Future;

public abstract class InvokeFuture implements Future<Response> {
	@Override
	public abstract Response get();
}