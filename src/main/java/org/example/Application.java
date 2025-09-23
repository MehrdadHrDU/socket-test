package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        var context = SpringApplication.run(Application.class, args);

        SocketServer server = context.getBean(SocketServer.class);
        new Thread(() -> {
            try {
                server.start();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
