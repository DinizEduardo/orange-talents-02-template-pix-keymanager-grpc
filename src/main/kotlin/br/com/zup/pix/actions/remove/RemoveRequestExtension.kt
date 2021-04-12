package br.com.zup.pix.actions.remove


import br.com.zup.RemoveChavePixRequest
import br.com.zup.RemoveChavePixResponse
import java.util.*

fun RemoveChavePixRequest.toModel(): RemoveChaveRequest = RemoveChaveRequest(
    clienteId = this.clienteId,
    pixId =  this.pixId
)

fun RemoveChavePixRequest.toResponse(): RemoveChavePixResponse = RemoveChavePixResponse
    .newBuilder()
    .setPixId(this.pixId)
    .setClienteId(this.clienteId)
    .build()