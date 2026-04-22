package com.example.nasacosmosmessenger.data.repository

import com.example.nasacosmosmessenger.data.local.dao.ApodCacheDao
import com.example.nasacosmosmessenger.data.mapper.ApodMapper
import com.example.nasacosmosmessenger.data.remote.api.NasaApodApi
import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.repository.ApodRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApodRepositoryImpl @Inject constructor(
    private val api: NasaApodApi,
    private val cacheDao: ApodCacheDao
) : ApodRepository {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override suspend fun getTodayApod(): Resource<Apod> {
        return getApodByDate(LocalDate.now())
    }

    override suspend fun getApodByDate(date: LocalDate): Resource<Apod> {
        val dateString = date.format(dateFormatter)

        // 1. Check cache first
        val cached = cacheDao.getByDate(dateString)
        if (cached != null) {
            return Resource.Success(ApodMapper.entityToDomain(cached))
        }

        // 2. Cache miss - fetch from network
        return try {
            val dateParam = if (date == LocalDate.now()) null else dateString
            val response = api.getApod(dateParam)
            val entity = ApodMapper.responseToEntity(response)

            // 3. Save to cache
            cacheDao.insert(entity)

            Resource.Success(ApodMapper.responseToDomain(response))
        } catch (e: Exception) {
            // 4. Network failed - check cache again (might have been added by another request)
            val fallback = cacheDao.getByDate(dateString)
            if (fallback != null) {
                Resource.Success(ApodMapper.entityToDomain(fallback))
            } else {
                Resource.Error(
                    message = e.message ?: "Failed to fetch APOD",
                    cause = e
                )
            }
        }
    }

    override suspend fun getCachedApod(date: LocalDate): Apod? {
        val dateString = date.format(dateFormatter)
        return cacheDao.getByDate(dateString)?.let { ApodMapper.entityToDomain(it) }
    }
}
