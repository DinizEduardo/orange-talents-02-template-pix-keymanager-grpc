package br.com.zup.pix.actions.registra

import br.com.zup.TipoConta
import br.com.zup.pix.TipoChave
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.models.Conta
import br.com.zup.pix.shared.validation.CampoUnico
import br.com.zup.pix.shared.validation.ValidaChavePix
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidaChavePix
@Introspected
data class ChavePixRequest(
    @field:NotBlank
    val clienteId: String?,

    @field:NotNull
    val tipoConta: TipoConta?,

    @field:Size(max = 77)
    @CampoUnico(domainClass = ChavePix::class, field = "chave", message = "Essa chave já está cadastrada")
    val chave: String?,

    @field:NotNull
    val tipoChave: TipoChave?
) {
    fun toModel(conta: Conta): ChavePix {
        return ChavePix(
            clienteId = UUID.fromString(clienteId),
            tipoConta = tipoConta!!,
            tipoChave = tipoChave!!,
            chave = if (this.tipoChave == TipoChave.ALEATORIA) UUID.randomUUID().toString() else this.chave!!,
            conta = conta
        )
    }

}
