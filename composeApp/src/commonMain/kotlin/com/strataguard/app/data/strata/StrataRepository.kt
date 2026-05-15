package com.strataguard.app.data.strata

interface StrataRepository {
    suspend fun searchPlans(query: String, state: String): List<StrataPlan>
    suspend fun getPlan(spNumber: String): StrataPlan?
    suspend fun seedIfEmpty()
    suspend fun createPlan(plan: StrataPlan)
    suspend fun searchNearby(lat: Double, lng: Double, radiusKm: Double): List<Pair<StrataPlan, Double>>
}
