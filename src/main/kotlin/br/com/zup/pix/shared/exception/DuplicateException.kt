package br.com.zup.pix.shared.exception

import java.lang.RuntimeException

class DuplicateException(mensagem: String) : RuntimeException(mensagem)