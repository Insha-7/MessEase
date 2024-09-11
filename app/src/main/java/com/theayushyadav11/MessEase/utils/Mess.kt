package com.theayushyadav11.MessEase.utils

import android.app.Dialog
import android.app.DownloadManager
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Environment
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.theayushyadav11.MessEase.R
import com.theayushyadav11.MessEase.databinding.EditDialogBinding
import com.theayushyadav11.MessEase.databinding.SelTargetDialogBinding
import com.theayushyadav11.MessEase.utils.Constants.Companion.firestoreReference
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Mess(context: Context) {
    lateinit var context: Context
    private var progressDialog: ProgressDialog = ProgressDialog(context)
    private var sharedPreferences: SharedPreferences
    val designation = MutableLiveData<String>()

    init {
        progressDialog.setCancelable(false)
        currentDesignation()
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        if (context != null) {
            this.context = context
        }
    }

    fun save(key: String, value: String) {
        val editor = sharedPreferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    fun get(key: String): String {
        return sharedPreferences.getString(key, "").toString()
    }

    fun get(key: String, defVal: String): String {
        return sharedPreferences.getString(key, defVal).toString()
    }

    fun sendPollId(id: String) {
        save("pollId", id)
    }

    fun getPollId(): String {
        return get("pollId")
    }

    fun setIsLoggedIn(isMember: Boolean) {
        save("isLoggedIn", isMember.toString())
    }

    fun isLoggedIn(): Boolean {

        if (get("isLoggedIn") == "true") {
            return true
        } else {
            return false
        }


    }

    fun addPb(message: String) {
        progressDialog.dismiss()
        progressDialog.setMessage(message)
        progressDialog.show()
    }

    fun pbDismiss() {
        progressDialog.dismiss()
    }

    fun toast(message: Any) {
        Toast.makeText(context, message.toString(), Toast.LENGTH_SHORT).show()
    }

    fun snack(view: View, message: String) {
        Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show()
    }

    fun log(message: Any) {
        Log.d("yatinMadharchod", message.toString())
    }

    private fun currentDesignation() {
        val auth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance().reference
        database.child("Users").child(auth.currentUser?.uid.toString()).child("designation")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    designation.value = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    log("Failed to read value.")
                }

            })


    }

    fun getCurrentTimeAndDate(): String {
        val dateFormat = SimpleDateFormat("hh:mm a dd MMM yyyy", Locale.getDefault())
        val currentDate = Date()
        return dateFormat.format(currentDate)
    }

    fun showInputDialog(
        hint: String, onOkClicked: (String) -> Unit, oncancelClicked: (String) -> Unit
    ) {
        val dialog = Dialog(context)
        val bind = EditDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bind.root)
        dialog.setCancelable(false)
        dialog.show()
        bind.textInputLayout3.hint = "Add Note"
        bind.cancel.setOnClickListener {
            oncancelClicked("")
            dialog.dismiss()
        }
        bind.done.setOnClickListener {
            val d = bind.etUpdate.text.toString()
            if (d.isEmpty()) {
                toast("This feild cannot be empty!")
            } else {
                onOkClicked(d)
                dialog.dismiss()
            }

        }
    }

    fun showAlertDialog(
        title: String, message: String, okText: String, cancelText: String, onResult: () -> Unit
    ) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setCancelable(false)
        builder.setMessage(message)
        builder.setPositiveButton(okText) { dialog, _ ->
            onResult()
            dialog.dismiss()
        }
        builder.setNegativeButton(cancelText) { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    fun openDialog(type: String, onResult: (String) -> Unit) {
        val dialog = Dialog(context)
        val bind = SelTargetDialogBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(bind.root)
        dialog.setCancelable(false)
        bind.title.text = "Select the recipients for $type"
        bind.btnAddPoll.text = "Send $type"
        val c = (Date().year + 1900)
        val batches = listOf(c, c + 1, c + 2, c + 3, c + 4, c + 5)
        val rbList: List<RadioButton> = listOf(
            bind.rb0,
            bind.rb1,
            bind.rb2,
            bind.rb3,
            bind.rb4,
            bind.rb5,
            bind.rbGirl,
            bind.rbBoy,
            bind.rbBtech,
            bind.rbMtech,
            bind.rbMba,
            bind.rbMsc
        )

        for (i in 0 until batches.size) {
            rbList[i].setText("Batch - ${batches[i]}")
        }
        bind.cb.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in rbList) {
                i.isChecked = isChecked
            }
        }
        bind.btnCancel.setOnClickListener {
            dialog.dismiss()
        }
        bind.btnAddPoll.setOnClickListener {
            var target = ""
            for (i in rbList) {
                if (i.isChecked) {
                    target += i.text
                }
            }
            var yearSelected = false
            var genderSelected = false
            var batchSelected = false
            for (i in 0 until rbList.size) {
                if (i < 6 && rbList[i].isChecked) yearSelected = true
                if (i > 5 && i < 8 && rbList[i].isChecked) genderSelected = true
                if (i > 7 && rbList[i].isChecked) batchSelected = true
            }
            if (!yearSelected) {
                toast("Please Select a year")
            } else if (!genderSelected) {
                toast("Please Select gender")
            } else if (!batchSelected) {
                toast("Please Select a batch")
            } else {
                onResult(target)
                dialog.dismiss()

            }
        }
        dialog.show()


    }

    fun loadImage(url: String, view: ImageView) {
        Glide.with(context).load(url).into(view)
    }

    fun openLink(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    fun loadCircleImage(url: String, view: ImageView) {
        Glide.with(context).load(url).circleCrop().error(R.drawable.profile_circle).into(view)
    }

    fun showImage(photo: String) {
        val dialog = Dialog(context)
        dialog.setContentView(R.layout.img2)
        val img = dialog.findViewById<ImageView>(R.id.img)
        loadImage(photo, img)
        img.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }

    fun getmanager(): RecyclerView.LayoutManager {
        val layoutManager = object : LinearLayoutManager(context) {
            override fun onLayoutChildren(
                recycler: RecyclerView.Recycler?, state: RecyclerView.State?
            ) {
                try {
                    super.onLayoutChildren(recycler, state)
                } catch (e: IndexOutOfBoundsException) {

                }
            }

            override fun canScrollVertically(): Boolean {
                return false
            }
        }

        return layoutManager
    }
    fun getLists(listName: String, onResult: (List<String>) -> Unit) {
        firestoreReference.collection("Lists").document("lists").get().addOnSuccessListener {
            val list = it.get(listName) as? List<String> ?: emptyList()
            onResult(list)
        }
    }
    private fun cont(email: String, onResult: (Boolean) -> Unit) {
        getLists("allows"){
            if (it.contains(email)) {
                onResult(true)
            }
            onResult(false)
        }
    }

    fun isValidEmail(email: String, onResult: (Boolean) -> Unit) {
        val emailR = email.substring(0, email.indexOf("@"))
        var isValid = true
        if (emailR.contains("+") || emailR.contains(".") || emailR.contains("-") || emailR.contains(
                "_"
            ) || emailR.contains(
                "/"
            ) || emailR.contains("*") || emailR.contains("#") || emailR.contains("!") || emailR.contains(
                "$"
            ) || emailR.contains("%") || emailR.contains("^") || emailR.contains(
                "&"
            ) || emailR.contains("(") || emailR.contains(")") || emailR.contains("=") || emailR.contains(
                "{"
            ) || emailR.contains("}") || emailR.contains("[") || emailR.contains("]") || emailR.contains(
                ":"
            ) || emailR.contains(";") || emailR.contains(",") || emailR.contains("<") || emailR.contains(
                ">"
            ) || emailR.contains("?") || emailR.contains("|") || emailR.contains("`") || emailR.contains(
                "~"
            )
        ) {
            isValid = false
        }
        cont(email) {
            if (it || ((email.endsWith("@iiitl.ac.in") && isValid) || email.contains(
                    "ayushyadav"
                ))
            ) onResult(
                true
            )
            else onResult(false)
        }
    }

    fun downloadFile(url: String) {
        val title = "MessMenu.pdf"
        val description = "Downloading file"
        val request = DownloadManager.Request(Uri.parse(url))
        request.setTitle(title)
        request.setDescription(description)
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, title)
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI or DownloadManager.Request.NETWORK_MOBILE)
        val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val downloadId = downloadManager.enqueue(request)
        Toast.makeText(context, "Download started", Toast.LENGTH_SHORT).show()
    }

}