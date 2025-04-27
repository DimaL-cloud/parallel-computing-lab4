package ua.dmytrolutsiuk.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import ua.dmytrolutsiuk.model.CommandRequest;
import ua.dmytrolutsiuk.model.CommandResponse;
import ua.dmytrolutsiuk.model.enums.CommandDataKey;
import ua.dmytrolutsiuk.model.enums.CommandResponseType;
import ua.dmytrolutsiuk.model.enums.ComputationStatus;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ClientSession implements Runnable {

    private final Socket socket;
    private final DataInputStream in;
    private final DataOutputStream out;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private int matrixSize;
    private int threadsAmount;
    private ComputationStatus computationStatus = ComputationStatus.NOT_STARTED;
    private long executionTime;
    private int[][] matrix;

    public ClientSession(Socket socket) throws IOException {
        this.socket = socket;
        this.in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        this.out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
    }

    @Override
    public void run() {
        try {
            while (!socket.isClosed()) {
                String jsonRequest = in.readUTF();
                log.debug("Received request: {} ({} bytes)", jsonRequest, jsonRequest.getBytes().length);
                CommandRequest commandRequest = objectMapper.readValue(jsonRequest, CommandRequest.class);
                handleCommand(commandRequest);
            }
        } catch (IOException e) {
            log.error("Error while processing client session", e);
        }
    }

    private void handleCommand(CommandRequest request) throws IOException {
        switch (request.commandRequestType()) {
            case INITIALIZE -> handleInitialize(request.data());
            case START -> handleStart();
            case GET_STATUS -> handleStatus();
            case GET_RESULT -> handleResult();
            default -> sendResponse(new CommandResponse(
                    CommandResponseType.ERROR,
                    Map.of(CommandDataKey.MESSAGE, "Unknown command")
            ));
        }
    }

    private void handleInitialize(Map<CommandDataKey, Object> data) throws IOException {
        if (computationStatus != ComputationStatus.NOT_STARTED) {
            sendResponse(new CommandResponse(
                    CommandResponseType.ERROR,
                    Map.of(CommandDataKey.MESSAGE, "Already initialized")
            ));
            return;
        }
        try {
            Object matrixSizeObj = data.get(CommandDataKey.MATRIX_SIZE);
            Object threadsAmountObj = data.get(CommandDataKey.THREADS_AMOUNT);

            if (matrixSizeObj == null || threadsAmountObj == null) {
                sendResponse(new CommandResponse(
                        CommandResponseType.ERROR,
                        Map.of(CommandDataKey.MESSAGE, "Missing parameters")
                ));
                return;
            }

            this.matrixSize = ((Number) matrixSizeObj).intValue();
            this.threadsAmount = ((Number) threadsAmountObj).intValue();

            if (matrixSize < 1 || threadsAmount < 1) {
                sendResponse(new CommandResponse(
                        CommandResponseType.ERROR,
                        Map.of(CommandDataKey.MESSAGE, "Matrix size and threads amount must be at least 1")
                ));
                return;
            }

            sendResponse(new CommandResponse(CommandResponseType.OK, new HashMap<>()));
        } catch (Exception e) {
            sendResponse(new CommandResponse(
                    CommandResponseType.ERROR,
                    Map.of(CommandDataKey.MESSAGE, "Invalid matrixSize or threadsAmount")
            ));
        }
    }

    private void handleStart() throws IOException {
        computationStatus = ComputationStatus.PROCESSING;
        new Thread(() -> this.processMatrix(matrixSize, threadsAmount)).start();
        sendResponse(new CommandResponse(
                CommandResponseType.OK,
                new HashMap<>()
        ));
    }

    private void handleStatus() throws IOException {
        sendResponse(new CommandResponse(
                CommandResponseType.OK,
                Map.of(CommandDataKey.STATUS, String.valueOf(computationStatus.getValue()))
        ));
    }

    private void handleResult() throws IOException {
        if (computationStatus != ComputationStatus.DONE) {
            sendResponse(new CommandResponse(
                    CommandResponseType.ERROR,
                    Map.of(CommandDataKey.MESSAGE, "Computation not finished yet")
            ));
            return;
        }
        sendResponse(new CommandResponse(
                CommandResponseType.OK,
                Map.of(
                        CommandDataKey.EXECUTION_TIME_MS, executionTime,
                        CommandDataKey.MATRIX_SIZE, matrixSize,
                        CommandDataKey.MATRIX, matrix
                )
        ));
    }

    private void sendResponse(CommandResponse response) throws IOException {
        String json = objectMapper.writeValueAsString(response);
        out.writeUTF(json);
        out.flush();
    }

    private void processMatrix(int matrixSize, int threadsAmount) {
        this.matrix = new int[matrixSize][matrixSize];
        Thread[] threads = new Thread[threadsAmount];
        long start = System.nanoTime();
        for (int i = 0; i < threadsAmount; i++) {
            threads[i] = new Thread(new MatrixColumnFiller(matrix, matrixSize, threadsAmount, i));
            threads[i].start();
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException ignored) {}
        }
        long end = System.nanoTime();
        executionTime = (end - start) / 1_000_000;
        computationStatus = ComputationStatus.DONE;
    }

}
