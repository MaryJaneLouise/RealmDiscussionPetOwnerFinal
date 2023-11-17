package ph.edu.auf.realmdiscussionbarebones.models

data class Owner(
    val id: String,
    val name: String,
    val petCount: Int,
    val pets: List<Pet>
)