package br.com.zup.pix.shared.exception

import java.lang.RuntimeException

class ClienteNotFoundException(mensagem: String) : RuntimeException(mensagem)