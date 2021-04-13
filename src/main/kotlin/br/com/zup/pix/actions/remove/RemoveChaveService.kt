package br.com.zup.pix.actions.remove

import br.com.zup.RemoveChavePixRequest
import br.com.zup.integration.bcb.BcbClient
import br.com.zup.integration.bcb.DeletaChaveBcbRequest
import br.com.zup.integration.itau.ItauClient
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ClienteNotFoundException
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalStateException
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class RemoveChaveService(
    @Inject val chaveRepository: ChaveRepository,
    @Inject val bcbClient: BcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    fun remove(@Valid request: RemoveChaveRequest) {
        logger.info("Service -> Começou o processo de exclusão da chava ${request.pixId}")


        val chavePix: Optional<ChavePix> =
            chaveRepository.findByIdAndClienteId(UUID.fromString(request.pixId), UUID.fromString(request.clienteId))
        if (!chavePix.isPresent) {
            logger.warn("A chave de id ${request.pixId} com cliente ${request.clienteId} não foi encontrada")
            throw ClienteNotFoundException("Não foi possivel encontrar a chave solicitada")
        }

        logger.info("Começa o processo de exclusão do bcb")
        try {
            val bcbExcluiu = bcbClient.deleta(
                key = chavePix.get().chave,
                request = DeletaChaveBcbRequest(key = chavePix.get().chave)
            )

            logger.info("Terminou a requisição de exclusão do bcb")

            if (bcbExcluiu.status != HttpStatus.OK) {
                throw IllegalStateException("Erro ao remover chave pix no banco central (BCB)")
            }
        } catch (e: Exception) {
            throw IllegalStateException("Erro ao remover chave pix no banco central (BCB)")
        }

        logger.info("Excluiu com sucesso no bcb")

        chaveRepository.delete(chavePix.get())

        logger.info("Service -> Terminou o processo de exclusão da chava ${request.pixId}")
    }

}
