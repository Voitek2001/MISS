package agh.edu.pl.backend.simulation.tracker

import agh.edu.pl.backend.simulation.agent.HealthStatus

fun interface ImmuneObserver {
    fun updateImmune(oldHealthStatus: HealthStatus)
}