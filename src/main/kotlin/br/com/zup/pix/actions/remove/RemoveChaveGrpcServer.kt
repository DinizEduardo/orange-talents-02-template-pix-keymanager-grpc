package br.com.zup.pix.actions.remove

import br.com.zup.PixRegistraChaveGrpcServiceGrpc
import br.com.zup.PixRemoveChaveGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.RemoveChavePixResponse
import br.com.zup.pix.actions.registra.RegistraChaveGrpcServer
import br.com.zup.pix.shared.exception.ErrorHandler
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class RemoveChaveGrpcServer(@Inject private val removeChaveService: RemoveChaveService) :
    PixRemoveChaveGrpcServiceGrpc.PixRemoveChaveGrpcServiceImplBase() {

    private val logger = LoggerFactory.getLogger(RegistraChaveGrpcServer::class.java)
    override fun remove(request: RemoveChavePixRequest?, responseObserver: StreamObserver<RemoveChavePixResponse>?) {

        logger.info("Server -> Iniciou o processo de exclusão da chave ${request!!.pixId}")

        val chaveRequest: RemoveChaveRequest = request.toModel()

        removeChaveService.remove(chaveRequest)

        logger.info("Server -> Finalizou o processo de exclusão da chave ${request.pixId}")

        responseObserver?.onNext(request.toResponse())
        responseObserver?.onCompleted()
    }
}