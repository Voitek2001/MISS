package agh.edu.pl.backend.simulation.agent

data class GovernmentPolicy(
    val level: Int,
    val maskRate: Double,
    val stayAtHomeRate: Double,
    val gatheringLimit: Int,
    val areaLockdown: Boolean,
    val isolation: Boolean
)
