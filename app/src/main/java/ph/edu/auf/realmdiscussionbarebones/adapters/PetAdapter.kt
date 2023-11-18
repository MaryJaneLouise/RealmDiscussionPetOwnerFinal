package ph.edu.auf.realmdiscussionbarebones.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ph.edu.auf.realmdiscussionbarebones.R
import ph.edu.auf.realmdiscussionbarebones.RealmDiscussionApplication
import ph.edu.auf.realmdiscussionbarebones.databinding.ContentPetRvBinding
import ph.edu.auf.realmdiscussionbarebones.models.Pet
import ph.edu.auf.realmdiscussionbarebones.realm.RealmDatabase

class PetAdapter(private var petList: ArrayList<Pet>, private var context: Context, var petAdapterCallback: PetAdapterInterface): RecyclerView.Adapter<PetAdapter.PetViewHolder>() {
    private var database = RealmDatabase()

    interface PetAdapterInterface{
        fun deletePet(id: String)
        fun updateOwnerForPet(pet: Pet, newOwnerName: String)

        fun updatePet(pet: Pet, newPetName: String, newAge: Int, newType: String, newOwnerName: String)
    }

    inner class PetViewHolder(val binding: ContentPetRvBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData : Pet) {
            with(binding){
                txtPetName.text = String.format("%s", itemData.name)

                // Checks each pet if their age is 1 or above
                if (itemData.age > 1) {
                    txtAge.text = String.format("%s years old", itemData.age.toString())
                } else {
                    txtAge.text = String.format("%s year old", itemData.age.toString())
                }

                // Checks if the type of pet is a dog or cat
                txtPetType.text = String.format("%s", itemData.petType)
                if (itemData.petType == "Dog") {
                    petImage.setImageResource(R.drawable.dog)
                } else {
                    petImage.setImageResource(R.drawable.cat)
                }

                // Checks each pet if they have owners or none
                if (itemData.ownerName.isNotEmpty()) {
                    txtOwnerName.visibility = View.VISIBLE
                    btnAdopt.visibility = View.GONE
                    txtOwnerName.text = String.format("Owned by %s",itemData.ownerName)
                } else {
                    txtOwnerName.visibility = View.GONE
                    btnAdopt.visibility = View.VISIBLE

                    // Adds an owner for the selected pet
                    btnAdopt.setOnClickListener {
                        val builder = AlertDialog.Builder(context)
                        val inflater = LayoutInflater.from(context)
                        val view = inflater.inflate(R.layout.dialog_add_owner_to_pet, null)

                        val title = view.findViewById<TextView>(R.id.dialog_add_owner_title)
                        val input = view.findViewById<EditText>(R.id.dialog_add_owner_name)

                        title.text = "Add owner to ${itemData.name}"
                        builder.setView(view)

                        builder.setPositiveButton("Add owner") { dialog, _ ->
                            val newOwnerName = input.text.toString()
                            if (newOwnerName.isNotEmpty()) {
                                if (newOwnerName.isNullOrEmpty() || newOwnerName.isNullOrBlank()) {
                                    Toast.makeText(context, "You cannot assign a blank owner.", Toast.LENGTH_SHORT).show()
                                    dialog.cancel()
                                } else {
                                    petAdapterCallback.updateOwnerForPet(itemData, newOwnerName)
                                    dialog.dismiss()
                                }
                            }
                        }
                        builder.setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                        }
                        builder.show()
                    }
                }

                // Removes the selected pet in the database
                btnRemove.setOnClickListener{
                    val builder = AlertDialog.Builder(context)
                    builder.setMessage("Are you sure you want to delete this pet?")
                    builder.setTitle("Warning!")
                    builder.setPositiveButton("Yes"){dialog, _ ->
                        petList.removeAt(adapterPosition)
                        notifyItemRemoved(adapterPosition)
                        petAdapterCallback.deletePet(itemData.id)
                        Toast.makeText(context, "The selected pet has been deleted successfully!", Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    builder.setNegativeButton("No") {dialog, _ ->
                        dialog.dismiss()
                    }
                    builder.show()
                }

                // Edit the details for the pet
                btnEditPet.setOnClickListener {
                    val builder = AlertDialog.Builder(context)
                    val inflater = LayoutInflater.from(context)
                    val view = inflater.inflate(R.layout.dialog_edit_pet, null)

                    builder.setView(view)

                    val inputName = view.findViewById<EditText>(R.id.dialog_edit_pet_name)
                    val inputAge = view.findViewById<EditText>(R.id.dialog_edit_pet_age)
                    val inputType = view.findViewById<Spinner>(R.id.dialog_edit_pet_type)
                    val inputNewOwner = view.findViewById<EditText>(R.id.dialog_edit_pet_owner)

                    val spinner: Spinner = inputType
                    val petTypes = database.getAllPetTypes()

                    val petTypesArray = petTypes.map { it.petType }.toTypedArray()
                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, petTypesArray)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    spinner.adapter = adapter

                    val selectedItemPosition = petTypesArray.indexOf(itemData.petType)

                    // Set the existing values
                    inputName.setText("${itemData.name}")
                    inputAge.setText("${itemData.age}")
                    spinner.setSelection(selectedItemPosition)
                    inputNewOwner.setText("${itemData.ownerName}")

                    builder.setCancelable(false)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newName = inputName.text.toString()
                        val newAge = inputAge.text.toString()
                        val newOwner = inputNewOwner.text.toString()
                        val selectedPetType = spinner.selectedItem as String

                        if ((newName.isNotEmpty() && newName.isNotBlank()) &&
                            (newAge >= "1" && newAge.isNotEmpty() && newAge.isNotBlank()) &&
                            (newOwner.isNotEmpty() && newOwner.isNotBlank())) {
                            if (newOwner.isNullOrEmpty() || newOwner.isNullOrBlank()) {
                                Toast.makeText(context, "You cannot assign a blank owner.", Toast.LENGTH_SHORT).show()
                                dialog.cancel()
                            } else {
                                petAdapterCallback.updatePet(itemData, newName, newAge.toInt(), selectedPetType, newOwner)
                                Toast.makeText(context, "Pet details updated successfully!", Toast.LENGTH_SHORT).show()
                                dialog.dismiss()
                            }
                        } else {
                            Toast.makeText(context, "Fill the required fields.", Toast.LENGTH_SHORT).show()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                    builder.show()
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PetViewHolder {
        val binding = ContentPetRvBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PetViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PetViewHolder, position: Int) {
        val petData = petList[position]
        holder.bind(petData)
    }

    override fun getItemCount(): Int {
        return petList.size
    }

    fun updatePetList(petList: ArrayList<Pet>){
        this.petList = arrayListOf()
        notifyDataSetChanged()
        this.petList = petList
        this.notifyItemInserted(this.petList.size)
    }

}