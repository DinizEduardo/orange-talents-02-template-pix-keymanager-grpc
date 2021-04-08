package br.com.zup.pix.shared.exception.handler

import br.com.zup.pix.shared.exception.DuplicateException
import br.com.zup.pix.shared.exception.ExceptionHandler
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class AlreadyExistsExceptionHandler : ExceptionHandler<DuplicateException> {

    override fun handle(e: DuplicateException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.ALREADY_EXISTS
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is DuplicateException
    }

}