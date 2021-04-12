package br.com.zup.pix.actions.remove

import br.com.zup.PixRemoveChaveGrpcServiceGrpc
import br.com.zup.RemoveChavePixRequest
import br.com.zup.TipoConta
import br.com.zup.pix.TipoChave
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.repositories.ChaveRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.*

@MicronautTest(transactional = false)
class RemoveChaveGrpcServerTest (
    private val chaveRepository: ChaveRepository,
    private val grpcRemove: PixRemoveChaveGrpcServiceGrpc.PixRemoveChaveGrpcServiceBlockingStub
) {
    private lateinit var chavePix: ChavePix

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
            chave = UUID.randomUUID().toString()
        )
    }

    @Test
    fun `deveria excluir uma chave existente`() {

        chaveRepository.save(chavePix)

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
            assertEquals("remove.request.pixId: O valor deve ser um UUID, remove.request.clienteId: O valor deve ser um UUID", it.status.description)
            assertEquals(1, chaveRepository.findAll().size)
        }
    }

}