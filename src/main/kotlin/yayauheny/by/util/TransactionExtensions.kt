package yayauheny.by.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.jooq.impl.DSL

/**
 * Выполняет блок кода в транзакции с поддержкой suspend функций.
 * Автоматически коммитит транзакцию при успешном выполнении или откатывает при ошибке.
 *
 * @param block блок кода для выполнения в транзакции
 * @return результат выполнения блока
 * @throws Throwable любое исключение, выброшенное в блоке, приведет к rollback транзакции
 */
suspend fun <T> DSLContext.transactionSuspend(block: suspend (DSLContext) -> T): T {
    return withContext(Dispatchers.IO) {
        var result: T? = null
        transaction { configuration ->
            val txCtx = DSL.using(configuration)
            // Используем runBlocking для выполнения suspend функции внутри синхронного блока транзакции
            result =
                kotlinx.coroutines.runBlocking {
                    block(txCtx)
                }
        }
        result!!
    }
}
