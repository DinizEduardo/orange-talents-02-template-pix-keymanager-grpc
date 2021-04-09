package br.com.zup.pix.actions.remove

import br.com.zup.RemoveChavePixRequest
import br.com.zup.integration.itau.ItauClient
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ClienteNotFoundException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoveChaveService(
    @Inject val chaveRepository: ChaveRepository
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    fun remove(request: RemoveChaveRequest) {
        logger.info("Service -> Começou o processo de exclusão da chava ${request.pixId}")


        val chavePix: Optional<ChavePix> = chaveRepository.findByIdAndClienteId(request.pixId, request.clienteId)
        if(!chavePix.isPresent) {
            logger.warn("A chave de id ${request.pixId} com cliente ${request.clienteId} não foi encontrada")
            throw ClienteNotFoundException("Não foi possivel encontrar a chave solicitada")
        }

        chaveRepository.delete(chavePix.get())

        logger.info("Service -> Terminou o processo de exclusão da chava ${request.pixId}")
    }

}
