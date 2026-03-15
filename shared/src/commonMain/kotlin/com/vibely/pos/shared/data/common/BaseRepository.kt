package com.vibely.pos.shared.data.common

import com.vibely.pos.shared.domain.result.Result
import com.vibely.pos.shared.domain.result.map

/**
 * Base class for repository implementations providing common DTO mapping utilities.
 */
abstract class BaseRepository {

    /**
     * Maps a single DTO to domain entity within a Result.
     *
     * @param result Result containing DTO
     * @param mapper Mapping function from DTO to domain entity
     * @return Result containing mapped domain entity
     */
    protected fun <D, E> mapSingle(result: Result<D>, mapper: (D) -> E): Result<E> = result.map(mapper)

    /**
     * Maps a list of DTOs to domain entities within a Result.
     *
     * @param result Result containing list of DTOs
     * @param mapper Mapping function from DTO to domain entity
     * @return Result containing list of mapped domain entities
     */
    protected fun <D, E> mapList(result: Result<List<D>>, mapper: (D) -> E): Result<List<E>> = result.map { dtos -> dtos.map(mapper) }
}
