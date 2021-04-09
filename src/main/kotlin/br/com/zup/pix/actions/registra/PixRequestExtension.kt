package br.com.zup.pix.utils

import br.com.zup.RegistraChavePixRequest
import br.com.zup.pix.TipoChave
import br.com.zup.pix.actions.registra.ChavePixRequest

fun RegistraChavePixRequest.toModel(): ChavePixRequest = ChavePixRequest(
    clienteId = this.clienteId,
    chave =  this.chave,
    tipoConta = this.tipoConta,
    tipoChave = TipoChave.valueOf(this.tipoChave.name)
)