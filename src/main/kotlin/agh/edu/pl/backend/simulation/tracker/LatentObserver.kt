package agh.edu.pl.backend.simulation.tracker

import agh.edu.pl.backend.simulation.agent.HealthStatus

fun interface LatentObserver {

    fun updateLatent(oldHealthStatus: HealthStatus)
}