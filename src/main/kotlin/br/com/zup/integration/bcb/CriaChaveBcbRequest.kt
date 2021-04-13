package br.com.zup.integration.bcb

import br.com.zup.TipoChave
import br.com.zup.TipoConta

data class CriaChaveBcbRequest(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner
)

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType
)

enum class AccountType {
    NOT_FOUND, CACC, SVGS;

    companion object {
        fun by(domainType: TipoConta): AccountType {
            return when (domainType) {
                TipoConta.CONTA_CORRENTE -> CACC
                TipoConta.CONTA_POUPANCA -> SVGS
                else -> NOT_FOUND
            }
        }
    }
}

data class Owner(
    val type: OwnerType,
    val name: String,
    val taxIdNumber: String
) {
    enum class OwnerType {
        NATURAL_PERSON,
        LEGAL_PERSON
    }
}
