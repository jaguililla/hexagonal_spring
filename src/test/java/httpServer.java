import com.sun.net.httpserver.HttpServer;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.net.HttpURLConnection.HTTP_OK;
import static java.nio.charset.StandardCharsets.UTF_8;

class httpServer {
    private static final String JSON = "application/json; charset=" + UTF_8.name();

    public static void main(String... args) throws Exception {
        var mockPort = System.getenv("OPENID_MOCK_PORT");
        var mockContext = System.getenv("OPENID_MOCK_CONTEXT");
        var mockRoot = System.getenv("OPENID_MOCK_ROOT");

        var port = Integer.parseInt(mockPort == null ? "12345" : mockPort);
        var context = mockContext == null ? "/context" : mockContext;
        var root = mockRoot == null ? "src/test/resources" : mockRoot;

        var rootPath = Path.of(root);
        var httpServer = HttpServer.create(new InetSocketAddress(port), 0);

        httpServer.createContext(context, exchange -> {
            var uriPath = exchange.getRequestURI().getPath();
            var path = uriPath.replaceFirst("^" + context + "/?", "");
            var host = exchange.getRequestHeaders().getFirst("Host");

            var responsePath = rootPath.resolve(path);
            var responseTemplate = Files.readAllBytes(responsePath);
            var responseText = new String(responseTemplate).replaceAll("<binding>", host);
            var response = responseText.getBytes();

            exchange.getResponseHeaders().set("Content-Type", JSON);
            exchange.sendResponseHeaders(HTTP_OK, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });

        httpServer.start();
        System.err.println("Running on: http://localhost:" + port);
    }
}
