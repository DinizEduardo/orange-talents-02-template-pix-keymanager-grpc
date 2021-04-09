package br.com.zup.pix.actions.registra

import br.com.zup.*
import br.com.zup.integration.itau.ClienteResponse
import br.com.zup.integration.itau.Instituicao
import br.com.zup.integration.itau.ItauClient
import br.com.zup.integration.itau.Titular
import br.com.zup.pix.repositories.ChaveRepository
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
import javax.inject.Inject
import org.mockito.Mockito.`when`
import java.util.*

@MicronautTest(transactional = false)
class RegistraChaveGrpcServerTest(
    private val chaveRepository: ChaveRepository,
    private val grpcRegistra: PixRegistraChaveGrpcServiceGrpc.PixRegistraChaveGrpcServiceBlockingStub
) {

    @Inject
    lateinit var itauClient: ItauClient

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
    fun `deveria cadastrar um valor aleatorio como uma nova chave`() {
        `when`(itauClient.consultaCliente(id = CLIENTE_ID.toString(), tipo = "CONTA_CORRENTE"))
            .thenReturn(HttpResponse.ok(dadosRetornadosDoItau()))

        grpcRegistra.registra(
            RegistraChavePixRequest.newBuilder()
                .setTipoChave(TipoChave.ALEATORIA)
                .setClienteId(CLIENTE_ID.toString())
                .setTipoConta(TipoConta.CONTA_CORRENTE)
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
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

        grpcRegistra.registra(dados)


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

}