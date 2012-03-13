package se.vgregion.jetty.embedded;

import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * User: pabe
 * Date: 2011-08-02
 * Time: 09:58
 */
public class JettyServer {

    private Server server;

    public JettyServer() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans-jetty.xml");
        server = (Server) context.getBean("server");
        server.addHandler(new AbstractHandler() {
            @Override
            public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch) throws IOException, ServletException {
                Enumeration headerNames = request.getHeaderNames();
                while(headerNames.hasMoreElements()) {
                    Object name = headerNames.nextElement();
                    String header = request.getHeader((String) name);
                    System.out.println(name + ": " + header);
                }
                String requestURI = request.getRequestURI();
                System.out.println("requestURI:" + requestURI);
                System.out.println("=============================================");
            }
        });
    }

    public void startServer() throws Exception {
        server.start();
    }

    public void stopServer() throws Exception {
        server.stop();
    }

    public static void main(String[] args) throws Exception {
        JettyServer server = new JettyServer();
        server.startServer();
    }
}
