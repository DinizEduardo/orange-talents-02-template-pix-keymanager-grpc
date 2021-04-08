package br.com.zup.pix.shared.exception.handler

import br.com.zup.pix.shared.exception.ClienteNotFoundException
import br.com.zup.pix.shared.exception.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ClienteNotFoundExceptionHandler : ExceptionHandler<ClienteNotFoundException> {

    override fun handle(e: ClienteNotFoundException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is ClienteNotFoundException
    }

}