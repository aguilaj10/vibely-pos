package com.vibely.pos.backend.data.room.mapper

import com.vibely.pos.backend.data.room.entity.CategoryEntity
import com.vibely.pos.backend.dto.request.CreateCategoryRequest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.util.UUID
import kotlin.time.Clock

private const val DEFAULT_DISPLAY_ORDER = 0

/**
 * Maps a [CategoryEntity] to a [JsonObject] for API responses.
 *
 * The JSON keys use the same snake_case column names expected by existing routes.
 *
 * @return [JsonObject] representation of this category
 */
fun CategoryEntity.toJsonObject(): JsonObject =
    buildJsonObject {
        put("id", id)
        put("name", name)
        put("description", description)
        put("parent_id", parentId)
        put("is_active", isActive)
        put("display_order", displayOrder)
        put("created_at", createdAt)
        put("updated_at", updatedAt)
    }

/**
 * Maps a [CreateCategoryRequest] to a new [CategoryEntity] ready for Room insertion.
 *
 * Generates a new random UUID for [CategoryEntity.id] and sets both timestamps to the
 * current instant.
 *
 * @return New [CategoryEntity] with a generated ID and current timestamps
 */
fun CreateCategoryRequest.toEntity(): CategoryEntity {
    val now = Clock.System.now().toString()
    return CategoryEntity(
        id = UUID.randomUUID().toString(),
        name = name,
        description = description,
        parentId = null,
        isActive = isActive,
        displayOrder = DEFAULT_DISPLAY_ORDER,
        createdAt = now,
        updatedAt = now,
    )
}
