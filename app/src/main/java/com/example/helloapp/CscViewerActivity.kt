package com.example.helloapp

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextWatcher
import android.text.style.BackgroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.samsung.requirements.automatechecklisttest.R
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class CscViewerActivity : AppCompatActivity() {

    private lateinit var listAdapter: CscAdapter
    private lateinit var expandableAdapter: CscExpandableAdapter
    
    private lateinit var listView: ListView
    private lateinit var expandableListView: ExpandableListView

    private var fullList: List<Map.Entry<String, String>> = emptyList()
    private var filteredList: List<Map.Entry<String, String>> = emptyList()
    
    private var fullGroups: Map<String, List<Map.Entry<String, String>>> = emptyMap()
    private var filteredGroups: Map<String, List<Map.Entry<String, String>>> = emptyMap()
    
    private var isHierarchical = false
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_csc_viewer)

        val toolbar: Toolbar = findViewById(R.id.toolbar_csc)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        val type = intent.getStringExtra("FILE_TYPE") ?: "cscfeature.xml"
        title = type
        
        isHierarchical = (type == "customer.xml")

        listView = findViewById(R.id.csc_list)
        expandableListView = findViewById(R.id.csc_expandable_list)

        loadCscData(type)

        if (isHierarchical) {
            listView.visibility = View.GONE
            expandableListView.visibility = View.VISIBLE
            expandableAdapter = CscExpandableAdapter()
            expandableListView.setAdapter(expandableAdapter)
        } else {
            listView.visibility = View.VISIBLE
            expandableListView.visibility = View.GONE
            listAdapter = CscAdapter()
            listView.adapter = listAdapter
        }

        val searchBox: EditText = findViewById(R.id.search_box)
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentQuery = s.toString()
                filter(currentQuery)
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun loadCscData(type: String) {
        val decoderHelper = CscDecoderHelper(this)
        val salesCode = getSystemProperty("ro.csc.sales_code").ifEmpty {
            getSystemProperty("ro.boot.sales_code")
        }

        if (salesCode.isEmpty()) {
            Toast("Sales Code não encontrado")
            return
        }

        val basePath = "/optics/configs/carriers/single/$salesCode/conf"
        val filePath = when (type) {
            "customer_carrier_feature.json" -> "$basePath/system/customer_carrier_feature.json"
            "customer.xml" -> "$basePath/customer.xml"
            else -> "$basePath/system/cscfeature.xml"
        }

        val content = decoderHelper.decryptFile(filePath)
        
        if (isHierarchical) {
            fullGroups = decoderHelper.parseXmlToGroups(content)
            filteredGroups = fullGroups
        } else {
            val map = if (filePath.endsWith(".json")) {
                decoderHelper.parseJsonToMap(content)
            } else {
                decoderHelper.parseXmlToMap(content)
            }
            fullList = ArrayList(map.entries)
            filteredList = fullList
        }
    }

    private fun filter(query: String) {
        if (isHierarchical) {
            filteredGroups = if (query.isEmpty()) {
                fullGroups
            } else {
                val newMap = mutableMapOf<String, List<Map.Entry<String, String>>>()
                fullGroups.forEach { (group, items) ->
                    val hasMatchInItems = items.any { 
                        it.key.contains(query, ignoreCase = true) || 
                        it.value.contains(query, ignoreCase = true) 
                    }
                    if (hasMatchInItems || group.contains(query, ignoreCase = true)) {
                        newMap[group] = items
                    }
                }
                newMap
            }
            expandableAdapter.notifyDataSetChanged()
            if (query.isNotEmpty()) {
                for (i in 0 until expandableAdapter.groupCount) {
                    expandableListView.expandGroup(i)
                }
            }
        } else {
            filteredList = if (query.isEmpty()) {
                fullList
            } else {
                fullList.filter { 
                    it.key.contains(query, ignoreCase = true) || 
                    it.value.contains(query, ignoreCase = true) 
                }
            }
            listAdapter.notifyDataSetChanged()
        }
    }

    private fun getHighlightedText(fullText: String, query: String): CharSequence {
        if (query.isEmpty() || !fullText.contains(query, ignoreCase = true)) {
            return fullText
        }
        val spannable = SpannableString(fullText)
        val lowerText = fullText.lowercase(Locale.getDefault())
        val lowerQuery = query.lowercase(Locale.getDefault())
        
        var startPos = lowerText.indexOf(lowerQuery)
        while (startPos != -1) {
            val endPos = startPos + query.length
            spannable.setSpan(BackgroundColorSpan(Color.YELLOW), startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            startPos = lowerText.indexOf(lowerQuery, endPos)
        }
        return spannable
    }

    private fun getSystemProperty(propName: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(process.inputStream)).use { it.readLine() ?: "" }
        } catch (e: Exception) { "" }
    }

    private fun Toast(msg: String) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_LONG).show()
    }

    private inner class CscAdapter : BaseAdapter() {
        override fun getCount(): Int = filteredList.size
        override fun getItem(position: Int): Any = filteredList[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@CscViewerActivity).inflate(R.layout.item_csc_feature, parent, false)
            view.findViewById<View>(R.id.indent_line).visibility = View.GONE
            val entry = filteredList[position]
            view.findViewById<TextView>(R.id.feature_tag).text = getHighlightedText(entry.key, currentQuery)
            view.findViewById<TextView>(R.id.feature_value).text = getHighlightedText(entry.value, currentQuery)
            return view
        }
    }

    private inner class CscExpandableAdapter : BaseExpandableListAdapter() {
        private val groups get() = filteredGroups.keys.toList()
        override fun getGroupCount(): Int = groups.size
        override fun getChildrenCount(groupPosition: Int): Int {
            val size = filteredGroups[groups[groupPosition]]?.size ?: 0
            return if (size > 0) size + 1 else 0
        }
        override fun getGroup(groupPosition: Int): Any = groups[groupPosition]
        override fun getChild(groupPosition: Int, childPosition: Int): Any? {
            val list = filteredGroups[groups[groupPosition]] ?: return null
            return if (childPosition < list.size) list[childPosition] else null
        }
        override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()
        override fun getChildId(groupPosition: Int, childPosition: Int): Long = childPosition.toLong()
        override fun hasStableIds(): Boolean = true
        override fun getGroupView(groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@CscViewerActivity).inflate(R.layout.item_csc_group, parent, false)
            val title = view.findViewById<TextView>(R.id.group_title)
            val indicator = view.findViewById<ImageView>(R.id.group_indicator)
            title.text = getHighlightedText(getGroup(groupPosition) as String, currentQuery)
            indicator.rotation = if (isExpanded) 0f else -90f
            return view
        }
        override fun getChildView(groupPosition: Int, childPosition: Int, isLastChild: Boolean, convertView: View?, parent: ViewGroup?): View {
            val list = filteredGroups[groups[groupPosition]] ?: return View(this@CscViewerActivity)
            if (childPosition == list.size) {
                val view = LayoutInflater.from(this@CscViewerActivity).inflate(R.layout.item_csc_footer, parent, false)
                view.findViewById<TextView>(R.id.footer_text).text = "Fim de ${groups[groupPosition]}"
                return view
            }
            val view = convertView?.let { 
                if (it.findViewById<View>(R.id.indent_line) != null) it 
                else LayoutInflater.from(this@CscViewerActivity).inflate(R.layout.item_csc_feature, parent, false)
            } ?: LayoutInflater.from(this@CscViewerActivity).inflate(R.layout.item_csc_feature, parent, false)
            view.findViewById<View>(R.id.indent_line).visibility = View.VISIBLE
            val entry = list[childPosition] as Map.Entry<String, String>
            view.findViewById<TextView>(R.id.feature_tag).text = getHighlightedText(entry.key, currentQuery)
            view.findViewById<TextView>(R.id.feature_value).text = getHighlightedText(entry.value, currentQuery)
            return view
        }
        override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = true
    }
}
