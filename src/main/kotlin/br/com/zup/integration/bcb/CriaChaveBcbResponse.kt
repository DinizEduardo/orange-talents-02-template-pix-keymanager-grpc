package br.com.zup.integration.bcb

import br.com.zup.pix.TipoChave
import java.time.LocalDateTime

data class CriaChaveBcbResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

}

enum class PixKeyType(val domainType: TipoChave?) {

    CPF(TipoChave.CPF),
    CNPJ(null),
    PHONE(TipoChave.CELULAR),
    EMAIL(TipoChave.EMAIL),
    RANDOM(TipoChave.ALEATORIA);

    companion object {

        private val mapping = PixKeyType.values().associateBy(PixKeyType::domainType)

        fun by(domainType: TipoChave): PixKeyType {
            return  mapping[domainType] ?: throw IllegalArgumentException("PixKeyType invalid or not found for $domainType")
        }
    }
}
