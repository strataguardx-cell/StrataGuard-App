package com.strataguard.app.data.strata

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.firestore

class FirestoreStrataRepository : StrataRepository {

    private val db = Firebase.firestore
    private val col get() = db.collection("strata_plans")

    override suspend fun searchPlans(query: String, state: String): List<StrataPlan> {
        val q = query.trim()
        if (q.length < 2) return emptyList()

        return if (q.uppercase().startsWith("SP") || q.uppercase().startsWith("OC")) {
            val doc = col.document(q.uppercase()).get()
            if (doc.exists) listOf(doc.data<StrataPlan>()) else emptyList()
        } else {
            // Search by suburb (case-insensitive via stored suburbLower field)
            val bySuburb = col
                .where { "suburbLower" equalTo q.lowercase() }
                .where { "state" equalTo state }
                .get()
                .documents
                .mapNotNull { runCatching { it.data<StrataPlan>() }.getOrNull() }

            if (bySuburb.isNotEmpty()) {
                bySuburb
            } else {
                // Fallback: prefix scan on suburb (ordered)
                col
                    .where { "state" equalTo state }
                    .orderBy("suburb")
                    .startAt(q.replaceFirstChar { it.uppercase() })
                    .endAt(q.replaceFirstChar { it.uppercase() } + "")
                    .limit(20)
                    .get()
                    .documents
                    .mapNotNull { runCatching { it.data<StrataPlan>() }.getOrNull() }
            }
        }
    }

    override suspend fun getPlan(spNumber: String): StrataPlan? {
        val doc = col.document(spNumber.uppercase()).get()
        return if (doc.exists) runCatching { doc.data<StrataPlan>() }.getOrNull() else null
    }

    override suspend fun seedIfEmpty() {
        val existing = col.limit(1).get()
        if (existing.documents.isNotEmpty()) return
        seedData.forEach { plan ->
            col.document(plan.spNumber).set(plan)
        }
    }

    override suspend fun createPlan(plan: StrataPlan) {
        col.document(plan.spNumber.uppercase()).set(plan)
    }

    override suspend fun searchNearby(lat: Double, lng: Double, radiusKm: Double): List<Pair<StrataPlan, Double>> {
        val all = col.get().documents
            .mapNotNull { runCatching { it.data<StrataPlan>() }.getOrNull() }
            .filter { it.latitude != 0.0 && it.longitude != 0.0 }
        return all
            .map { plan -> plan to haversineKm(lat, lng, plan.latitude, plan.longitude) }
            .filter { (_, dist) -> dist <= radiusKm }
            .sortedBy { (_, dist) -> dist }
    }
}

private fun Double.toRad() = this * (kotlin.math.PI / 180.0)

private fun haversineKm(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val r = 6371.0
    val dLat = (lat2 - lat1).toRad()
    val dLng = (lng2 - lng1).toRad()
    val a = kotlin.math.sin(dLat / 2) * kotlin.math.sin(dLat / 2) +
        kotlin.math.cos(lat1.toRad()) * kotlin.math.cos(lat2.toRad()) *
        kotlin.math.sin(dLng / 2) * kotlin.math.sin(dLng / 2)
    return r * 2 * kotlin.math.atan2(kotlin.math.sqrt(a), kotlin.math.sqrt(1 - a))
}

