package com.swang;

import com.swang.grpc.CalculatorServiceImpl;
import io.grpc.Server;
import io.grpc.ServerBuilder;

import java.io.IOException;

public class CalculatorServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50051)
                .addService(new CalculatorServiceImpl())
                .build();

        System.out.println("Server started on port 50051");
        server.start();
        server.awaitTermination();
    }
}
