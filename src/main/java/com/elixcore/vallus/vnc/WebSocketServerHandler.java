package com.elixcore.vallus.vnc;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class WebSocketServerHandler implements WebSocketHandler {

    private TcpTunnel tcpTunnel;

    public WebSocketServerHandler() {
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {

        // 이 예제에서는 "localhost"와 port 9090의 TCP 서버에 연결하는 TcpService를 사용
        this.tcpTunnel = new TcpTunnel("100.0.0.231", 5901, webSocketSession);
//        this.tcpService = new TcpService("100.0.0.15", 5900, webSocketSession);

        return webSocketSession.receive()
//                               .doOnNext(t -> log.info("Received message: {}", t.getPayloadAsText()))
                               .doOnNext(message -> this.tcpTunnel.received(message))
                               .then();

        // TCP 서버 연결
        //        var connection = tcpService.connect();

        // WebSocket 클라이언트 -> TCP 서버 로직
        //        Flux<String> clientToTcp = webSocketSession.receive()
        //                                                   .map(WebSocketMessage::getPayloadAsText)
        //                                                   .publishOn(Schedulers.boundedElastic())
        //                                                   .doOnNext(text -> connection.outbound()
        //                                                                               .sendString(Mono.just(text))
        //                                                                               .then()
        //                                                                               .subscribe());

        // TCP 서버 -> WebSocket 클라이언트 로직
        //        Flux<String> tcpToClient = connection.inbound()
        //                                             .receive()
        //                                             .asString(StandardCharsets.UTF_8)
        //                                             .publishOn(Schedulers.boundedElastic())
        //                                             .doOnNext(data -> webSocketSession.send(
        //                                                                                       Mono.just(webSocketSession.textMessage(data))
        //                                                                               )
        //                                                                               .subscribe());

        //        // WebSocket 클라이언트와 TCP 서버 메시지를 계속 처리
        //        return Mono.zip(clientToTcp.collectList(), tcpToClient.collectList())
        //                   .then();
    }
}