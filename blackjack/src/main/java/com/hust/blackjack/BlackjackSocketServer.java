package com.hust.blackjack;

import com.hust.blackjack.service.RequestProcessingService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

@Service
@Slf4j
public class BlackjackSocketServer {
    private final int port;
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ByteBuffer commonBuffer = ByteBuffer.allocate(10000);

    private final RequestProcessingService processingService;

    public BlackjackSocketServer(RequestProcessingService processingService,
                                 @Value("${server-port}") int port) throws IOException {
        this.processingService = processingService;

        //Create TCP server channel
        this.port = port;
        this.serverSocketChannel = ServerSocketChannel.open();
        this.serverSocketChannel.socket().bind(new InetSocketAddress(this.port));
        this.serverSocketChannel.configureBlocking(false);

        //Create a selector to attend all the incoming requests
        this.selector = Selector.open();
        this.serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        commonBuffer.clear();
    }

    public void run() {
        try {
            while (serverSocketChannel.isOpen()) {
                log.info("Waiting for events......");
                if (selector.select() <= 0) // blocking call
                    continue;
                log.info("New event received");

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    log.info("Processing key: {}", key);

                    try {
                        if (key.isAcceptable()) {
                            log.info("Received new connection request");
                            processConnectionRequest(key);
                        } else if (key.isReadable()) {
                            log.info("Received new reading request");
                            processReadingRequest(key);
                        }
                    } catch (Exception e) {
                        key.cancel();
                        try {
                            key.channel().close();
                        } catch (Exception ce) {
                            ce.printStackTrace();
                        }
                    }
                    iterator.remove(); // remove to avoid duplicate
                }
            }
        } catch (IOException e) {
            log.info("IOException, server of port {} terminating. Stack trace:", this.port);
            e.printStackTrace();
        }
    }

    private void processConnectionRequest(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        client.configureBlocking(false);
        log.info("Registering new reading channel: {}", client);

        client.register(selector, SelectionKey.OP_READ, client);     // attach client channel
    }

    private void processReadingRequest(SelectionKey selectionKey) throws IOException {
        SocketChannel client = (SocketChannel) selectionKey.channel();
        StringBuilder sb = new StringBuilder();     // store message received

        commonBuffer.clear();
        int read;
        while ((read = client.read(commonBuffer)) > 0) {
            commonBuffer.flip();
            byte[] bytes = new byte[commonBuffer.limit()];
            commonBuffer.get(bytes);
            sb.append(new String(bytes));
            commonBuffer.clear();
        }
        if (read < 0) {
            log.info("Closing channel {}", client);
            processingService.processChannelClose(client);
            client.close();
        } else {
            String msg = sb.toString().trim();     // read message
            log.info("Message received from {}: {}", client.getRemoteAddress(), msg);

            try {
                processingService.process(client, msg);     // process request HERE
            } catch (NumberFormatException e) {
                processingService.writeToChannel(client, "FAIL-Wrong data format");
                log.error("Wrong data format");
                log.error(e.getMessage(), e);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
