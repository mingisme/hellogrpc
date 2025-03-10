package com.swang;

import com.example.grpc.CalculatorProto;
import com.example.grpc.CalculatorServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;

import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CalculatorClient {

    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        CalculatorServiceGrpc.CalculatorServiceBlockingStub blockingStub =
                CalculatorServiceGrpc.newBlockingStub(channel);
        CalculatorServiceGrpc.CalculatorServiceStub asyncStub =
                CalculatorServiceGrpc.newStub(channel);

        // Unary call: Add
        System.out.println("Blocked Add: ");
        CalculatorProto.AddRequest addRequest = CalculatorProto.AddRequest.newBuilder().setA(10).setB(20).build();
        CalculatorProto.AddResponse addResponse = blockingStub.add(addRequest);
        System.out.println("Add Result: " + addResponse.getResult());
        CountDownLatch latch5 = new CountDownLatch(1);
        System.out.println("Asynchronous Add: ");
        asyncStub.add(addRequest, new StreamObserver<CalculatorProto.AddResponse>() {
            @Override
            public void onNext(CalculatorProto.AddResponse addResponse) {
                System.out.println("Add Result: " + addResponse.getResult());
            }

            @Override
            public void onError(Throwable throwable) {
                System.err.println("Error Add Result: " + throwable);
                latch5.countDown();
            }

            @Override
            public void onCompleted() {
                latch5.countDown();
            }
        });
        latch5.await(5, TimeUnit.SECONDS);


        // Server Streaming call: PrimeDecomposition
        System.out.println("Blocked Prime factors:");
        CalculatorProto.PrimeRequest primeRequest = CalculatorProto.PrimeRequest.newBuilder().setNumber(84).build();
        blockingStub.primeDecomposition(primeRequest)
                .forEachRemaining(primeResponse -> System.out.println(primeResponse.getFactor()));

        System.out.println("Asynchronous Prime factors:");
        CountDownLatch latch0 = new CountDownLatch(1);
        StreamObserver<com.example.grpc.CalculatorProto.PrimeResponse> responseObserver = new StreamObserver<com.example.grpc.CalculatorProto.PrimeResponse>(){
            @Override
            public void onNext(CalculatorProto.PrimeResponse primeResponse) {
                System.out.println(primeResponse.getFactor());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error prime factor: " + t.getMessage());
                latch0.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Finished prime factor");
                latch0.countDown();
            }
        };
        asyncStub.primeDecomposition(primeRequest,responseObserver);
        latch0.await(3, TimeUnit.SECONDS);

        // Client Streaming call: ComputeAverage
        CountDownLatch latch1 = new CountDownLatch(1);
        StreamObserver<CalculatorProto.AverageResponse> averageResponseObserver = new StreamObserver<CalculatorProto.AverageResponse>() {
            @Override
            public void onNext(CalculatorProto.AverageResponse response) {
                System.out.println("Average: " + response.getAverage());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error computing average: " + t.getMessage());
                latch1.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Finished computing average");
                latch1.countDown();
            }
        };

        StreamObserver<CalculatorProto.AverageRequest> averageRequestObserver = asyncStub.computeAverage(averageResponseObserver);
        Arrays.asList(1, 2, 3, 4, 5).forEach(num -> {
            CalculatorProto.AverageRequest req = CalculatorProto.AverageRequest.newBuilder().setNumber(num).build();
            averageRequestObserver.onNext(req);
        });
        averageRequestObserver.onCompleted();
        latch1.await(3, TimeUnit.SECONDS);

        // Bidirectional Streaming call: FindMaximum
        CountDownLatch latch2 = new CountDownLatch(1);
        StreamObserver<CalculatorProto.MaximumResponse> maximumResponseObserver = new StreamObserver<CalculatorProto.MaximumResponse>() {
            @Override
            public void onNext(CalculatorProto.MaximumResponse response) {
                System.out.println("New Maximum: " + response.getMaximum());
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in FindMaximum: " + t.getMessage());
                latch2.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Finished streaming maximum values");
                latch2.countDown();
            }
        };

        StreamObserver<CalculatorProto.MaximumRequest> maximumRequestObserver = asyncStub.findMaximum(maximumResponseObserver);
        // Send a series of numbers to the server
        Arrays.asList(3, 10, 6, 2, 20, 15).forEach(num -> {
            CalculatorProto.MaximumRequest req = CalculatorProto.MaximumRequest.newBuilder().setNumber(num).build();
            maximumRequestObserver.onNext(req);
            try {
                Thread.sleep(500); // simulate delay
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        maximumRequestObserver.onCompleted();
        latch2.await(3, TimeUnit.SECONDS);

        channel.shutdown();
    }

}
