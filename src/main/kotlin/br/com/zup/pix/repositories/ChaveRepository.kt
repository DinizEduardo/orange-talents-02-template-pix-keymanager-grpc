package br.com.zup.pix.repositories

import br.com.zup.pix.models.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChaveRepository : JpaRepository<ChavePix, UUID> {



}