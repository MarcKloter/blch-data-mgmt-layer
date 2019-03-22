package bdml.api;

import bdml.api.jsonrpc.CoreProxy;
import bdml.api.websocket.CoreWebSocket;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import org.apache.commons.cli.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import spark.Spark;

public class Starter {
    private static final String DEFAULT_PORT = "8550";
    private static final Logger LOGGER = LogManager.getLogger(Starter.class);

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

        Spark.initExceptionHandler(e -> {
            LOGGER.error(e.getMessage());
            System.exit(1);
        });
        
        CoreProxy coreService;
        try {
            coreService = new CoreProxy();
        } catch (ExceptionInInitializerError e) {
            LOGGER.error(e.getCause().getMessage());
            return;
        }

        JsonRpcServer rpcServer = new JsonRpcServer();

        // WebSocket endpoint
        Spark.webSocket("/dataListener", CoreWebSocket.class);

        LOGGER.info(String.format("WebSocket endpoint listening on wss://localhost:%d/dataListener", port));

        // HTTPS POST routing
        Spark.post("/", (request, response) -> {
            String jsonRequest = request.body();
            return rpcServer.handle(jsonRequest, coreService);
        });

        LOGGER.info(String.format("JSON-RPC API endpoint listening on https://localhost:%d", port));
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
            LOGGER.error(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }

        return cmd;
    }
}
