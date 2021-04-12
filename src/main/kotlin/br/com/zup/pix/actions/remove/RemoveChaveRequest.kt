package br.com.zup.pix.actions.remove

import br.com.zup.pix.shared.validation.ValidaUUID
import io.micronaut.core.annotation.Introspected

@Introspected
class RemoveChaveRequest(
    @field:ValidaUUID(message = "Não é um formato valido para UUID")
    val pixId: String,

    @field:ValidaUUID(message = "Não é um formato valido para UUID")
    val clienteId: String
)