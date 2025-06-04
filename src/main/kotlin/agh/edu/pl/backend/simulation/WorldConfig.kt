package agh.edu.pl.backend.simulation

import agh.edu.pl.backend.simulation.utils.Percentage

data class WorldConfig(
    val width: Int,
    val height: Int,
    val startNumberOfPeople: Int,
    val infectiousness: Percentage,
    val recoverChance: Percentage,
    val daysOfSimulation: Int,
    val infectionDistanceThreshold: Int,
    val numberOfPointOfInterest: Int,
    val numberOfHouses: Int,
    val numberOfHospitals: Int,
    val infectiousnessWithMask: Percentage
)
