package bdml.core;

import bdml.core.jsonrpc.CoreProxy;
import com.github.arteam.simplejsonrpc.server.JsonRpcServer;
import org.apache.commons.cli.*;

import java.io.*;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

import static spark.Spark.*;

public class Starter {
    private final static String DEFAULT_PORT = "8550";

    public static void main(String[] args) throws FileNotFoundException, IOException {
        CommandLine cmd = handleCLIArguments(args);

        // optional port argument
        int port = Integer.parseInt(cmd.getOptionValue("port", DEFAULT_PORT));

        port(port);

        // optional truststore arguments
        String truststoreFile = cmd.getOptionValue("truststore", null);
        String truststorePassword = cmd.getOptionValue("truststore-password", null);

        // required keystore arguments
        String keystoreFile = getLocation() + cmd.getOptionValue("keystore");
        String keystorePassword = cmd.getOptionValue("password");

        secure(keystoreFile, keystorePassword, truststoreFile, truststorePassword);

        // TODO: exceptions thrown by core proxy are being e.printStackTrace()'ed

        CoreProxy coreService = new CoreProxy();
        JsonRpcServer rpcServer = new JsonRpcServer();

        // HTTPS POST routing
        post("/", (request, response) -> {
            String jsonRequest = request.body();
            return rpcServer.handle(jsonRequest, coreService);
        });

        // TODO: websocket endpoint
        // TODO: HTTPS extension routing

        System.out.println("JSON-RPC API endpoint listening on https://localhost:" + port);
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

    /**
     * Returns the absolute path to the jar file.
     * Evaluates to bdml.core/target/classes/ during development.
     *
     * @return String defining the absolute path to the directory in which the jar file resides or null in case of an error.
     */
    private static String getLocation() {
        try {
            String path = Starter.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }
}
