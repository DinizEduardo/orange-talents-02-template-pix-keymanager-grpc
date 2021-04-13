package br.com.zup.pix.actions.registra

import br.com.zup.integration.bcb.*
import br.com.zup.integration.itau.ItauClient
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.shared.exception.ClienteNotFoundException
import br.com.zup.pix.shared.exception.DuplicateException
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalStateException
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChaveService(
    @Inject val chaveRepository: ChaveRepository,
    @Inject val itauClient: ItauClient,
    @Inject val bcbClient: BcbClient
) {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Transactional
    fun registra(@Valid chave: ChavePixRequest): ChavePix {
        logger.info("Iniciando o cadastro no banco da chave: $chave")


        val consultaCliente = itauClient.consultaCliente(chave.clienteId.toString(), chave.tipoConta!!.name)
        if(consultaCliente.body() == null) {
            logger.warn("O cliente de id ${chave.clienteId} com a conta do tipo ${chave.tipoConta} nao foi encontrado")
            throw ClienteNotFoundException("Não foi possivel encontrar o cliente com esse id e com esse tipo de conta")
        }

        val chavePix: ChavePix = chave.toModel();

        val clienteItau = consultaCliente.body()!!

        logger.info("Cadastrando a chave no bcb")
        try {
            val bcbResponse = bcbClient.registra(CriaChaveBcbRequest(
                keyType = PixKeyType.by(chavePix.tipoChave),
                key = chavePix.chave,
                bankAccount = BankAccount(
                    participant = clienteItau.instituicao.ispb,
                    branch = clienteItau.agencia,
                    accountNumber = clienteItau.numero,
                    accountType = AccountType.by(chavePix.tipoConta)
                ),
                owner = Owner(
                    type = Owner.OwnerType.NATURAL_PERSON,
                    name = clienteItau.titular.nome,
                    taxIdNumber = clienteItau.titular.cpf
                )
            ))

//            if(bcbResponse.status != HttpStatus.CREATED) {
//                logger.warn("Ocorreu um erro para cadastrar a chave no bcb")
//                throw IllegalStateException("Erro ao tentar registrar chave no Banco Do Brasil (BCB)")
//            }


            logger.info("Chave cadastrada com sucesso no bcb")

            chavePix.atualiza(bcbResponse.body()!!.key)
        } catch (e: HttpClientResponseException) {
            logger.warn("Deu algum problema na hora de cadastrar a chave no bcb")
            if(e.status == HttpStatus.UNPROCESSABLE_ENTITY) {
                logger.warn("A chave ja existe no banco de dados da bcb")
                throw DuplicateException("Esse valor de chave já está cadastrada no banco do brasil (BCB)")
            }
            logger.warn("Algum erro desconhecido no bcb")
            throw IllegalStateException("Erro ao tentar registrar chave no banco do brasil (bcb)")
        }

        chaveRepository.save(chavePix)

        logger.info("Finalizando o cadastro no banco da chave: $chavePix")

        return chavePix
    }

}