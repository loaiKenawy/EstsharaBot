package com.example.estsharabot.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.estsharabot.adapters.ReportsAdapter
import com.example.estsharabot.databinding.FragmentReportsBinding
import java.io.File

class ReportsFragment : Fragment() {

    private val TAG = "ReportsFragment"

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!


    private var mList = ArrayList<File>()

    private lateinit var reportsAdapter: ReportsAdapter
    private lateinit var reportsRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        val view = binding.root

        if (mList.isEmpty()) {
            binding.rvReports.visibility = View.GONE
            binding.tvNoData.visibility = View.VISIBLE
        } else {
            initRecyclerView()
        }

        return view
    }

    private fun initRecyclerView() {
        val layoutManager = LinearLayoutManager(context)
        reportsAdapter = context?.let { ReportsAdapter(it, mList) }!!
        reportsRecyclerView = binding.rvReports
        reportsRecyclerView.adapter = reportsAdapter
        reportsRecyclerView.layoutManager = layoutManager
        reportsRecyclerView.smoothScrollToPosition(mList.size - 1)

    }

    private fun findPdf(file: File): ArrayList<File> {

        var array = ArrayList<File>()

        val files = file.listFiles()
        for (items in files!!) {
            if (items.isDirectory && !items.isHidden) {
                array.addAll(findPdf(items));
            } else {
                if (items.getName().endsWith(".pdf")) {
                    array.add(items);
                }
            }
        }
        return array
    }

}