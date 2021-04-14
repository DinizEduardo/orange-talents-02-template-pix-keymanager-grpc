package br.com.zup.integration.itau

import br.com.zup.pix.models.Conta

data class ClienteResponse(
    val tipo: String,
    val instituicao: Instituicao,
    val agencia: String,
    val numero: String,
    val titular: Titular
) {
    fun toModel(): Conta {
        return Conta(
            instituicao = this.instituicao.nome,
            numero = this.numero,
            agencia = this.agencia,
            cpfTitular = this.titular.cpf,
            nomeTitular = this.titular.nome
        )
    }

}

data class Titular(
    val id: String,
    val nome: String,
    val cpf: String
)

data class Instituicao(
    val nome: String,
    val ispb: String
)
