package com.example.helloapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.net.Uri
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.samsung.requirements.automatechecklisttest.R
import java.io.BufferedReader
import java.io.File
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

    private var currentRow: LinearLayout? = null

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

        checkPermissions()
        setupSummary()
        setupTestButtons()
        loadInstalledPackages()
        
        showHome()
    }

    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        
        val listPermissionsNeeded = ArrayList<String>()
        for (p in permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(p)
            }
        }
        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), 100)
        }
    }

    override fun onRestart() {
        super.onRestart()
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
        currentRow = null
        
        addSectionTitle("Informações do Dispositivo")
        addInfoRow("Modelo", getSystemProperty("ro.product.model").ifEmpty { Build.MODEL }, true)
        addInfoRow("Marca", Build.BRAND, true)
        
        val serial = getSystemProperty("ro.serialno").ifEmpty { 
            getSystemProperty("ro.boot.serialno").ifEmpty { 
                getSystemProperty("ril.serialnumber").ifEmpty {
                    getSystemProperty("sys.serialnumber").ifEmpty {
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                            @Suppress("DEPRECATION")
                            Build.SERIAL 
                        } else {
                            try {
                                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                                    Build.getSerial()
                                } else "Sem Permissão (READ_PHONE_STATE)"
                            } catch (e: Exception) {
                                "Protegido pelo Android 10+"
                            }
                        }
                    }
                }
            }
        }
        addInfoRow("Número de Série", serial, false)
        
        addSectionTitle("Build e Sistema Operacional")
        addInfoRow("Android", Build.VERSION.RELEASE + " (" + Build.VERSION.SDK_INT + ")", true)
        addInfoRow("Build Type", getSystemProperty("ro.build.type").ifEmpty { Build.TYPE }, true)
        addInfoRow("Root Access", if (checkRoot()) "Sim (Rooted)" else "Não", true)
        addInfoRow("Build ID", Build.DISPLAY, false)
        addInfoRow("Kernel", System.getProperty("os.version") ?: "N/A", false)
        
        addSectionTitle("Customizações e Região (CSC)")
        val isSingleSku = getSystemProperty("mdc.singlesku").equals("true", ignoreCase = true)
        val isSkuActivated = getSystemProperty("mdc.singlesku.activated").equals("true", ignoreCase = true)

        addInfoRow("Single SKU", isSingleSku.toString(), true)
        addInfoRow("SKU Activated", isSkuActivated.toString(), true)
        
        forceNewLine()
        
        addInfoRow("SKU Type", getSystemProperty("mdc.singlesku.type"), true)
        addInfoRow("Country", getSystemProperty("ro.csc.country_code"), true)

        val salesCode = getSystemProperty("ro.csc.sales_code").ifEmpty { getSystemProperty("ro.boot.sales_code") }
        addInfoRow("Sales Code", salesCode, true)
        addInfoRow("Activated ID", getSystemProperty("ro.boot.activatedid"), true)

        if (isSingleSku && isSkuActivated) {
            addSectionTitle("TSS Client Identifiers")
            loadTssClientIds()
        }
        
        addSectionTitle("Caminhos de Configuração")
        addInfoRow("OMC Path", getSystemProperty("persist.sys.omc_path"), false)
        
        addSectionTitle("Google Client Identifiers")
        addInfoRow("Base ID", getSystemProperty("ro.com.google.clientidbase"), false)
        addInfoRow("PG2 ID", getSystemProperty("ro.com.google.clientidbase.pg2"), false)
        addInfoRow("TX ID", getSystemProperty("ro.com.google.clientidbase.tx"), false)
    }

    private fun checkRoot(): Boolean {
        val paths = arrayOf(
            "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su",
            "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
            "/system/bin/failsafe/su", "/data/local/su"
        )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    private fun forceNewLine() {
        currentRow = null
    }

    private fun loadTssClientIds() {
        try {
            val uri = Uri.parse("content://com.google.settings/partner")
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                while (it.moveToNext()) {
                    val nameColumn = it.getColumnIndex("name")
                    val valueColumn = it.getColumnIndex("value")
                    
                    if (nameColumn != -1 && valueColumn != -1) {
                        val name = it.getString(nameColumn)
                        val value = it.getString(valueColumn)
                        if (name.contains("client_id", ignoreCase = true)) {
                            addInfoRow(name.replace("client_id", "ID"), value, name.length < 15)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            addInfoRow("TSS Error", "Provider indisponível", false)
        }
    }

    private fun addSectionTitle(title: String) {
        currentRow = null 
        val tv = TextView(this).apply {
            text = title
            setTextColor(getColor(R.color.primary))
            setTypeface(null, Typeface.BOLD)
            textSize = 14f
            setPadding(0, 32, 0, 8)
        }
        containerInfo.addView(tv)
    }

    private fun addInfoRow(label: String, value: String, isShort: Boolean) {
        if (!isShort || currentRow == null) {
            currentRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                setPadding(0, 8, 0, 8)
            }
            containerInfo.addView(currentRow)
        }

        val itemLayout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            val weight = if (isShort) 1f else 2f
            layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, weight)
            setPadding(0, 0, 16, 0)
        }

        val tvLabel = TextView(this).apply {
            text = label.uppercase()
            setTypeface(null, Typeface.BOLD)
            textSize = 10f
            setTextColor(getColor(R.color.text_medium_emphasis))
        }
        val tvValue = TextView(this).apply {
            text = if (value.isNullOrEmpty()) "N/A" else value
            textSize = 14f
            setTextColor(getColor(R.color.text_high_emphasis))
            setPadding(0, 2, 0, 0)
        }

        itemLayout.addView(tvLabel)
        itemLayout.addView(tvValue)
        currentRow?.addView(itemLayout)

        if (!isShort) {
            currentRow = null 
            val divider = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1)
                setBackgroundColor(Color.parseColor("#F0F0F0"))
            }
            containerInfo.addView(divider)
        }
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
            R.id.nav_home -> showHome()
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
