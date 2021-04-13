package br.com.zup.integration.bcb

import java.time.LocalDateTime

data class DeletaChaveBcbResponse (
    val key: String,
    val participant: String,
    val deletedAt: LocalDateTime,
)