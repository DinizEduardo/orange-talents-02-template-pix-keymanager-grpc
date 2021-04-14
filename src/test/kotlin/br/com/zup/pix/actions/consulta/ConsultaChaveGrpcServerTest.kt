package br.com.zup.pix.actions.consulta

import br.com.zup.*
import br.com.zup.integration.bcb.*
import br.com.zup.integration.itau.ClienteResponse
import br.com.zup.integration.itau.Instituicao
import br.com.zup.integration.itau.Titular
import br.com.zup.pix.Instituicoes
import br.com.zup.pix.TipoChave
import br.com.zup.pix.actions.registra.RegistraChaveGrpcServerTest
import br.com.zup.pix.actions.remove.RemoveChaveGrpcServerTest
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.models.Conta
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.utils.toModel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
class ConsultaChaveGrpcServerTest(
    private val chaveRepository: ChaveRepository,
    private val grpcConsulta: PixConsultaChaveGrpcServiceGrpc.PixConsultaChaveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var bcbClient: BcbClient

    private lateinit var chavePix: ChavePix

    @BeforeEach
    fun beforeEach() {
        chaveRepository.deleteAll()

        chavePix = ChavePix(
            clienteId = CLIENTE_ID,
            tipoChave = TipoChave.ALEATORIA,
            tipoConta = TipoConta.CONTA_CORRENTE,
            chave = UUID.randomUUID().toString(),
            conta = Conta(
                instituicao = Instituicoes.nome("60701190"),
                agencia = "0001",
                numero =  "236906",
                cpfTitular = "48948943863",
                nomeTitular = "Eduardo Diniz",
            )
        )

        chaveRepository.save(chavePix)
    }


    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @Test
    fun `deveria encontrar uma chave com pixId e clienteId`() {
        grpcConsulta.pesquisa(
            ConsultaChavePixRequest.newBuilder()
                .setPixId(
                    ConsultaChavePixRequest.FiltroPixId.newBuilder()
                        .setClienteId(CLIENTE_ID.toString())
                        .setPixId(chavePix.id.toString())
                        .build()
                )
                .build()
        ).let {
            assertEquals(it.clienteId, chavePix.clienteId.toString())
            assertEquals(it.chave.chave, chavePix.chave)
            assertEquals(it.pixId, chavePix.id.toString())
        }
    }

    @Test
    fun `deveria achar uma chave usando sua chave como parametro de busca`() {
        grpcConsulta.pesquisa(
            ConsultaChavePixRequest.newBuilder()
                .setChave(chavePix.chave)
                .build()
        ).let {
            assertEquals(it.clienteId, chavePix.clienteId.toString())
            assertEquals(it.chave.chave, chavePix.chave)
            assertEquals(it.pixId, chavePix.id.toString())
        }
    }

    @Test
    fun `nao deveria encontrar uma chave com clienteId incorreto`() {
        assertThrows<StatusRuntimeException> {
            grpcConsulta.pesquisa(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPixId.newBuilder()
                            .setClienteId(chavePix.id.toString())
                            .setPixId(chavePix.id.toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave pix nao foi encontrada em nosso sistema")
        }
    }

    @Test
    fun `nao deveria encontrar uma chave com pixId incorreto`() {
        org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcConsulta.pesquisa(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPixId.newBuilder()
                            .setClienteId(CLIENTE_ID.toString())
                            .setPixId(CLIENTE_ID.toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave pix nao foi encontrada em nosso sistema")
        }
    }

    @Test
    fun `nao deveria encontrar uma chave com ambos parametros incorretos`() {
        org.junit.jupiter.api.assertThrows<StatusRuntimeException> {
            grpcConsulta.pesquisa(
                ConsultaChavePixRequest.newBuilder()
                    .setPixId(
                        ConsultaChavePixRequest.FiltroPixId.newBuilder()
                            .setClienteId(UUID.randomUUID().toString())
                            .setPixId(UUID.randomUUID().toString())
                            .build()
                    )
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave pix nao foi encontrada em nosso sistema")
        }
    }

    @Test
    fun `deveria encontrar a chave no sistema bcb passando sua chave`() {
        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.ok(dadosRetornoBcb(chave)))

        grpcConsulta.pesquisa(
            ConsultaChavePixRequest.newBuilder()
                .setChave(chave)
                .build()
        ).let {
            assertEquals(it.chave.chave, chave)
            assertEquals(it.chave.tipoChave.name, TipoChave.ALEATORIA.name)
            assertEquals(it.chave.conta.agencia, "0001")
            assertEquals(it.chave.conta.numero, "321321")
        }
    }

    @Test
    fun `nao deveria encontrar a chave no sistema bcb passando uma chave invalida`() {
        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcConsulta.pesquisa(
                ConsultaChavePixRequest.newBuilder()
                    .setChave(chave)
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.NOT_FOUND.code)
            assertEquals(it.status.description, "Chave pix não existe")
        }
    }

    @Test
    fun `nao deveria encontrar nada sem passar nenhum parametro`() {
        val chave = UUID.randomUUID().toString()

        `when`(bcbClient.findByKey(chave)).thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcConsulta.pesquisa(
                ConsultaChavePixRequest.newBuilder()
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.INVALID_ARGUMENT.code)
            assertEquals(it.status.description, "Chave pix invalida ou não informada")
        }
    }

    private fun dadosRetornoBcb(chave: String): ProcuraChaveBcbResponse? {
        return ProcuraChaveBcbResponse(
            key = chave,
            keyType = PixKeyType.RANDOM,
            bankAccount = BankAccount(
                participant = "60701190",
                branch = "0001",
                accountNumber = "321321",
                accountType = AccountType.CACC
            ),
            owner = Owner(
                type = Owner.OwnerType.NATURAL_PERSON,
                name = "Eduardo Diniz",
                taxIdNumber = "48948943863"
            ),
            createdAt = LocalDateTime.now()
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

}