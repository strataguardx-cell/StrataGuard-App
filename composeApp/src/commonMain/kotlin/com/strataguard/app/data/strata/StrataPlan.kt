package com.strataguard.app.data.strata

import kotlinx.serialization.Serializable

@Serializable
data class StrataPlan(
    val spNumber: String = "",
    val address: String = "",
    val suburb: String = "",
    val suburbLower: String = "",
    val postcode: String = "",
    val state: String = "NSW",
    val registrationDate: String = "",
    val lotCount: Int = 0,
    val managingAgent: String = "",
    val managingAgentLicence: String = "",
    val lastAGM: String = "",
    val buildingClass: String = "Class 2",
    val workOrders: List<WorkOrder> = emptyList(),
    val sinkingFundStatus: String = "unknown",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)

@Serializable
data class WorkOrder(
    val type: String = "",
    val description: String = "",
    val issueDate: String = "",
    val status: String = "active",
)

val StrataPlan.hasActiveWorkOrders: Boolean
    get() = workOrders.any { it.status == "active" }

val StrataPlan.displaySinkingFundStatus: SinkingFundStatus
    get() = when (sinkingFundStatus) {
        "adequate" -> SinkingFundStatus.ADEQUATE
        "low" -> SinkingFundStatus.LOW
        "critical" -> SinkingFundStatus.CRITICAL
        else -> SinkingFundStatus.UNKNOWN
    }

enum class SinkingFundStatus { ADEQUATE, LOW, CRITICAL, UNKNOWN }

enum class WorkOrderType(val label: String) {
    RECTIFICATION("Rectification Order"),
    STOP_WORK("Stop Work Order"),
    PROHIBITION("Prohibition Order"),
    UNKNOWN("Work Order"),
}

val WorkOrder.displayType: WorkOrderType
    get() = when (type) {
        "rectification" -> WorkOrderType.RECTIFICATION
        "stop_work" -> WorkOrderType.STOP_WORK
        "prohibition" -> WorkOrderType.PROHIBITION
        else -> WorkOrderType.UNKNOWN
    }
