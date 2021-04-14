package br.com.zup.pix.models

import br.com.zup.RegistraChavePixResponse
import br.com.zup.pix.TipoChave
import br.com.zup.TipoConta
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
class ChavePix(
    @field:NotNull
    val clienteId: UUID,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoChave: TipoChave,

    @field:NotBlank
    @Column(unique = true, nullable = false)
    var chave: String,

    @field:NotNull
    @Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:Valid
    @Embedded
    var conta: Conta
) {

    @Id
    @GeneratedValue
    val id: UUID? = null

    @Column(nullable = false)
    val criadaEm: LocalDateTime = LocalDateTime.now()

    override fun toString(): String {
        return "ChavePix(clienteId=$clienteId, tipoChave=$tipoChave, chave='$chave', tipoConta=$tipoConta, id=$id, criadaEm=$criadaEm)"
    }

    fun toResponse(): RegistraChavePixResponse {
        return RegistraChavePixResponse.newBuilder()
            .setClienteId(this.clienteId.toString())
            .setPixId(this.id.toString())
            .build()
    }

    fun atualiza(key: String) {
        if(this.tipoChave == TipoChave.ALEATORIA)
            this.chave = key
    }

    fun pertence(clienteId: UUID) = this.clienteId.equals(clienteId)


}
