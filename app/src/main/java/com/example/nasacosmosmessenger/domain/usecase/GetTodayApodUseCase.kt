package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.repository.ApodRepository
import javax.inject.Inject

class GetTodayApodUseCase @Inject constructor(
    private val repository: ApodRepository
) {
    suspend operator fun invoke(): Resource<Apod> {
        return repository.getTodayApod()
    }
}
