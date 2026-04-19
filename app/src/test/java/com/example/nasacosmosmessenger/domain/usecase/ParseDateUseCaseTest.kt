package com.example.nasacosmosmessenger.domain.usecase

import com.google.common.truth.Truth.assertThat
import org.junit.jupiter.api.Test
import java.time.LocalDate

class ParseDateUseCaseTest {

    private val useCase = ParseDateUseCase()

    @Test
    fun `parse valid yyyy-MM-dd returns date`() {
        val result = useCase("Show me 2020-01-15")
        assertThat(result).isEqualTo(LocalDate.of(2020, 1, 15))
    }

    @Test
    fun `parse valid yyyy_MM_dd (slash) returns date`() {
        val result = useCase("Show me 2020/01/15")
        assertThat(result).isEqualTo(LocalDate.of(2020, 1, 15))
    }

    @Test
    fun `message without supported date returns null`() {
        val result = useCase("Hello Nova!")
        assertThat(result).isNull()
    }

    @Test
    fun `date before APOD start is treated as no parseable date`() {
        // APOD started 1995-06-16
        val result = useCase("Show me 1990/01/01")
        assertThat(result).isNull()
    }

    @Test
    fun `future date is treated as no parseable date`() {
        val futureDate = LocalDate.now().plusDays(1)
        val result = useCase("Show me $futureDate")
        assertThat(result).isNull()
    }
}
