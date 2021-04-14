package br.com.zup.pix.actions.remove

import br.com.zup.PixRemoveChaveGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.TipoConta
import br.com.zup.integration.bcb.*
import br.com.zup.integration.itau.Instituicao
import br.com.zup.integration.itau.Titular
import br.com.zup.pix.Instituicoes
import br.com.zup.pix.TipoChave
import br.com.zup.pix.actions.registra.RegistraChaveGrpcServerTest
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.models.Conta
import br.com.zup.pix.repositories.ChaveRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
class RemoveChaveGrpcServerTest (
    private val chaveRepository: ChaveRepository,
    private val grpcRemove: PixRemoveChaveGrpcServiceGrpc.PixRemoveChaveGrpcServiceBlockingStub
) {
    private lateinit var chavePix: ChavePix

    @Inject
    lateinit var bcbClient: BcbClient

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
    }

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
    }

    @Test
    fun `deveria excluir uma chave existente`() {

        chaveRepository.save(chavePix)

        `when`(bcbClient.deleta(chavePix.chave, DeletaChaveBcbRequest(key = chavePix.chave)))
            .thenReturn(HttpResponse.ok(
                DeletaChaveBcbResponse(
                    key = chavePix.chave,
                    participant = "60701190",
                    deletedAt = LocalDateTime.now()
                )
            ))

        grpcRemove.remove(
            RemoveChavePixRequest.newBuilder()
                .setClienteId(chavePix.clienteId.toString())
                .setPixId(chavePix.id.toString())
                .build()
        ).let {
            assertNotNull(it.pixId)
            assertNotNull(it.clienteId)
            assertEquals(it.pixId, chavePix.id.toString())
            assertEquals(it.clienteId, chavePix.clienteId.toString())
            assertEquals(0, chaveRepository.findAll().size)
        }

    }

    @Test
    fun `nao deveria excluir uma chave que nao foi excluida no bcb por retornar 403`() {
        chaveRepository.save(chavePix)

        `when`(bcbClient.deleta(chavePix.chave, DeletaChaveBcbRequest(key = chavePix.chave)))
            .thenReturn(HttpResponse.unauthorized())

        assertThrows<StatusRuntimeException> {
            grpcRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(chavePix.clienteId.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }.let {
            assertEquals(Status.FAILED_PRECONDITION.code, it.status.code)
            assertEquals("Erro ao remover chave pix no banco central (BCB)", it.status.description)
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

    @Test
    fun `nao deveria excluir uma chave que nao foi excluida no bcb por retornar 404`() {
        chaveRepository.save(chavePix)

        `when`(bcbClient.deleta(chavePix.chave, DeletaChaveBcbRequest(key = chavePix.chave)))
            .thenReturn(HttpResponse.notFound())

        assertThrows<StatusRuntimeException> {
            grpcRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setClienteId(chavePix.clienteId.toString())
                    .setPixId(chavePix.id.toString())
                    .build()
            )
        }.let {
            assertEquals(Status.FAILED_PRECONDITION.code, it.status.code)
            assertEquals("Erro ao remover chave pix no banco central (BCB)", it.status.description)
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

    @Test
    fun `nao deveria excluir uma chave com clienteId incorreto`() {
        chaveRepository.save(chavePix)

        assertThrows<StatusRuntimeException> {
            grpcRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(chavePix.id.toString())
                    .setClienteId(UUID.randomUUID().toString())
                    .build()
            )
        }.let {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals("Não foi possivel encontrar a chave solicitada", it.status.description)
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

    @Test
    fun `nao deveria excluir uma chave com pixId incorreto`() {
        chaveRepository.save(chavePix)

        assertThrows<StatusRuntimeException> {
            grpcRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .setPixId(UUID.randomUUID().toString())
                    .setClienteId(chavePix.clienteId.toString())
                    .build()
            )
        }.let {
            assertEquals(Status.NOT_FOUND.code, it.status.code)
            assertEquals("Não foi possivel encontrar a chave solicitada", it.status.description)
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

    @Test
    fun `nao deveria excluir uma chave sem passar nenhuma informacao`() {
        chaveRepository.save(chavePix)

        assertThrows<StatusRuntimeException> {
            grpcRemove.remove(
                RemoveChavePixRequest.newBuilder()
                    .build()
            )
        }.let {
            assertEquals(Status.INVALID_ARGUMENT.code, it.status.code)
            assertEquals(it.status.description!!.length, 98)
            assertTrue(it.status.description!!.endsWith("O valor deve ser um UUID"))
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

    @MockBean(BcbClient::class)
    fun bcbClient(): BcbClient? {
        return Mockito.mock(BcbClient::class.java)
    }

}