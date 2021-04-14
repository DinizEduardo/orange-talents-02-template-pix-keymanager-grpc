package br.com.zup.pix.repositories

import br.com.zup.pix.models.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChaveRepository : JpaRepository<ChavePix, UUID> {

    fun findByIdAndClienteId(id: UUID, clienteId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findAllByClienteId(clienteId: UUID): List<ChavePix>

}