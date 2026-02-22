package yayauheny.by.model.enums

/**
 * Статус импорта здания.
 * COMPLETED - по зданию есть полноценные данные.
 * PENDING - здание создано "по пути" при импорте туалета, пока известен только минимум.
 */
enum class BuildingImportStatus {
    COMPLETED,
    PENDING
}
