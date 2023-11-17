package ph.edu.auf.realmdiscussionbarebones.models

import org.mongodb.kbson.BsonObjectId

data class PetType (
    var id: String,
    var petType: String,
    var type: Int
)