package com.dubture.getcomposer.httpclient;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.HttpParams;

/**
 * Used as an adapter for HttpAsyncClient until it becomes available
 * as a stable version in eclipse orbit updatesite. 
 * 
 * @see http://hc.apache.org/httpcomponents-asyncclient-dev/httpasyncclient/apidocs/overview-summary.html
 * @author Robert Gruendler <r.gruendler@gmail.com>
 */
public interface AsyncClientInterface {

	void start() throws IOReactorException;

	HttpParams getParams();

	void execute(HttpGet httpGet, FutureCallback<HttpResponse> futureCallback);

	void shutdown() throws InterruptedException;
	
	HttpClient getBackend();

}