package br.com.zup.integration.bcb

import br.com.zup.TipoConta
import br.com.zup.pix.Instituicoes
import br.com.zup.pix.actions.consulta.ChavePixInfo
import br.com.zup.pix.models.Conta
import java.time.LocalDateTime

class ProcuraChaveBcbResponse(
    val keyType: PixKeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {
    fun toModel(): ChavePixInfo {

        return ChavePixInfo(
            tipo = keyType.domainType!!,
            chave = this.key,
            tipoDeConta = when (this.bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                else -> TipoConta.DESCONHECIDA
            },
            conta = Conta(
                instituicao = Instituicoes.nome(bankAccount.participant),
                nomeTitular = owner.name,
                cpfTitular = owner.taxIdNumber,
                agencia = bankAccount.branch,
                numero = bankAccount.accountNumber
            )
        )

    }

}
