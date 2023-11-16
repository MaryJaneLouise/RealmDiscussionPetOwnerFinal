package ph.edu.auf.realmdiscussionbarebones.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mongodb.kbson.ObjectId
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

    // Get all pets as Flows (Compose)
    fun getAllPetsAsFlow() : Flow<List<PetRealm>> {
        return realm.query<PetRealm>().asFlow().map { it.list }
    }

    // Search Query
    fun getPetsByName(name: String) : List<PetRealm> {
        return realm.query<PetRealm>("name CONTAINS $0", name).find()
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

    fun getAllOwners() : List<OwnerRealm> {
        return realm.query<OwnerRealm>().find()
    }
}