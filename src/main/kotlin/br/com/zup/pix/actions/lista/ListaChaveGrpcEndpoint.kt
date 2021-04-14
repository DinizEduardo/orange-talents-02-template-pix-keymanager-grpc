package br.com.zup.pix.actions.lista

import br.com.zup.*
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.lang.IllegalArgumentException
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandler
@Singleton
class ListaChaveGrpcEndpoint(@Inject val chaveRepository: ChaveRepository) :
    PixListaChavesPorClienteGrpcServiceGrpc.PixListaChavesPorClienteGrpcServiceImplBase() {

    override fun lista(request: ListaChavePixRequest, responseObserver: StreamObserver<ListaChavePixResponse>?) {

        if(request.clienteId.isNullOrBlank()) {
            throw IllegalArgumentException("É necessario informar o clienteId que deseja buscar")
        }

        val clienteId = UUID.fromString(request.clienteId)

        val chaves = chaveRepository.findAllByClienteId(clienteId).map {
            ListaChavePixResponse.Chave.newBuilder()
                .setChave(it.chave)
                .setPixId(it.id.toString())
                .setTipoChave(TipoChave.valueOf(it.tipoChave.name))
                .setTipoConta(TipoConta.valueOf(it.tipoConta.name))
                .setCriadaEm(it.criadaEm.let {
                    val criadaEm = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setSeconds(criadaEm.epochSecond)
                        .setNanos(criadaEm.nano)
                        .build()
                })
                .build()
        }

        val resposta: ListaChavePixResponse = ListaChavePixResponse.newBuilder()
            .addAllChaves(chaves)
            .setClienteId(clienteId.toString())
            .build()

        responseObserver?.onNext(resposta)
        responseObserver?.onCompleted()


    }

}