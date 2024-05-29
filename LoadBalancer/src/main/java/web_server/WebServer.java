package web_server;

import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

import load_balancer.LoadBalancer;

public class WebServer {
	public static void main(String[] args) throws Exception {
		System.out.println("LoadBalancer-Server started");
		int portnumber = 8001;
		HttpServer server = HttpServer.create(new InetSocketAddress(portnumber), 0);
		System.out.println("LoadBalancer running on Port: " + portnumber);
		server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
		server.createContext("/", new RootHandler());
		server.createContext("/raytracer", new LoadBalancer());
		server.createContext("/blurimage", new LoadBalancer());
		server.createContext("/enhanceimage", new LoadBalancer());
		server.start();
	}
}