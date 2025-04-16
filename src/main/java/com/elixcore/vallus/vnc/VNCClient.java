package com.elixcore.vallus.vnc;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VNCClient {

    public static void connectToVNCServer(String serverIp, int serverPort) {
        try (Socket socket = new Socket(serverIp, serverPort)) {
            System.out.println("Connecting to VNC server at " + serverIp + ":" + serverPort + "...");

            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // 1. 서버 버전 읽기 (12 바이트)
            byte[] serverVersionBytes = new byte[12];
            inputStream.read(serverVersionBytes);
            String serverVersion = new String(serverVersionBytes).trim();
            System.out.println("Server version: " + serverVersion);

            // 2. 클라이언트 버전 전송
            String clientVersion = "RFB 003.008\n"; // RFB 3.8 프로토콜 사용
                outputStream.write(clientVersion.getBytes());
            outputStream.flush();
            System.out.println("Sent client version: " + clientVersion.trim());

            // 3. 보안 타입 수신 (첫 번째 바이트 = 보안 타입 개수)
            int numSecurityTypes = inputStream.read();
            System.out.println("Number of security types supported: " + numSecurityTypes);

            if (numSecurityTypes > 0) {
                // 지원되는 보안 타입 읽기
                byte[] securityTypes = new byte[numSecurityTypes];
                inputStream.read(securityTypes);
                System.out.print("Supported security types: ");
                for (byte type : securityTypes) {
                    System.out.print(type + " ");
                }
                System.out.println();

                // 4. 보안 타입 선택 (1: None)
                outputStream.write(1); // None 선택
                outputStream.flush();

                // 5. 서버의 보안 결과 확인 (4 바이트)
                byte[] securityResult = new byte[4];
                inputStream.read(securityResult);
                int securityStatus = byteArrayToInt(securityResult);
                
                if (securityStatus == 0) {
                    System.out.println("Security handshake successful.");
                } else {
                    System.out.println("Security handshake failed with status code: " + securityStatus);
                    return;
                }
            } else {
                System.out.println("No supported security types available.");
                return;
            }

            // 6. 초기 통신 완료 -> 추가 동작 가능
            System.out.println("Handshake complete. Ready to interact with the server.");

        } catch (Exception e) {
            System.out.println("An error occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 바이트 배열을 정수로 변환 (빅엔디안)
    private static int byteArrayToInt(byte[] bytes) {
        return ((bytes[0] & 0xFF) << 24) |
               ((bytes[1] & 0xFF) << 16) |
               ((bytes[2] & 0xFF) << 8) |
               (bytes[3] & 0xFF);
    }

    public static void main(String[] args) {
        String serverIp = "100.0.0.231"; // 로컬 호스트 (VNC 테스트용)
        int serverPort = 5901;        // 기본 VNC 서버 포트

        connectToVNCServer(serverIp, serverPort);
    }
}