package WebServer;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import LoadBalancer.LoadBalancer;

public class WebServer {
	public static void main(String[] args) throws Exception {
		HttpServer server = HttpServer.create(new InetSocketAddress(8000), 0);
		server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		server.createContext("/", new RootHandler());
		server.createContext("/raytracer", new LoadBalancer());
		server.createContext("/blurimage", new LoadBalancer());
		server.createContext("/enhanceimage", new LoadBalancer());
		server.start();
	}
}