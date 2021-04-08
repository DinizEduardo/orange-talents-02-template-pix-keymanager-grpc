package br.com.zup.pix

import br.com.zup.*
import io.grpc.ManagedChannel
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel

@Factory
class Clients {

    @Bean
    fun grpcRegistra(
        @GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel
    ) : PixRegistraChaveGrpcServiceGrpc.PixRegistraChaveGrpcServiceBlockingStub{
        return PixRegistraChaveGrpcServiceGrpc.newBlockingStub(channel);
    }

}