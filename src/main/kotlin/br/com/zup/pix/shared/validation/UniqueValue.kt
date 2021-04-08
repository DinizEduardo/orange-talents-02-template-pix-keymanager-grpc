package br.com.zup.pix.shared.validation

import br.com.zup.pix.shared.exception.DuplicateException
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.persistence.EntityManager
import javax.persistence.NoResultException
import javax.transaction.Transactional
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.annotation.AnnotationRetention.RUNTIME
import kotlin.annotation.AnnotationTarget.CONSTRUCTOR
import kotlin.annotation.AnnotationTarget.FIELD
import kotlin.reflect.KClass

@MustBeDocumented
@Target(FIELD, CONSTRUCTOR)
@Retention(RUNTIME)
@Constraint(validatedBy = [CampoUnicoValidator::class])
annotation class CampoUnico(
    val field: String,
    val domainClass: KClass<*>,
    val message: String = "O campo já está cadastrado no banco de dados",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
open class CampoUnicoValidator(private val em: EntityManager) : ConstraintValidator<CampoUnico, String> {

    @Transactional
    override fun isValid(
        value: String?,
        annotationMetadata: AnnotationValue<CampoUnico>,
        context: ConstraintValidatorContext
    ): Boolean {
        val campo = annotationMetadata.stringValue("field").get()
        val klass = annotationMetadata.classValue("domainClass").get()
        val message = annotationMetadata.stringValue("message").get()

        try {
            em.createQuery("SELECT k FROM ${klass.simpleName} k WHERE k.$campo=:value")
                .setParameter("value", value).singleResult
            // se continuar a execução significa que encontrou um resultado, então temos que devolver o erro
            throw DuplicateException(message)
        } catch (e: NoResultException){
            // caso não tenha resultado retorna true pra continuar as coisas
            return true
        }

    }

}