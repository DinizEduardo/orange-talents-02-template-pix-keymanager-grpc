package br.com.zup.integration.bcb

import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:8082/")
interface BcbClient {

    @Post(
        "/api/v1/pix/keys",
        processes = [MediaType.APPLICATION_XML]
    )
    fun registra(@Body request: CriaChaveBcbRequest): HttpResponse<CriaChaveBcbResponse>

    @Delete(
        "/api/v1/pix/keys/{key}",
        processes = [MediaType.APPLICATION_XML]
    )
    fun deleta(@PathVariable key: String, @Body request: DeletaChaveBcbRequest): HttpResponse<DeletaChaveBcbResponse>

    @Get("/api/v1/pix/keys/{key}",
        consumes = [MediaType.APPLICATION_XML])
    fun findByKey(@PathVariable key: String): HttpResponse<ProcuraChaveBcbResponse>

}