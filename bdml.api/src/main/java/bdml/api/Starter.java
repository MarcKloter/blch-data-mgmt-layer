package bdml.api;

import bdml.api.jsonrpc.CoreProxy;
import bdml.api.websocket.CoreWebSocket;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import org.apache.commons.cli.*;
import spark.Spark;

public class Starter {
    private final static String DEFAULT_PORT = "8550";

    public static void main(String[] args) {
        CommandLine cmd = handleCLIArguments(args);

        // optional port argument
        int port = Integer.parseInt(cmd.getOptionValue("port", DEFAULT_PORT));

        Spark.port(port);

        // optional truststore arguments
        String truststoreFile = cmd.getOptionValue("truststore", null);
        String truststorePassword = cmd.getOptionValue("truststore-password", null);

        // required keystore arguments
        String keystoreFile = cmd.getOptionValue("keystore");
        String keystorePassword = cmd.getOptionValue("password");

        Spark.secure(keystoreFile, keystorePassword, truststoreFile, truststorePassword);

        // TODO: exceptions thrown by core proxy are being e.printStackTrace()'ed

        CoreProxy coreService = new CoreProxy();
        JsonRpcServer rpcServer = new JsonRpcServer();

        // WebSocket endpoint
        Spark.webSocket("/dataListener", CoreWebSocket.class);

        System.out.println(String.format("WebSocket endpoint listening on wss://localhost:%d/dataListener", port));

        // HTTPS POST routing
        Spark.post("/", (request, response) -> {
            String jsonRequest = request.body();
            return rpcServer.handle(jsonRequest, coreService);
        });

        System.out.println(String.format("JSON-RPC API endpoint listening on https://localhost:%d", port));
    }

    /**
     * Configures Apache Commons CLI for this project and parses the supplied arguments.
     *
     * @param args Command line arguments.
     * @return Parsed command line arguments.
     */
    private static CommandLine handleCLIArguments(String[] args) {
        Options options = new Options();

        // optional argument port
        options.addOption(null,"port", true, String.format("Port to listen to. Default: %s.", DEFAULT_PORT));

        // mandatory argument keystore
        options.addRequiredOption("k","keystore", true, "Keystore containing key material for the SSL socket to use.");

        // mandatory argument password
        options.addRequiredOption("p", "password", true, "The password to opten the keystore.");

        // optional argument truststore
        options.addOption(null,"truststore", true, "Truststore containing trust material for the SSL socket to base trust decisions on.");

        // optional argument truststore-password
        options.addOption(null,"truststore-password", true, "The password to open the truststore.");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;
        try {
            cmd = parser.parse(options, args);
        } catch(ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        return cmd;
    }
}
