package com.example.formula1app.data

object DemoStandings {

    val items: List<DriverStanding> = listOf(
        DriverStanding(
            driverId = "max_verstappen", position = 1, points = 100.0,
            driverName = "Max Verstappen",
            teamId = "red_bull", teamName = "Red Bull"
        ),
        DriverStanding(
            driverId = "norris", position = 2, points = 95.0,
            driverName = "Lando Norris",
            teamId = "mclaren", teamName = "McLaren"
        ),
        DriverStanding(
            driverId = "leclerc", position = 3, points = 80.0,
            driverName = "Charles Leclerc",
            teamId = "ferrari", teamName = "Ferrari"
        ),
        DriverStanding(
            driverId = "russell", position = 4, points = 65.0,
            driverName = "George Russell",
            teamId = "mercedes", teamName = "Mercedes"
        ),
        DriverStanding(
            driverId = "alonso", position = 5, points = 60.0,
            driverName = "Fernando Alonso",
            teamId = "aston_martin", teamName = "Aston Martin"
        ),
        DriverStanding(
            driverId = "gasly", position = 6, points = 48.0,
            driverName = "Pierre Gasly",
            teamId = "alpine", teamName = "Alpine"
        ),
        DriverStanding(
            driverId = "albon", position = 7, points = 40.0,
            driverName = "Alexander Albon",
            teamId = "williams", teamName = "Williams"
        ),
        DriverStanding(
            driverId = "lawson", position = 8, points = 30.0,
            driverName = "Liam Lawson",
            teamId = "rb", teamName = "RB"
        ),
        DriverStanding(
            driverId = "hulkenberg", position = 9, points = 22.0,
            driverName = "Nico Hülkenberg",
            teamId = "audi", teamName = "Audi"
        ),
        DriverStanding(
            driverId = "perez", position = 10, points = 16.0,
            driverName = "Sergio Pérez",
            teamId = "cadillac", teamName = "Cadillac"
        ),
        DriverStanding(
            driverId = "ocon", position = 11, points = 10.0,
            driverName = "Esteban Ocon",
            teamId = "haas", teamName = "Haas"
        )
    )
}