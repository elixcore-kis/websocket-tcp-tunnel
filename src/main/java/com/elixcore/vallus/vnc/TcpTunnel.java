package com.elixcore.vallus.vnc;

import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

import java.util.function.Consumer;

public class TcpTunnel {


    private final String                     clientVersion = "RFB 003.008\n"; // RFB 3.8 프로토콜 사용
    private final TcpClient                  tcpClient;
    private       Connection                 connection;
    private       Consumer<WebSocketMessage> webSocketSendQueue;
    private       WebSocketSession           webSocketSession;

    public TcpTunnel(String host, int port, WebSocketSession webSocketSession) {
        this.webSocketSession = webSocketSession;
        this.webSocketSendQueue = new Consumer<WebSocketMessage>() {
            @Override
            public void accept(WebSocketMessage webSocketMessage) {
                webSocketSession.send(Mono.just(webSocketMessage))
                                .subscribe();
            }
        };

        this.tcpClient = TcpClient.create()
                                  .host(host)
                                  .port(port);
        this.tcpClient.doOnConnected(connection -> {
                connection.inbound()
                          .receive()
                          //                          .asString()
                          .asByteArray()
//                          .doOnNext(System.out::println)
                          .doOnNext(tcpReceivedMessage -> this.webSocketSendQueue.accept(this.webSocketSession.binaryMessage(dataBufferFactory -> dataBufferFactory.wrap(
                                  tcpReceivedMessage))))
                          .then()
                          .subscribe();
                this.connection = connection;
            })
                      .connect()
                      .subscribe();

    }

    public void received(WebSocketMessage message) {
        DataBuffer payload = message.getPayload();
        byte[]     bytes   = new byte[payload.readableByteCount()];
        payload.read(bytes);
        this.connection.outbound()
                       .sendByteArray(Mono.just(bytes))
                       .then()
                       .subscribe();
    }

}