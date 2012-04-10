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
 * An embedded Jetty Server.
 * <p/>
 * User: pabe
 * Date: 2011-08-02
 * Time: 09:58
 */
public class JettyServer {

    private Server server;

    /**
     * Constructor.
     */
    public JettyServer() {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("beans-jetty.xml");
        server = (Server) context.getBean("server");
        server.addHandler(new SimpleLoggingHandler());
    }

    /**
     * Starts the server.
     *
     * @throws Exception Exception
     */
    public void startServer() throws Exception {
        server.start();
    }

    /**
     * Stops the server.
     *
     * @throws Exception Exception
     */
    public void stopServer() throws Exception {
        server.stop();
    }

    /**
     * Main method that creates and starts a server.
     *
     * @param args args
     * @throws Exception Exception
     */
    public static void main(String[] args) throws Exception {
        JettyServer server = new JettyServer();
        server.startServer();
    }

    private static class SimpleLoggingHandler extends AbstractHandler {
        @Override
        public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
                throws IOException, ServletException {
            Enumeration headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                Object name = headerNames.nextElement();
                String header = request.getHeader((String) name);
                System.out.println(name + ": " + header);
            }
            String requestURI = request.getRequestURI();
            System.out.println("requestURI:" + requestURI);
            System.out.println("=============================================");
        }
    }
}
