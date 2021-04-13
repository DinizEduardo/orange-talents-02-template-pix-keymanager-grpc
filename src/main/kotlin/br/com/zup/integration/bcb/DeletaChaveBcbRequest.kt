package br.com.zup.integration.bcb

val ITAU_UNIBANCO_ISPB: String = "60701190"

data class DeletaChaveBcbRequest(
    val key: String,
    val participant: String = ITAU_UNIBANCO_ISPB
)
