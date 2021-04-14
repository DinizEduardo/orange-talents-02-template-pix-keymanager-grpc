package br.com.zup.pix.models

import javax.persistence.Embeddable

@Embeddable
class Conta(
    val instituicao: String,

    val nomeTitular: String,

    val cpfTitular: String,

    val agencia: String,

    val numero: String

) {
    companion object{
        public val ITAU_ISPB: String = "60701190"
    }


}