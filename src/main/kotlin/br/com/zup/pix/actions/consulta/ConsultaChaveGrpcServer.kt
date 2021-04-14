package br.com.zup.pix.actions.consulta

import br.com.zup.*
import br.com.zup.integration.bcb.BcbClient
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ErrorHandler
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import javax.xml.validation.Validator

@ErrorHandler
@Singleton
class ConsultaChaveGrpcServer(
    @Inject private val chaveRepository: ChaveRepository,
    @Inject private val bcbClient: BcbClient
) :
    PixConsultaChaveGrpcServiceGrpc.PixConsultaChaveGrpcServiceImplBase() {

    override fun pesquisa(
        request: ConsultaChavePixRequest?,
        responseObserver: StreamObserver<ConsultaChavePixResponse>?
    ) {

        val filtro = request!!.toModel()

        val chavePixInfo: ChavePixInfo = filtro.filtra(chaveRepository, bcbClient)

        val resposta = ConsultaChavePixResponse.newBuilder()
            .setClienteId(chavePixInfo.clienteId?.toString() ?: "")
            .setPixId(chavePixInfo.pixId?.toString() ?: "")
            .setChave(
                ConsultaChavePixResponse.ChavePix.newBuilder()
                    .setTipoChave(TipoChave.valueOf(chavePixInfo.tipo.name))
                    .setChave(chavePixInfo.chave)
                    .setCriadaEm(
                        chavePixInfo.registradaEm.let {
                            val criadaEm = it.atZone(ZoneId.of("UTC")).toInstant()
                            Timestamp.newBuilder()
                                .setSeconds(criadaEm.epochSecond)
                                .setNanos(criadaEm.nano)
                                .build()
                        }
                    )
                    .setConta(
                        ConsultaChavePixResponse.ChavePix.InfoConta.newBuilder()
                        .setTipoConta(TipoConta.valueOf(chavePixInfo.tipoDeConta.name))
                        .setInstituicao(chavePixInfo.conta.instituicao)
                        .setNomeTitular(chavePixInfo.conta.nomeTitular)
                        .setCpfTitular(chavePixInfo.conta.cpfTitular)
                        .setAgencia(chavePixInfo.conta.agencia)
                        .setNumero(chavePixInfo.conta.numero)
                        .build()
                    )
                    .build()
            ).build()

        responseObserver?.onNext(resposta)
        responseObserver?.onCompleted()
    }
}