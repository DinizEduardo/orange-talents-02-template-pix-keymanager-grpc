package br.com.zup.pix.actions.registra

import br.com.zup.PixRegistraChaveGrpcServiceGrpc
import br.com.zup.RegistraChavePixRequest
import br.com.zup.RegistraChavePixResponse
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.shared.exception.ErrorHandler
import br.com.zup.pix.utils.toModel
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RegistraChaveGrpcServer(@Inject private val novaChaveService: NovaChaveService)
    : PixRegistraChaveGrpcServiceGrpc.PixRegistraChaveGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RegistraChaveGrpcServer::class.java)

    override fun registra(
        request: RegistraChavePixRequest?,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        logger.info("Registrando chave: $request")

        val chaveCadastrada: ChavePix = novaChaveService.registra(request!!.toModel())

        logger.info("Terminando de registrar chave: $request")

        val response: RegistraChavePixResponse = chaveCadastrada.toResponse()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }

}