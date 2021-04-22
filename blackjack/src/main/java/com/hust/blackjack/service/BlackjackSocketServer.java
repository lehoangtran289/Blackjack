package com.hust.blackjack.service;

import com.hust.blackjack.exception.RequestException;
import com.hust.blackjack.repository.CreditCardRepository;
import com.hust.blackjack.repository.MatchHistoryRepository;
import com.hust.blackjack.repository.PlayerRepository;
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

    private final CreditCardRepository creditCardRepository;
    private final PlayerRepository playerRepository;
    private final MatchHistoryRepository matchHistoryRepository;
    private final RequestProcessingService processingService;

    public BlackjackSocketServer(CreditCardRepository creditCardRepository,
                                 PlayerRepository playerRepository,
                                 MatchHistoryRepository matchHistoryRepository,
                                 RequestProcessingService processingService,
                                 @Value("${server-port}") int port) throws IOException {
        this.creditCardRepository = creditCardRepository;
        this.playerRepository = playerRepository;
        this.matchHistoryRepository = matchHistoryRepository;
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
                System.out.println("Waiting for events......");
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

        String address = client.getRemoteAddress().toString();
        client.register(selector, SelectionKey.OP_READ, client);     // TODO: attach here
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
            client.close();
        } else {
            String msg = sb.toString().trim();     // TODO: read message here
            log.info("Message received from {}: {}", client.getRemoteAddress(), msg);

            //TODO: process request
            try {
                processingService.process(client, msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
//            processWriting(msg);
        }
    }

    private void processWriting(String message) throws IOException {
        ByteBuffer messageBuffer = ByteBuffer.wrap(message.getBytes());
        for (SelectionKey key : selector.keys()) {
            if (key.isValid() && key.channel() instanceof SocketChannel) {
                SocketChannel client = (SocketChannel) key.channel();
                log.info("Writing to channel {}", key);
                client.write(messageBuffer);
                messageBuffer.rewind();
            }
        }
    }
}
