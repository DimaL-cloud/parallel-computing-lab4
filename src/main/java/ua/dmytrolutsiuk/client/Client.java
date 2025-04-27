package ua.dmytrolutsiuk.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ua.dmytrolutsiuk.model.CommandRequest;
import ua.dmytrolutsiuk.model.CommandResponse;
import ua.dmytrolutsiuk.model.enums.CommandDataKey;
import ua.dmytrolutsiuk.model.enums.CommandRequestType;
import ua.dmytrolutsiuk.model.enums.CommandResponseType;
import ua.dmytrolutsiuk.model.enums.ComputationStatus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 8080;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
             DataOutputStream out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
             Scanner scanner = new Scanner(System.in)) {

            int matrixSize;
            int threadsAmount;

            while (true) {
                System.out.print("Enter matrix size (>= 1): ");
                matrixSize = Integer.parseInt(scanner.nextLine());

                System.out.print("Enter number of threads (>= 1): ");
                threadsAmount = Integer.parseInt(scanner.nextLine());

                if (matrixSize >= 1 && threadsAmount >= 1) {
                    break;
                } else {
                    System.out.println("Matrix size and threads amount must be at least 1. Please try again.");
                }
            }

            Map<CommandDataKey, Object> initParams = new HashMap<>();
            initParams.put(CommandDataKey.MATRIX_SIZE, matrixSize);
            initParams.put(CommandDataKey.THREADS_AMOUNT, threadsAmount);

            System.out.println("Sending INITIALIZE request with params: " + initParams);

            CommandRequest initializeRequest = new CommandRequest(CommandRequestType.INITIALIZE, initParams);
            sendRequest(out, initializeRequest);

            readResponse(in);

            System.out.println("Sending START request");
            CommandRequest startRequest = new CommandRequest(CommandRequestType.START, new HashMap<>());
            sendRequest(out, startRequest);

            readResponse(in);

            while (true) {
                Thread.sleep(500);
                CommandRequest statusRequest = new CommandRequest(CommandRequestType.GET_STATUS, new HashMap<>());
                sendRequest(out, statusRequest);

                System.out.println("Sending GET_STATUS request");
                CommandResponse statusResponse = readResponse(in);

                int statusCode = Integer.parseInt(String.valueOf(statusResponse.data().get(CommandDataKey.STATUS)));
                ComputationStatus status = ComputationStatus.fromValue(statusCode);

                System.out.println("Status: " + status);

                if (status == ComputationStatus.DONE) {
                    break;
                }
            }

            System.out.println("Sending GET_RESULT request");
            CommandRequest resultRequest = new CommandRequest(CommandRequestType.GET_RESULT, new HashMap<>());
            sendRequest(out, resultRequest);

            CommandResponse resultResponse = readResponse(in);

            System.out.println("Execution time (ms): " + resultResponse.data().get(CommandDataKey.EXECUTION_TIME_MS));
            System.out.println("Matrix size: " + resultResponse.data().get(CommandDataKey.MATRIX_SIZE));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void sendRequest(DataOutputStream out, CommandRequest request) throws Exception {
        String json = objectMapper.writeValueAsString(request);
        out.writeUTF(json);
        out.flush();
    }

    private static CommandResponse readResponse(DataInputStream in) throws Exception {
        String responseJson = in.readUTF();
        CommandResponse response = objectMapper.readValue(responseJson, new TypeReference<>() {});

        if (response.commandResponseType() == CommandResponseType.ERROR) {
            System.err.println("Error: " + response);
        } else {
            System.out.println("Response: " + response);
        }

        return response;
    }
}
