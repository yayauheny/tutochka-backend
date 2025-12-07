package yayauheny.by.model.import

/**
 * Статус импорта здания.
 * COMPLETED - по зданию есть полноценные данные.
 * PENDING_DETAILS - здание создано "по пути" при импорте туалета, пока известен только минимум.
 */
enum class BuildingImportStatus {
    COMPLETED,
    PENDING_DETAILS
}
