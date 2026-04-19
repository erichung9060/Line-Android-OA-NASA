package com.example.nasacosmosmessenger.domain.usecase

import com.example.nasacosmosmessenger.domain.model.Apod
import com.example.nasacosmosmessenger.domain.model.Resource
import com.example.nasacosmosmessenger.domain.repository.ApodRepository
import java.time.LocalDate
import javax.inject.Inject

class GetApodByDateUseCase @Inject constructor(
    private val repository: ApodRepository
) {
    suspend operator fun invoke(date: LocalDate): Resource<Apod> {
        return repository.getApodByDate(date)
    }
}
