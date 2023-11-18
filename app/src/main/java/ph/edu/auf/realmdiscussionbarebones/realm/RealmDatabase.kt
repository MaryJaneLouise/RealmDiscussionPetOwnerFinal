package ph.edu.auf.realmdiscussionbarebones.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
import ph.edu.auf.realmdiscussionbarebones.models.Owner
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.OwnerRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetRealm
import ph.edu.auf.realmdiscussionbarebones.realm.realmmodels.PetTypeRealm
import ph.edu.auf.realmdiscussionbarebones.utils.Encrypter
import java.lang.IllegalStateException

class RealmDatabase {
    private val realm : Realm by lazy {
        val config = RealmConfiguration
            .Builder(schema =  setOf(PetRealm::class, OwnerRealm::class, PetTypeRealm::class))
            .schemaVersion(1)
            .initialData {
                copyToRealm(PetTypeRealm().apply {
                    petType = "Cat"
                    type = 1
                })
                copyToRealm(PetTypeRealm().apply {
                    petType = "Dog"
                    type = 2
                })
            }
            .build()
        Realm.open(config)
    }

    // Get all pets as normal
    fun getAllPets() : List<PetRealm> {
        return realm.query<PetRealm>().find()
    }

    fun getAllPetTypes(): List<PetTypeRealm> {
        return realm.query<PetTypeRealm>().find()
    }

    // Get all pets as Flows (Compose)
    fun getAllPetsAsFlow() : Flow<List<PetRealm>> {
        return realm.query<PetRealm>().asFlow().map { it.list }
    }

    // Search Query for Pets
    fun getPetsByName(name: String) : List<PetRealm> {
        return realm.query<PetRealm>("name CONTAINS[c] $0", name).find()
    }

    // Add Pet object
    suspend fun addPet(name: String, age: Int, type: String, ownerName: String = "") {
        realm.write {
            val pet = PetRealm().apply {
                this.name = name
                this.age = age
                this.petType = type
            }

            val managePet = copyToRealm(pet)

            if (ownerName.isNotEmpty()) {
                // Check if there's an owner
                val ownerResult: OwnerRealm? = realm.query<OwnerRealm>("name == $0", ownerName).first().find()

                if (ownerResult == null) {
                    // If there's no owner
                    val owner = OwnerRealm().apply {
                        this.name = ownerName
                        this.pets.add(managePet)
                    }

                    val manageOwner = copyToRealm(owner)
                    managePet.owner = manageOwner
                } else {
                    // If there's an owner
                    findLatest(ownerResult)?.pets?.add(managePet)
                    findLatest(managePet)?.owner = findLatest(ownerResult)
                }
            }
        }
    }

    // Adds Owner object to a Pet object
    suspend fun updateOwnerForPet(pet: Pet, newOwnerName: String) {
        realm.write {
            // Find the pet to update
            val petResult: PetRealm? = realm.query<PetRealm>("id == $0", ObjectId(pet.id)).first().find()

            if (petResult != null) {
                // If the pet exists
                val petRealm = findLatest(petResult)

                // Check if there's an owner
                val ownerResult: OwnerRealm? = realm.query<OwnerRealm>("name == $0", newOwnerName).first().find()

                if (ownerResult == null) {
                    // If there's no owner
                    val owner = OwnerRealm().apply {
                        this.name = newOwnerName
                        this.pets.add(petRealm!!)
                    }

                    val manageOwner = copyToRealm(owner)
                    petRealm?.owner = manageOwner
                } else {
                    // If there's an owner
                    findLatest(ownerResult)?.pets?.add(petRealm!!)
                    petRealm?.owner = findLatest(ownerResult)
                }
            }
        }
    }

    // Updates the details of the Pet object
    suspend fun updatePet(pet: Pet, newPetName: String, newAge: Int, newType: String, newOwnerName: String) {
        realm.write {
            // Find the pet to update
            val petResult: PetRealm? = realm.query<PetRealm>("id == $0", ObjectId(pet.id)).first().find()

            if (petResult != null) {
                // If the pet exists
                val petRealm = findLatest(petResult)

                // Update the pet data
                petRealm?.apply {
                    this.name = newPetName
                    this.age = newAge
                    this.petType = newType
                }

                petRealm?.owner?.pets?.remove(petRealm)

                if (newOwnerName.isNotEmpty()) {
                    // Check if there's an owner
                    val ownerResult: OwnerRealm? = realm.query<OwnerRealm>("name == $0", newOwnerName).first().find()

                    if (ownerResult == null) {
                        // If there's no owner
                        val owner = OwnerRealm().apply {
                            this.name = newOwnerName
                            this.pets.add(petRealm!!)
                        }

                        val manageOwner = copyToRealm(owner)
                        petRealm?.owner = manageOwner
                    } else {
                        // If there's an owner
                        findLatest(ownerResult)?.pets?.add(petRealm!!)
                        petRealm?.owner = findLatest(ownerResult)
                    }
                }
            }
        }
    }

    // Delete Pet
    suspend fun deletePet(id: ObjectId) {
        realm.write {
            query<PetRealm>("id == $0", id)
                .first()
                .find()
                ?.let { delete(it) }
                ?: throw IllegalStateException("Pet not found")
        }
    }

    // Updates the details for the Owner object
    suspend fun updateOwner(owner: Owner, newName: String) {
        realm.write {
            // Find the owner to update
            val ownerResult: OwnerRealm? = realm.query<OwnerRealm>("id == $0", ObjectId(owner.id)).first().find()

            if (ownerResult != null) {
                // If the owner exists
                val ownerRealm = findLatest(ownerResult)

                // Update the owner data
                ownerRealm?.apply {
                    this.name = newName
                }
            }
        }
    }

    // Delete owner
    suspend fun deleteOwner(id: ObjectId) {
        realm.write {
            query<OwnerRealm>("id == $0", id)
                .first()
                .find()
                ?.let { delete(it) }
                ?:throw IllegalStateException("Owner not found")
        }
    }

    // Get all of the owners in a list
    fun getAllOwners() : List<OwnerRealm> {
        return realm.query<OwnerRealm>().find()
    }

    // Search Query for owner
    // [c] means ignore case
    fun getOwnerByName(name: String) : List<OwnerRealm> {
        return realm.query<OwnerRealm>("name CONTAINS[c] $0", name).find()
    }
}