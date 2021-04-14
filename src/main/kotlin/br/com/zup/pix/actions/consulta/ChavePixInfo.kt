package br.com.zup.pix.actions.consulta

import br.com.zup.TipoConta
import br.com.zup.pix.TipoChave
import br.com.zup.pix.models.ChavePix
import br.com.zup.pix.models.Conta
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val pixId: UUID? = null,
    val clienteId: UUID? = null,
    val tipo: TipoChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: Conta,
    val registradaEm: LocalDateTime = LocalDateTime.now()
) {
    companion object {

        fun of(chave: ChavePix): ChavePixInfo {

            return ChavePixInfo(
                pixId = chave.id,
                clienteId = chave.clienteId,
                tipo = chave.tipoChave,
                chave = chave.chave,
                conta = chave.conta,
                tipoDeConta = chave.tipoConta,
                registradaEm = chave.criadaEm
            )

        }
    }

}
