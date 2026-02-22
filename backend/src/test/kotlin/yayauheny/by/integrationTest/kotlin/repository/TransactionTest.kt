package integration.repository

import integration.base.BaseIntegrationTest
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import yayauheny.by.tables.references.RESTROOMS
import yayauheny.by.util.pointExpr
import yayauheny.by.util.transactionSuspend

@DisplayName("Transaction atomicity tests")
class TransactionTest : BaseIntegrationTest() {
    @Test
    @DisplayName("GIVEN transaction with error WHEN rollback THEN no data persisted")
    fun given_transaction_with_error_when_rollback_then_no_data_persisted() =
        runTest {
            val testId = java.util.UUID.randomUUID()

            // Попытка выполнить транзакцию с ошибкой
            try {
                dslContext.transactionSuspend { txCtx ->
                    // Вставляем запись
                    txCtx
                        .insertInto(RESTROOMS)
                        .set(RESTROOMS.ID, testId)
                        .set(RESTROOMS.NAME, "Test Restroom")
                        .set(RESTROOMS.ADDRESS, "Test Address")
                        .set(RESTROOMS.FEE_TYPE, "FREE")
                        .set(RESTROOMS.ACCESSIBILITY_TYPE, "WHEELCHAIR")
                        .set(RESTROOMS.STATUS, "ACTIVE")
                        .set(RESTROOMS.DATA_SOURCE, "MANUAL")
                        .set(RESTROOMS.COORDINATES, pointExpr(37.6176, 55.7558, RESTROOMS.COORDINATES))
                        .execute()

                    // Вызываем ошибку - транзакция должна откатиться
                    throw RuntimeException("Simulated error")
                }
            } catch (e: RuntimeException) {
                // Ожидаем ошибку
                assertEquals("Simulated error", e.message)
            }

            val savedRecord =
                dslContext
                    .select(RESTROOMS.ID, RESTROOMS.NAME)
                    .from(RESTROOMS)
                    .where(RESTROOMS.ID.eq(testId))
                    .fetchOne()

            assertNull(savedRecord, "Запись не должна быть сохранена после rollback транзакции")
        }

    @Test
    @DisplayName("GIVEN successful transaction WHEN commit THEN data persisted")
    fun given_successful_transaction_when_commit_then_data_persisted() =
        runTest {
            val testId = java.util.UUID.randomUUID()

            dslContext.transactionSuspend { txCtx ->
                txCtx
                    .insertInto(RESTROOMS)
                    .set(RESTROOMS.ID, testId)
                    .set(RESTROOMS.NAME, "Test Restroom")
                    .set(RESTROOMS.ADDRESS, "Test Address")
                    .set(RESTROOMS.FEE_TYPE, "FREE")
                    .set(RESTROOMS.ACCESSIBILITY_TYPE, "WHEELCHAIR")
                    .set(RESTROOMS.STATUS, "ACTIVE")
                    .set(RESTROOMS.DATA_SOURCE, "MANUAL")
                    .set(RESTROOMS.COORDINATES, pointExpr(37.6176, 55.7558, RESTROOMS.COORDINATES))
                    .execute()
            }

            val savedRecord =
                dslContext
                    .select(RESTROOMS.ID, RESTROOMS.NAME)
                    .from(RESTROOMS)
                    .where(RESTROOMS.ID.eq(testId))
                    .fetchOne()

            assertEquals(testId, savedRecord?.get(RESTROOMS.ID), "Запись должна быть сохранена после успешной транзакции")
            assertEquals("Test Restroom", savedRecord?.get(RESTROOMS.NAME), "Имя должно совпадать")
        }
}
