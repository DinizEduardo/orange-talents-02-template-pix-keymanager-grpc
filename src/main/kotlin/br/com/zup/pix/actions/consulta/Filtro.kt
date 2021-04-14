package br.com.zup.pix.actions.consulta

import br.com.zup.integration.bcb.BcbClient
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ChavePixNaoEncontradaException
import io.micronaut.http.HttpStatus
import org.slf4j.LoggerFactory
import java.lang.IllegalArgumentException
import java.util.*

sealed class Filtro {


    abstract fun filtra(repository: ChaveRepository, bcbClient: BcbClient) : ChavePixInfo

    data class PorPixId(
        val clienteId: String,
        val pixId: String
    ): Filtro() {
        private val logger = LoggerFactory.getLogger(this::class.java)

        private val pixIdUUID = UUID.fromString(pixId)
        private val clienteIdUUID = UUID.fromString(clienteId)

        override fun filtra(repository: ChaveRepository, bcbClient: BcbClient) : ChavePixInfo {

            logger.info("Pesquisando por PixId")

            return repository.findById(pixIdUUID)
                .filter { it.pertence(clienteIdUUID) }
                .map(ChavePixInfo::of)
                .orElseThrow{ChavePixNaoEncontradaException("Chave pix nao foi encontrada em nosso sistema")}

        }

    }


    data class PelaChave(val chave: String) : Filtro() {
        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChaveRepository, bcbClient: BcbClient): ChavePixInfo {
            logger.info("Pesquisando pela chave no nosso banco")
            return repository.findByChave(chave)
                .map(ChavePixInfo::of)
                .orElseGet {
                    logger.info("Pesquisando pela chave no BCB")
                    val bcbConsulta = bcbClient.findByKey(chave)
                    when(bcbConsulta.status) {
                        HttpStatus.OK -> bcbConsulta.body()!!.toModel()
                        else -> throw ChavePixNaoEncontradaException("Chave pix não existe")
                    }
                }

        }

    }

    class Invalido : Filtro() {
        private val logger = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChaveRepository, bcbClient: BcbClient): ChavePixInfo {
            logger.info("Pesquisa invalida")
            throw IllegalArgumentException("Chave pix invalida ou não informada")
        }
    }


}
