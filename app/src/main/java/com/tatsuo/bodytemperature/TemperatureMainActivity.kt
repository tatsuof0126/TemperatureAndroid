package com.tatsuo.bodytemperature

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.gms.ads.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.tatsuo.bodytemperature.db.Person
import com.tatsuo.bodytemperature.db.Temperature
import com.tatsuo.bodytemperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.activity_temperature_main.*
import java.util.*
import java.util.concurrent.Executors
import kotlin.random.Random

class TemperatureMainActivity : AppCompatActivity(), TemperatureListFragment.OnListFragmentInteractionListener, GraphFragment.OnFragmentInteractionListener, PeopleFragment.OnFragmentInteractionListener, ConfigFragment.OnFragmentInteractionListener {

    private lateinit var mAdView: AdView
    private lateinit var mInterstitialAd: InterstitialAd
    private val bannerAdRequest = AdRequest.Builder().addTestDevice("276EB5AF5B07A5B142631A70BEE353EE").addTestDevice("08F7DFA353E42A1A2C190B38622A90BD").build()
    private val interstitialAdRequest = AdRequest.Builder().addTestDevice("276EB5AF5B07A5B142631A70BEE353EE").addTestDevice("08F7DFA353E42A1A2C190B38622A90BD").build()
    private val INTERSTITIAL_RATE = 25

    // HUAWEI：276EB5AF5B07A5B142631A70BEE353EE、ASUS：08F7DFA353E42A1A2C190B38622A90BD
    // val bannerAdRequest = AdRequest.Builder().build()
    // val interstitialAdRequest = AdRequest.Builder()..build()

    var targetDate: Date = Date()

    private val mOnNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_temperature_list -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, TemperatureListFragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_graph -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, GraphFragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_people -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, PeopleFragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_config -> {
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ConfigFragment())
                        .commit()
                return@OnNavigationItemSelectedListener true
            }
        }
        false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temperature_main)

        val targetPersonId = ConfigManager().loadTargetPersonId()
        if(targetPersonId == 0){
            // 初期化処理
            val personId = 1
            val personName = "あなた"

            ConfigManager().saveTargetPersonId(personId)
            ConfigManager().saveTargetPersonName(personName)

            val database = Room.databaseBuilder(this.applicationContext, objectOf<TemperatureDatabase>(), "temperature_database.db").build()
            val dao = database.personDao()

            val myExecutor = Executors.newSingleThreadExecutor()
            myExecutor.execute() {
                val personList = dao.loadPersonById(1)
                if(personList.size == 0){
                    var person = Person()
                    person.personId = personId
                    person.name = personName
                    dao.insert(person)
                }
            }
        }

        // AdMobの設定
        if (ConfigManager().loadShowAdsFlag()) {
            // アプリID（本番）：ca-app-pub-6719193336347757~4001892480
            // アプリID（テスト）: ca-app-pub-3940256099942544~3347511713
            MobileAds.initialize(this, "ca-app-pub-6719193336347757~4001892480")

            mAdView = findViewById(R.id.adView)

            // 本番：ca-app-pub-6719193336347757/6704733987
            // テスト：ca-app-pub-3940256099942544/6300978111
            mAdView.loadAd(bannerAdRequest)
            mAdView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    mAdView.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    Log.e("***", "AdView onAdFailedToLoad : "+p0)
                }
            }

            // 本番：ca-app-pub-6719193336347757/3203816136
            // テスト：ca-app-pub-3940256099942544/1033173712
            mInterstitialAd = InterstitialAd(this)
            mInterstitialAd.adUnitId = "ca-app-pub-6719193336347757/3203816136"
            mInterstitialAd.loadAd(interstitialAdRequest)
            mInterstitialAd.adListener = object : AdListener() {
                override fun onAdClosed() {
                    super.onAdClosed()
                    Log.e("***", "InterstitialAd onAdClosed")
                    mInterstitialAd.loadAd(interstitialAdRequest)
                }

                override fun onAdLeftApplication() {
                    super.onAdLeftApplication()
                    Log.e("***", "InterstitialAd onAdLeftApplication")
                }

                override fun onAdFailedToLoad(p0: Int) {
                    super.onAdFailedToLoad(p0)
                    Log.e("***", "InterstitialAd onAdFailedToLoad : "+p0)
                }
            }
        }

        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener)

        supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, TemperatureListFragment())
                .commit()

    }

    override fun onResume() {
        super.onResume()

        // 保存からの戻りの場合はインタースティシャル広告を表示
        if (ConfigManager().loadUpdatedDataFlag() && ConfigManager().loadShowAdsFlag()) {
            if (mInterstitialAd.isLoaded && Random.nextInt(100) <= INTERSTITIAL_RATE) {
                mInterstitialAd.show()
            }

            if (mInterstitialAd.isLoaded == false){
                mInterstitialAd.loadAd(interstitialAdRequest)
            }

        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Log.e("***Temperature***", "Activity : onOptionsItemSelected")

        return when (item.itemId) {
            R.id.addTemperature -> {
                val intent = Intent(this, InputTemperatureActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.graphdays3 -> {
                ConfigManager().saveGraphType(1)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, GraphFragment())
                        .commit()
                true
            }
            R.id.graphdays7 -> {
                ConfigManager().saveGraphType(2)
                supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, GraphFragment())
                        .commit()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    override fun onListFragmentInteraction(temperature: Temperature?) {
        // Log.e("***Temperature***", "" + temperature?.getTemperatureString())

        val intent = Intent(this, InputTemperatureActivity::class.java)
        intent.putExtra("TEMPERATURE_ID", temperature?.id)
        startActivity(intent)
    }

    override fun onFragmentInteraction(uri: Uri) {


    }

    fun removeAds(){
        mAdView.visibility = View.GONE
    }

}
