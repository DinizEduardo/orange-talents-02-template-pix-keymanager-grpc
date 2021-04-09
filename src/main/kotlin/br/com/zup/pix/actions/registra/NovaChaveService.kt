package br.com.zup.pix.actions.registra

import br.com.zup.integration.itau.ItauClient
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ClienteNotFoundException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Valid

@Validated
@Singleton
class NovaChaveService(
    @Inject val chaveRepository: ChaveRepository,
    @Inject val itauClient: ItauClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)
    fun registra(@Valid chave: ChavePixRequest): ChavePix {
        logger.info("Iniciando o cadastro no banco da chave: $chave")


        val consultaCliente = itauClient.consultaCliente(chave.clienteId.toString(), chave.tipoConta!!.name)
        if(consultaCliente.body() == null) {
            logger.warn("O cliente de id ${chave.clienteId} com a conta do tipo ${chave.tipoConta} nao foi encontrado")
            throw ClienteNotFoundException("Não foi possivel encontrar o cliente com esse id e com esse tipo de conta")
        }

        val chavePix: ChavePix = chave.toModel();

        chaveRepository.save(chavePix)

        logger.info("Finalizando o cadastro no banco da chave: $chavePix")

        return chavePix
    }

}