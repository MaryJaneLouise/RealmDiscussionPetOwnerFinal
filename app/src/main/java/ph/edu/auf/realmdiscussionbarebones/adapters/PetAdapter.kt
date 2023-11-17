package ph.edu.auf.realmdiscussionbarebones.adapters

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import ph.edu.auf.realmdiscussionbarebones.RealmDiscussionApplication
import ph.edu.auf.realmdiscussionbarebones.databinding.ContentPetRvBinding
import ph.edu.auf.realmdiscussionbarebones.models.Pet

class PetAdapter(private var petList: ArrayList<Pet>, private var context: Context, private var petAdapterCallback: PetAdapterInterface): RecyclerView.Adapter<PetAdapter.PetViewHolder>() {

    interface PetAdapterInterface{
        fun deletePet(id: String)
        fun updateOwnerForPet(pet: Pet, newOwnerName: String)

        fun updatePet(pet: Pet, newPetName: String, newAge: Int, newOwnerName: String)
    }

    inner class PetViewHolder(private val binding: ContentPetRvBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(itemData : Pet) {

            with(binding){
                txtPetName.text = String.format("%s", itemData.name)

                // Checks each pet if their age is 1 or above
                if (itemData.age > 1) {
                    txtAge.text = String.format("%s years old", itemData.age.toString())
                } else {
                    txtAge.text = String.format("%s year old", itemData.age.toString())
                }

                txtPetType.text = String.format("%s", itemData.petType)

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
                        builder.setTitle("Add owner to ${itemData.name}")

                        val input = EditText(context)
                        input.inputType = InputType.TYPE_CLASS_TEXT
                        builder.setView(input)

                        builder.setPositiveButton("Add owner") { dialog, _ ->
                            val newOwnerName = input.text.toString()
                            if (newOwnerName.isNotEmpty()) {
                                petAdapterCallback.updateOwnerForPet(itemData, newOwnerName)
                            }
                            dialog.dismiss()
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
                    builder.setTitle("Edit details for ${itemData.name}")

                    val layout = LinearLayout(context)
                    layout.orientation = LinearLayout.VERTICAL

                    val inputName = EditText(context)
                    inputName.inputType = InputType.TYPE_CLASS_TEXT
                    inputName.setText("${itemData.name}")
                    layout.addView(inputName)

                    val inputAge = EditText(context)
                    inputAge.inputType = InputType.TYPE_CLASS_TEXT
                    inputAge.setText("${itemData.age}")
                    layout.addView(inputAge)

                    val inputNewOwner = EditText(context)
                    inputNewOwner.inputType = InputType.TYPE_CLASS_TEXT
                    inputNewOwner.setText("${itemData.ownerName}")
                    layout.addView(inputNewOwner)

                    builder.setView(layout)

                    builder.setPositiveButton("Update details") { dialog, _ ->
                        val newName = inputName.text.toString()
                        val newAge = inputAge.text.toString().toInt()
                        val newOwner = inputNewOwner.text.toString()

                        if (newName.isNotEmpty() && (newAge > 1 && newAge.toString().isNotEmpty()) && newOwner.isNotEmpty()) {
                            petAdapterCallback.updatePet(itemData, newName, newAge, newOwner)
                            dialog.dismiss()
                        }
                    }
                    builder.setNegativeButton("Cancel") { dialog, _ ->
                        dialog.dismiss()
                    }

                    builder.show()
                }

            }
//            ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
//                override fun onMove(
//                    recyclerView: RecyclerView,
//                    viewHolder: RecyclerView.ViewHolder,
//                    target: RecyclerView.ViewHolder
//                ): Boolean {
//                    // this method is called
//                    // when the item is moved.
//                    return false
//                }
//                override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
//                    // this method is called when we swipe our item to right direction.
//                    // on below line we are getting the item at a particular position.
//                    val deletedPet: Pet =
//                        petList[viewHolder.adapterPosition]
//
//                    val position = viewHolder.adapterPosition
//                    petList.removeAt(viewHolder.adapterPosition)
//                    notifyItemRemoved(adapterPosition)
//                    petAdapterCallback.deletePet(itemData.id)
//
//                    // Show snackbar to bring back the item
//                    Snackbar.make(RealmDiscussionApplication.petRecyclerView, "Deleted " + deletedPet.name, Snackbar.LENGTH_LONG)
//                        .setAction(
//                            "Undo",
//                            View.OnClickListener {
//                                // adding on click listener to our action of snack bar.
//                                // below line is to add our item to array list with a position.
//                                petList.add(position, deletedPet)
//
//                                // below line is to notify item is
//                                // added to our adapter class.
//                                notifyItemInserted(position)
//                            }).show()
//                }
//            }).attachToRecyclerView(RealmDiscussionApplication.petRecyclerView)
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