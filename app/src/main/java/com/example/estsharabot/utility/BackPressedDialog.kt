package com.example.estsharabot.utility

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment

class BackPressedDialog() : DialogFragment() {
    companion object {
        var discard = false
    }
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        AlertDialog.Builder(requireContext())
            .setMessage("You are going to discard this session")
            .setPositiveButton("Discard") { _, _ ->
                discard = true
            }
            .setNegativeButton("Stay") { _, _ -> }
            .create()
}