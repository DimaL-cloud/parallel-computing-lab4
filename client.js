const net = require('net');
const readline = require('readline');

const SERVER_ADDRESS = 'localhost';
const SERVER_PORT = 8080;

const rl = readline.createInterface({
    input: process.stdin,
    output: process.stdout
});

const objectMapper = {
    serialize: (obj) => JSON.stringify(obj),
    deserialize: (str) => JSON.parse(str)
};

function sendRequest(socket, request) {
    const json = objectMapper.serialize(request);
    const buffer = Buffer.alloc(2 + Buffer.byteLength(json, 'utf8'));
    buffer.writeUInt16BE(Buffer.byteLength(json, 'utf8'), 0);
    buffer.write(json, 2);
    socket.write(buffer);
}

function readResponse(data) {
    const length = data.readUInt16BE(0);
    const jsonString = data.slice(2, 2 + length).toString('utf8');
    return objectMapper.deserialize(jsonString);
}

function askQuestion(query) {
    return new Promise(resolve => rl.question(query, resolve));
}

async function askMatrixParams() {
    while (true) {
        const matrixSizeInput = await askQuestion('Enter matrix size (>= 1): ');
        const threadsAmountInput = await askQuestion('Enter number of threads (>= 1): ');

        const matrixSize = parseInt(matrixSizeInput.trim(), 10);
        const threadsAmount = parseInt(threadsAmountInput.trim(), 10);

        if (Number.isInteger(matrixSize) && Number.isInteger(threadsAmount) && matrixSize >= 1 && threadsAmount >= 1) {
            return { matrixSize, threadsAmount };
        } else {
            console.log('Matrix size and threads amount must be integers >= 1. Please try again.\n');
        }
    }
}

async function main() {
    try {
        const { matrixSize, threadsAmount } = await askMatrixParams();

        const initParams = {
            1: matrixSize,
            2: threadsAmount
        };

        console.log('Connecting to server...');
        const socket = new net.Socket();

        socket.connect(SERVER_PORT, SERVER_ADDRESS, async () => {
            console.log('Connected to server');

            console.log('Sending INITIALIZE request with params:', initParams);
            const initializeRequest = {
                0: 0,
                1: initParams
            };
            sendRequest(socket, initializeRequest);
            await waitResponse(socket);

            console.log('Sending START request');
            const startRequest = {
                0: 1,
                1: {}
            };
            sendRequest(socket, startRequest);
            await waitResponse(socket);

            while (true) {
                await new Promise(resolve => setTimeout(resolve, 500));
                const statusRequest = {
                    0: 2,
                    1: {}
                };
                sendRequest(socket, statusRequest);

                console.log('Sending GET_STATUS request');
                const statusResponse = await waitResponse(socket);

                const statusCode = parseInt(statusResponse[1][3]);
                console.log('Status code:', statusCode);

                if (statusCode === 2) {
                    break;
                }
            }

            console.log('Sending GET_RESULT request');
            const resultRequest = {
                0: 3,
                1: {}
            };
            sendRequest(socket, resultRequest);

            const resultResponse = await waitResponse(socket);

            console.log('Execution time (ms):', resultResponse[1][4]);
            console.log('Matrix size:', resultResponse[1][1]);
            console.log('Matrix received!');

            const matrix = resultResponse[1][5];
            if (Array.isArray(matrix)) {
                console.log('\nFull matrix:');
                matrix.forEach(row => {
                    console.log(row.join(' '));
                });
            }

            socket.end();
            rl.close();
        });

        socket.on('error', (err) => {
            console.error('Socket error:', err);
            rl.close();
        });

    } catch (error) {
        console.error('Error:', error);
        rl.close();
    }
}

function waitResponse(socket) {
    return new Promise(resolve => {
        socket.once('data', (data) => {
            const response = readResponse(data);
            if (response[0] === 0) {
                console.log('Response:', response);
            } else if (response[0] === 1) {
                console.error('Error response:', response);
            }
            resolve(response);
        });
    });
}

main();
