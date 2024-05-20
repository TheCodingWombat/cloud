package web_server;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import load_balancer.LoadBalancer;

public class WebServer {
	public static void main(String[] args) throws Exception {
		System.out.println("LoadBalancer-Server started");
		HttpServer server = HttpServer.create(new InetSocketAddress(8001), 0);
		server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		server.createContext("/", new RootHandler());
		server.createContext("/raytracer", new LoadBalancer());
		server.createContext("/blurimage", new LoadBalancer());
		server.createContext("/enhanceimage", new LoadBalancer());
		server.start();
	}
}