package br.com.zup.pix.actions.lista

import br.com.zup.ListaChavePixRequest
import br.com.zup.PixListaChavesPorClienteGrpcServiceGrpc
import br.com.zup.TipoConta
import br.com.zup.integration.bcb.BcbClient
import br.com.zup.pix.Instituicoes
import br.com.zup.pix.TipoChave
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.models.Conta
import br.com.zup.pix.repositories.ChaveRepository
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.RuntimeException
import java.util.*
import javax.inject.Inject

@MicronautTest(transactional = false)
class ListaChaveGrpcEndpointTest(
    private val chaveRepository: ChaveRepository,
    private val grpcLista: PixListaChavesPorClienteGrpcServiceGrpc.PixListaChavesPorClienteGrpcServiceBlockingStub

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
    fun `deveria devolver uma lista com a chave cadastrada do clienteId`() {
        grpcLista.lista(
            ListaChavePixRequest.newBuilder()
                .setClienteId(CLIENTE_ID.toString())
                .build()
        ).let {
            assertEquals(it.chavesList.size, 1)
            assertEquals(it.clienteId, CLIENTE_ID.toString())
            assertEquals(it.chavesList[0].chave, chavePix.chave)
        }
    }

    @Test
    fun `deveria devolver uma lista vazia passando um clienteId sem chaves`() {

        val clienteSemChave = UUID.randomUUID().toString()
        grpcLista.lista(
            ListaChavePixRequest.newBuilder()
                .setClienteId(clienteSemChave)
                .build()
        ).let {
            assertEquals(it.chavesList.size, 0)
            assertEquals(it.clienteId.toString(), clienteSemChave)
        }
    }

    @Test
    fun `nao deveria devolver listas sem passar um clienteId`() {
        assertThrows<StatusRuntimeException> {
            grpcLista.lista(
                ListaChavePixRequest.newBuilder()
                    .build()
            )
        }.let {
            assertEquals(it.status.code, Status.INVALID_ARGUMENT.code)
            assertEquals(it.status.description, "É necessario informar o clienteId que deseja buscar")
        }
    }
}