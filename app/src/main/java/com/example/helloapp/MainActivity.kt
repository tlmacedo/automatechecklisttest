package com.example.helloapp

import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.samsung.requirements.automatechecklisttest.R
import java.io.BufferedReader
import java.io.InputStreamReader

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: Drawable,
    val version: String
)

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var packageList: ListView
    private lateinit var layoutHome: View
    private lateinit var layoutTests: View
    private lateinit var containerInfo: LinearLayout
    private lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        packageList = findViewById(R.id.package_list)
        layoutHome = findViewById(R.id.layout_home)
        layoutTests = findViewById(R.id.layout_tests)
        containerInfo = findViewById(R.id.container_info)

        setupSummary()
        setupTestButtons()
        loadInstalledPackages()
        
        // Garante que inicia na Home
        showHome()
    }

    override fun onRestart() {
        super.onRestart()
        // Sempre que voltar de outra Activity (como a de CSC), reseta para a Home
        showHome()
    }

    private fun showHome() {
        layoutHome.visibility = View.VISIBLE
        layoutTests.visibility = View.GONE
        packageList.visibility = View.GONE
        title = "Resumo do Sistema"
        navView.setCheckedItem(R.id.nav_home)
    }

    private fun setupSummary() {
        containerInfo.removeAllViews()
        addInfoRow("Modelo", Build.MODEL)
        addInfoRow("Marca", Build.BRAND)
        addInfoRow("Android", Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")")
        addInfoRow("Sales Code (CSC)", getSystemProperty("ro.csc.sales_code").ifEmpty { getSystemProperty("ro.boot.sales_code") })
        addInfoRow("Build", Build.DISPLAY)
    }

    private fun addInfoRow(label: String, value: String) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 12, 0, 12)
        }
        val tvLabel = TextView(this).apply {
            text = label.uppercase()
            setTypeface(null, Typeface.BOLD)
            textSize = 12f
            setTextColor(getColor(R.color.text_medium_emphasis))
        }
        val tvValue = TextView(this).apply {
            text = value
            textSize = 16f
            setTextColor(getColor(R.color.text_high_emphasis))
            setPadding(0, 4, 0, 0)
        }
        row.addView(tvLabel)
        row.addView(tvValue)
        containerInfo.addView(row)
        
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
            setBackgroundColor(Color.parseColor("#EEEEEE"))
        }
        containerInfo.addView(divider)
    }

    private fun setupTestButtons() {
        findViewById<View>(R.id.card_run_nfc).setOnClickListener { runTest("NfcTest") }
        findViewById<View>(R.id.card_run_regulatory).setOnClickListener { runTest("RegulatoryInfoTest") }
        findViewById<View>(R.id.card_run_network).setOnClickListener { runTest("MobileNetworkIconTest") }
    }

    private fun runTest(className: String) {
        Toast.makeText(this, "Comando enviado: $className\nUse o script run_test.sh para acompanhar.", Toast.LENGTH_LONG).show()
        Thread {
            try {
                Runtime.getRuntime().exec("am instrument -w -e class com.samsung.requirements.automatechecklisttest.tests.$className com.samsung.requirements.automatechecklisttest.test/androidx.test.runner.AndroidJUnitRunner")
            } catch (e: Exception) { e.printStackTrace() }
        }.start()
    }

    private fun loadInstalledPackages() {
        val pm = packageManager
        val packages: List<PackageInfo> = pm.getInstalledPackages(PackageManager.GET_META_DATA)
        val appList = packages.mapNotNull { pi ->
            pi.applicationInfo?.let { AppInfo(it.loadLabel(pm).toString(), pi.packageName, it.loadIcon(pm), "v${pi.versionName ?: "N/A"}") }
        }.sortedBy { it.name.lowercase() }
        packageList.adapter = AppAdapter(appList)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        layoutHome.visibility = View.GONE
        layoutTests.visibility = View.GONE
        packageList.visibility = View.GONE

        when (item.itemId) {
            R.id.nav_home -> {
                showHome()
            }
            R.id.nav_tests -> {
                title = "Executar Testes"
                layoutTests.visibility = View.VISIBLE
            }
            R.id.nav_installed_packages -> {
                title = "Pacotes Instalados"
                packageList.visibility = View.VISIBLE
            }
            R.id.nav_csc_feature -> openCscViewer("cscfeature.xml")
            R.id.nav_customer_carrier_feature -> openCscViewer("customer_carrier_feature.json")
            R.id.nav_customer -> openCscViewer("customer.xml")
        }
        drawerLayout.closeDrawers()
        return true
    }

    private fun openCscViewer(type: String) {
        val intent = Intent(this, CscViewerActivity::class.java)
        intent.putExtra("FILE_TYPE", type)
        startActivity(intent)
    }

    private fun getSystemProperty(propName: String): String {
        return try {
            val process = Runtime.getRuntime().exec("getprop $propName")
            BufferedReader(InputStreamReader(process.getInputStream())).use { it.readLine() ?: "" }
        } catch (e: Exception) { "" }
    }

    private inner class AppAdapter(private val list: List<AppInfo>) : BaseAdapter() {
        override fun getCount(): Int = list.size
        override fun getItem(position: Int): Any = list[position]
        override fun getItemId(position: Int): Long = position.toLong()
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val view = convertView ?: LayoutInflater.from(this@MainActivity).inflate(R.layout.item_package, parent, false)
            val app = list[position]
            view.findViewById<ImageView>(R.id.package_icon).setImageDrawable(app.icon)
            view.findViewById<TextView>(R.id.app_name).text = app.name
            view.findViewById<TextView>(R.id.package_name).text = app.packageName
            view.findViewById<TextView>(R.id.version_info).text = app.version
            return view
        }
    }
}
