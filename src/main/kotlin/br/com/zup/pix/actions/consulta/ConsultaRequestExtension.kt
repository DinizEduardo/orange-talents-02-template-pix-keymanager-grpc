package br.com.zup.pix.actions.consulta

import br.com.zup.ConsultaChavePixRequest

fun ConsultaChavePixRequest.toModel() : Filtro {

    val filtro = when(filtroCase!!) {
        ConsultaChavePixRequest.FiltroCase.PIXID -> pixId.let {
            Filtro.PorPixId(clienteId = it.clienteId, pixId = it.pixId)
        }
        ConsultaChavePixRequest.FiltroCase.CHAVE -> Filtro.PelaChave(chave)
        ConsultaChavePixRequest.FiltroCase.FILTRO_NOT_SET -> Filtro.Invalido()
    }

    return filtro

}