// ---------------------------------------------------------------------------
// Seed data — mirrors NSW Strata Hub public fields + Building Commission orders
// Source: NSW Strata Hub (nsw.gov.au/strata-search), NSW Building Commission
// ---------------------------------------------------------------------------
private val seedData = listOf(

    StrataPlan(
        spNumber = "SP83985",
        address = "1 Bourke Road, Mascot",
        suburb = "Mascot",
        suburbLower = "mascot",
        postcode = "2020",
        state = "NSW",
        registrationDate = "12 Sep 1998",
        lotCount = 132,
        managingAgent = "City Strata Management Pty Ltd",
        managingAgentLicence = "1234567",
        lastAGM = "15 Jun 2024",
        buildingClass = "Class 2",
        workOrders = listOf(
            WorkOrder("rectification", "Structural defects — façade water ingress and balcony cracking requiring rectification", "12 Jan 2024", "active"),
            WorkOrder("stop_work", "Unauthorised works to load-bearing walls in basement car park", "3 Mar 2024", "active"),
        ),
        sinkingFundStatus = "critical",
        latitude = -33.9283,
        longitude = 151.1994,
    ),

    StrataPlan(
        spNumber = "SP52948",
        address = "200 Harris Street, Pyrmont",
        suburb = "Pyrmont",
        suburbLower = "pyrmont",
        postcode = "2009",
        state = "NSW",
        registrationDate = "5 Mar 2003",
        lotCount = 48,
        managingAgent = "Harris Strata Services",
        managingAgentLicence = "2345678",
        lastAGM = "22 Sep 2024",
        buildingClass = "Class 2",
        workOrders = listOf(
            WorkOrder("rectification", "Fire door non-compliance — 14 doors require replacement or rectification", "8 Aug 2024", "active"),
        ),
        sinkingFundStatus = "adequate",
        latitude = -33.8763,
        longitude = 151.1938,
    ),

    StrataPlan(
        spNumber = "SP75403",
        address = "55 King Street, Newtown",
        suburb = "Newtown",
        suburbLower = "newtown",
        postcode = "2042",
        state = "NSW",
        registrationDate = "20 Jul 2008",
        lotCount = 18,
        managingAgent = "Inner West Strata",
        managingAgentLicence = "3456789",
        lastAGM = "10 Mar 2025",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -33.8987,
        longitude = 151.1792,
    ),

    StrataPlan(
        spNumber = "SP91234",
        address = "15 Albert Avenue, Chatswood",
        suburb = "Chatswood",
        suburbLower = "chatswood",
        postcode = "2067",
        state = "NSW",
        registrationDate = "14 Feb 2011",
        lotCount = 60,
        managingAgent = "North Shore Strata Pty Ltd",
        managingAgentLicence = "4567890",
        lastAGM = "18 Nov 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "low",
        latitude = -33.7969,
        longitude = 151.1828,
    ),

    StrataPlan(
        spNumber = "SP44567",
        address = "88 Crown Street, Surry Hills",
        suburb = "Surry Hills",
        suburbLower = "surry hills",
        postcode = "2010",
        state = "NSW",
        registrationDate = "9 Nov 2001",
        lotCount = 24,
        managingAgent = "Crown Strata Management",
        managingAgentLicence = "5678901",
        lastAGM = "5 Aug 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -33.8848,
        longitude = 151.2115,
    ),

    StrataPlan(
        spNumber = "SP33210",
        address = "3 Church Street, Parramatta",
        suburb = "Parramatta",
        suburbLower = "parramatta",
        postcode = "2150",
        state = "NSW",
        registrationDate = "28 Apr 2006",
        lotCount = 96,
        managingAgent = "Western Sydney Strata Group",
        managingAgentLicence = "6789012",
        lastAGM = "2 Dec 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -33.8153,
        longitude = 151.0034,
    ),

    StrataPlan(
        spNumber = "SP88765",
        address = "10 Whistler Street, Manly",
        suburb = "Manly",
        suburbLower = "manly",
        postcode = "2095",
        state = "NSW",
        registrationDate = "3 Aug 1995",
        lotCount = 12,
        managingAgent = "Northern Beaches Strata",
        managingAgentLicence = "7890123",
        lastAGM = "20 Jul 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -33.7969,
        longitude = 151.2869,
    ),

    StrataPlan(
        spNumber = "SP66543",
        address = "25 Moore Street, Liverpool",
        suburb = "Liverpool",
        suburbLower = "liverpool",
        postcode = "2170",
        state = "NSW",
        registrationDate = "17 Jun 2013",
        lotCount = 72,
        managingAgent = "South West Strata Services",
        managingAgentLicence = "8901234",
        lastAGM = "14 Jan 2025",
        buildingClass = "Class 2",
        workOrders = listOf(
            WorkOrder("rectification", "Structural cracking to columns B4–B7 on levels 3 and 4 requiring engineer assessment and rectification", "21 Feb 2024", "active"),
            WorkOrder("prohibition", "Pool area closed pending safety audit — non-compliant pool barrier", "30 Apr 2024", "active"),
        ),
        sinkingFundStatus = "critical",
        latitude = -33.9208,
        longitude = 150.9236,
    ),

    StrataPlan(
        spNumber = "SP22341",
        address = "180 Campbell Parade, Bondi Beach",
        suburb = "Bondi Beach",
        suburbLower = "bondi beach",
        postcode = "2026",
        state = "NSW",
        registrationDate = "30 Jan 2000",
        lotCount = 36,
        managingAgent = "Eastern Suburbs Strata",
        managingAgentLicence = "9012345",
        lastAGM = "8 Oct 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -33.8915,
        longitude = 151.2747,
    ),

    StrataPlan(
        spNumber = "SP55678",
        address = "100 Miller Street, North Sydney",
        suburb = "North Sydney",
        suburbLower = "north sydney",
        postcode = "2060",
        state = "NSW",
        registrationDate = "22 Mar 2009",
        lotCount = 54,
        managingAgent = "North Shore Premier Strata",
        managingAgentLicence = "0123456",
        lastAGM = "3 Feb 2025",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "low",
        latitude = -33.8394,
        longitude = 151.2073,
    ),

    // VIC plans
    StrataPlan(
        spNumber = "OC1234567",
        address = "1 Queens Road, Melbourne",
        suburb = "Melbourne",
        suburbLower = "melbourne",
        postcode = "3004",
        state = "VIC",
        registrationDate = "10 Oct 2007",
        lotCount = 84,
        managingAgent = "Melbourne Strata Management",
        managingAgentLicence = "VIC12345",
        lastAGM = "25 Aug 2024",
        buildingClass = "Class 2",
        workOrders = emptyList(),
        sinkingFundStatus = "adequate",
        latitude = -37.8399,
        longitude = 144.9738,
    ),

    StrataPlan(
        spNumber = "OC7654321",
        address = "50 Southbank Boulevard, Southbank",
        suburb = "Southbank",
        suburbLower = "southbank",
        postcode = "3006",
        state = "VIC",
        registrationDate = "5 May 2015",
        lotCount = 120,
        managingAgent = "Southbank OC Management",
        managingAgentLicence = "VIC67890",
        lastAGM = "12 Dec 2024",
        buildingClass = "Class 2",
        workOrders = listOf(
            WorkOrder("rectification", "External cladding non-compliant with current fire safety standards — rectification required", "9 Jul 2024", "active"),
        ),
        sinkingFundStatus = "low",
        latitude = -37.8234,
        longitude = 144.9656,
    ),
)
