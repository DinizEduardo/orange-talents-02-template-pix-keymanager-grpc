package br.com.zup.pix.actions.registra

import br.com.zup.*
import br.com.zup.integration.bcb.*
import br.com.zup.integration.itau.ClienteResponse
import br.com.zup.integration.itau.Instituicao
import br.com.zup.integration.itau.ItauClient
import br.com.zup.integration.itau.Titular
import br.com.zup.pix.repositories.ChaveRepository
import br.com.zup.pix.utils.toModel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import javax.inject.Inject
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*

@MicronautTest(transactional = false)
class RegistraChaveGrpcServerTest(
    private val chaveRepository: ChaveRepository,
    private val grpcRegistra: PixRegistraChaveGrpcServiceGrpc.PixRegistraChaveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

    @Inject
    lateinit var bcbClient: BcbClient

    @BeforeEach
    fun beforeEach() {
        chaveRepository.deleteAll()
    }

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

    @Test
    fun `deveria cadasrar um cpf como uma nova chave`() {

        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        `when`(bcbClient.registra(dadosRequestDoBcb(PixKeyType.CPF, "48948943863")))
            .thenReturn(HttpResponse.created(dadosResponseBcb()))

        grpcRegistra.registra(
            RegistraChavePixRequest.newBuilder()
                .setChave("48948943863")
                .setTipoChave(TipoChave.CPF)
                .setClienteId(CLIENTE_ID.toString())
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
        }

    }

    @Test
    fun `deveria cadastrar um celular como uma nova chave`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        `when`(bcbClient.registra(dadosRequestDoBcb(PixKeyType.PHONE, "+55992733791")))
            .thenReturn(HttpResponse.created(dadosResponseBcb()))

        grpcRegistra.registra(
            RegistraChavePixRequest.newBuilder()
                .setChave("+55992733791")
                .setTipoChave(TipoChave.CELULAR)
                .setClienteId(CLIENTE_ID.toString())
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
        }
    }

    @Test
    fun `deveria cadastrar um email como uma nova chave`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        `when`(bcbClient.registra(dadosRequestDoBcb(PixKeyType.EMAIL, "eduardo.diniz@zup.com.br")))
            .thenReturn(HttpResponse.created(dadosResponseBcb()))

        grpcRegistra.registra(
            RegistraChavePixRequest.newBuilder()
                .setChave("eduardo.diniz@zup.com.br")
                .setTipoChave(TipoChave.EMAIL)
                .setClienteId(CLIENTE_ID.toString())
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
        }
    }

    @Test
    fun `nao deveria cadastrar uma chave que nao foi cadastrada no bcb por erro 422`() {

        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        `when`(bcbClient.registra(dadosRequestDoBcb(PixKeyType.CPF, "48948943863")))
            .thenThrow(HttpClientResponseException("Erro", HttpResponse.unprocessableEntity<Any>()))

        assertThrows<StatusRuntimeException> {
            grpcRegistra.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("48948943863")
                    .setTipoChave(TipoChave.CPF)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.ALREADY_EXISTS.code, it.status.code)
            assertEquals(
                "Esse valor de chave já está cadastrada no banco do brasil (BCB)",
                it.status.description
            )
            assertEquals(0, chaveRepository.findAll().size)
        }

    }

    @Test
    fun `nao deveria cadastrar uma chave que nao foi cadastrada no bcb por erro 500`() {

        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        `when`(bcbClient.registra(dadosRequestDoBcb(PixKeyType.CPF, "48948943863")))
            .thenThrow(HttpClientResponseException("Erro", HttpResponse.serverError<Any>()))

        assertThrows<StatusRuntimeException> {
            grpcRegistra.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("48948943863")
                    .setTipoChave(TipoChave.CPF)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.FAILED_PRECONDITION.code, it.status.code)
            assertEquals(
                "Erro ao tentar registrar chave no banco do brasil (bcb)",
                it.status.description
            )
            assertEquals(0, chaveRepository.findAll().size)
        }

    }

    @Test
    fun `nao deveria cadastrar uma chave com digito do cpf invalido`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        assertThrows<StatusRuntimeException> {
            grpcRegistra.registra(
                RegistraChavePixRequest.newBuilder()
                    .setChave("48948943862")
                    .setTipoChave(TipoChave.CPF)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals(
                "registra.chave: O valor da chave não está valida para o seu tipo",
                it.status.description
            )
        }
    }

    @Test
    fun `nao deveria cadastrar uma chave com cliente inexistente`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcRegistra.registra(
                RegistraChavePixRequest.newBuilder()
                    .setTipoChave(TipoChave.ALEATORIA)
                    .setClienteId(CLIENTE_ID.toString())
                    .setTipoConta(TipoConta.CONTA_CORRENTE)
                    .build()
            )
        }.let {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals(
                "Não foi possivel encontrar o cliente com esse id e com esse tipo de conta",
                it.status.description
            )
        }
    }

    @Test
    fun `nao deveria cadastrar duas chaves iguais`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))


        val dados = RegistraChavePixRequest.newBuilder()
            .setTipoChave(TipoChave.CPF)
            .setChave("48948943863")
            .setClienteId(CLIENTE_ID.toString())
            .setTipoConta(TipoConta.CONTA_CORRENTE)
            .build()

        chaveRepository.save(dados.toModel().toModel())

        assertThrows<StatusRuntimeException> {
            grpcRegistra.registra(dados)
        }.let {
            assertEquals(Status.ALREADY_EXISTS.code, it.status.code)
            assertEquals(
                "Essa chave já está cadastrada",
                it.status.description
            )
        }


    }

    // necessario mockar a classe inteira antes de mockar o metodo
    @MockBean(ItauClient::class)
    fun itauClient(): ItauClient? {
        return Mockito.mock(ItauClient::class.java)
    }

    private fun dadosRetornadosDoItau(): ClienteResponse {
        return ClienteResponse(
            tipo = TipoConta.CONTA_CORRENTE.name,
            instituicao = Instituicao("UNIBANCO ITAU SA", "60701190"),
            agencia = "0001",
            numero = "236906",
            titular = Titular(CLIENTE_ID.toString(), "Eduardo Diniz", "48948943863")
        )
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

    private fun dadosRequestDoBcb(keyType: PixKeyType, key: String) : CriaChaveBcbRequest {
        return CriaChaveBcbRequest(
            keyType = keyType,
            key = key,
            bankAccount = contaBanco(),
            owner = dono()
        )
    }


    private fun contaBanco(): BankAccount {
        return BankAccount(
            participant = "60701190",
            branch = "0001",
            accountNumber = "236906",
            accountType = AccountType.CACC
        )
    }

    private fun dono(): Owner {
        return Owner(
            type = Owner.OwnerType.NATURAL_PERSON,
            name = "Eduardo Diniz",
            taxIdNumber = "48948943863"
        )

    }


    private fun dadosResponseBcb(): CriaChaveBcbResponse {
        return CriaChaveBcbResponse(
            keyType = PixKeyType.EMAIL,
            key = "48948943863",
            bankAccount = contaBanco(),
            owner = dono(),
            createdAt = LocalDateTime.now()
        )
    }


}