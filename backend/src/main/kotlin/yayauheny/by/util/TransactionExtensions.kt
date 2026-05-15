package yayauheny.by.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.exception.DataAccessException
import org.jooq.impl.DSL
import org.postgresql.util.PSQLException
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import yayauheny.by.common.errors.EntityNotFoundException

/**
 * Выполняет блок кода в транзакции с поддержкой suspend функций.
 * Автоматически коммитит транзакцию при успешном выполнении или откатывает при ошибке.
 * Пробрасывает оригинальные исключения (PSQLException, EntityNotFoundException) напрямую,
 * извлекая их из DataAccessException, если они были обернуты.
 *
 * @param block блок кода для выполнения в транзакции
 * @return результат выполнения блока
 * @throws PSQLException если произошла ошибка базы данных PostgreSQL
 * @throws EntityNotFoundException если сущность не найдена
 * @throws Throwable любое другое исключение, выброшенное в блоке, приведет к rollback транзакции
 */
suspend fun <T> DSLContext.transactionSuspend(block: suspend (DSLContext) -> T): T {
    return withContext(Dispatchers.IO) {
        try {
            transactionResult { configuration ->
                val txCtx = DSL.using(configuration)
                var outcome: Result<T>? = null

                block.startCoroutine(
                    txCtx,
                    object : Continuation<T> {
                        override val context = EmptyCoroutineContext

                        override fun resumeWith(result: Result<T>) {
                            outcome = result
                        }
                    }
                )

                val resolvedOutcome = outcome ?: error("transactionSuspend block must not suspend")
                resolvedOutcome.getOrThrow()
            }
        } catch (e: DataAccessException) {
            val cause = e.cause
            when {
                cause is PSQLException -> throw cause
                cause is EntityNotFoundException -> throw cause
                else -> throw e
            }
        }
    }
}
