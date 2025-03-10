package com.swang.grpc;

import com.example.grpc.CalculatorProto;
import com.example.grpc.CalculatorServiceGrpc;
import io.grpc.stub.StreamObserver;

public class CalculatorServiceImpl extends CalculatorServiceGrpc.CalculatorServiceImplBase {
    @Override
    public void add(CalculatorProto.AddRequest request, StreamObserver<CalculatorProto.AddResponse> responseObserver) {
        int sum = request.getA() + request.getB();
        CalculatorProto.AddResponse response = CalculatorProto.AddResponse.newBuilder().setResult(sum).build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void primeDecomposition(CalculatorProto.PrimeRequest request, StreamObserver<CalculatorProto.PrimeResponse> responseObserver) {
        int number = request.getNumber();
        int divisor = 2;
        while (number > 1) {
            if (number % divisor == 0) {
                CalculatorProto.PrimeResponse response = CalculatorProto.PrimeResponse.newBuilder().setFactor(divisor).build();
                responseObserver.onNext(response);
                number /= divisor;
            } else {
                divisor++;
            }
        }
        responseObserver.onCompleted();
    }

    @Override
    public StreamObserver<CalculatorProto.AverageRequest> computeAverage(StreamObserver<CalculatorProto.AverageResponse> responseObserver) {
        return new StreamObserver<CalculatorProto.AverageRequest>() {
            int sum = 0;
            int count = 0;

            @Override
            public void onNext(CalculatorProto.AverageRequest request) {
                sum += request.getNumber();
                count++;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in ComputeAverage: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                double average = (count == 0) ? 0 : (double) sum / count;
                CalculatorProto.AverageResponse response = CalculatorProto.AverageResponse.newBuilder().setAverage(average).build();
                responseObserver.onNext(response);
                responseObserver.onCompleted();
            }
        };
    }

    @Override
    public StreamObserver<CalculatorProto.MaximumRequest> findMaximum(StreamObserver<CalculatorProto.MaximumResponse> responseObserver) {
        return new StreamObserver<CalculatorProto.MaximumRequest>() {
            int currentMaximum = Integer.MIN_VALUE;

            @Override
            public void onNext(CalculatorProto.MaximumRequest request) {
                int number = request.getNumber();
                if (number > currentMaximum) {
                    currentMaximum = number;
                    CalculatorProto.MaximumResponse response = CalculatorProto.MaximumResponse.newBuilder().setMaximum(currentMaximum).build();
                    responseObserver.onNext(response);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error in FindMaximum: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        };
    }
}
