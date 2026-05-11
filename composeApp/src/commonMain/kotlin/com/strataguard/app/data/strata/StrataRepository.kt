package com.strataguard.app.data.strata

interface StrataRepository {
    suspend fun searchPlans(query: String, state: String): List<StrataPlan>
    suspend fun getPlan(spNumber: String): StrataPlan?
    suspend fun seedIfEmpty()
}
