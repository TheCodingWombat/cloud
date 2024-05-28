package pt.ulisboa.tecnico.cnv.webserver;

import java.net.InetSocketAddress;
import com.sun.net.httpserver.HttpServer;

import pt.ulisboa.tecnico.cnv.imageproc.BlurImageHandler;
import pt.ulisboa.tecnico.cnv.imageproc.EnhanceImageHandler;
import pt.ulisboa.tecnico.cnv.raytracer.RaytracerHandler;

public class WebServer {
    public static void main(String[] args) throws Exception {
    	System.out.println("Start Worker-Webservice");
    	int socketNum = 8000;
        HttpServer server = HttpServer.create(new InetSocketAddress(socketNum), 0);
        System.out.println("Worker-Webservice running on Port: " + socketNum);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.createContext("/", new RootHandler());
        server.createContext("/raytracer", new RaytracerHandler());
        server.createContext("/blurimage", new BlurImageHandler());
        server.createContext("/enhanceimage", new EnhanceImageHandler());
        server.start();
    }
}
