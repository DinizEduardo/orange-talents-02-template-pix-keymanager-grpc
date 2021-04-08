package br.com.zup.integration.itau

import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1")
interface ItauClient {

    // /clientes/5260263c-a3c1-4727-ae32-3bdb2538841b/contas?tipo=CONTA_CORRENTE
    // http://localhost:9091/api/v1/clientes/5260263c-a3c1-4727-ae32-3bdb2538841b/contas?tipo=CONTA_CORRENTE
    @Get("/clientes/{id}/contas?tipo={tipo}")
    fun consultaCliente(@PathVariable id: String, @QueryValue tipo: String ) : HttpResponse<ClienteResponse>

}