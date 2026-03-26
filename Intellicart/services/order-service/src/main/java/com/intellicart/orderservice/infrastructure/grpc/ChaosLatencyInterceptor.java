package com.intellicart.orderservice.infrastructure.grpc;

import io.grpc.*;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.interceptor.GrpcGlobalClientInterceptor;

@Slf4j
@GrpcGlobalClientInterceptor // Se aplica a todos los stubs gRPC del servicio
public class ChaosLatencyInterceptor implements ClientInterceptor {

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void sendMessage(ReqT message) {
                // Simulación de latencia: 2 segundos de delay artificial
                if (Math.random() > 0.7) { // 30% de probabilidad de fallo de latencia
                    log.warn("Chaos Engineering: Inyectando latencia de 2s en llamada gRPC a {}", method.getFullMethodName());
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                super.sendMessage(message);
            }
        };
    }
}