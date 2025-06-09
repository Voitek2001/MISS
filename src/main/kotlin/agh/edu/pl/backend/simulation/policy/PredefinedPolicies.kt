package agh.edu.pl.backend.simulation.policy



val predefinedPolicies = listOf(
    GovernmentPolicy(
        level = 0,
        maskRate = 0.1,
        stayAtHomeRate = 0.0,
        gatheringLimit = Int.MAX_VALUE,
        areaLockdown = false,
        isolation = false
    ),
    GovernmentPolicy(
        level = 1,
        maskRate = 0.3,
        stayAtHomeRate = 0.2,
        gatheringLimit = Int.MAX_VALUE,
        areaLockdown = false,
        isolation = false
    ),
    GovernmentPolicy(
        level = 2,
        maskRate = 0.3,
        stayAtHomeRate = 0.5,
        gatheringLimit = Int.MAX_VALUE,
        areaLockdown = false,
        isolation = false
    ),
    GovernmentPolicy(
        level = 3,
        maskRate = 0.3,
        stayAtHomeRate = 0.5,
        gatheringLimit = 8,
        areaLockdown = false,
        isolation = false
    ),
    GovernmentPolicy(
        level = 4,
        maskRate = 0.3,
        stayAtHomeRate = 0.5,
        gatheringLimit = 4,
        areaLockdown = false,
        isolation = true
    ),
    GovernmentPolicy(
        level = 5,
        maskRate = 0.5,
        stayAtHomeRate = 0.8,
        gatheringLimit = 0,
        areaLockdown = true,
        isolation = true
    )
)