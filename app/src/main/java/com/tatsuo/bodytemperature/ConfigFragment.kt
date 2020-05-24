package com.tatsuo.bodytemperature

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import kotlinx.android.synthetic.main.fragment_config.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [ConfigFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [ConfigFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ConfigFragment : Fragment(), PurchasesUpdatedListener, AcknowledgePurchaseResponseListener {

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    lateinit private var billingClient: BillingClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_config, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        requireActivity().setTitle(getString(R.string.title_config))

        if(ConfigManager.loadShowAdsFlag()){
            purchaseRemoveAdsButton.visibility = View.VISIBLE
        } else {
            purchaseRemoveAdsButton.visibility = View.GONE
        }

        fahrenheitCheck.isChecked = ConfigManager.loadUseFahrenheitFlag()
        fahrenheitCheck.setOnClickListener(View.OnClickListener {
            ConfigManager.saveUseFahrenheitFlag(fahrenheitCheck.isChecked)
        })

        versionText.text = getString(R.string.app_name)+" ver"+BuildConfig.VERSION_NAME

        // BillingClientを初期化
        billingClient = BillingClient.newBuilder(requireActivity()).enablePendingPurchases().setListener(this).build()
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                Log.e("***BillingClientを初期化***","Code : "+billingResult.responseCode)
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // The BillingClient is ready. You can query purchases here.
                    Log.e("***BillingClientを初期化***","SetUp成功")
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
                Log.e("***BillingClientを初期化***","onBillingServiceDisconnected")
            }
        })

        purchaseRemoveAdsButton.setOnClickListener {
            purchaseRemoveAds()
        }

        privacyPolicyButton.setOnClickListener {
            openPrivacyPolicy()
        }

    }

    private fun purchaseRemoveAds() {
        if(billingClient.isReady == false){
            Toast.makeText(activity, getString(R.string.error_connect_failed), Toast.LENGTH_SHORT).show()
            return
        }

        val itemType = BillingClient.SkuType.INAPP
        val skuList = listOf("remove_ads")
        val skuDetailsParams = SkuDetailsParams.newBuilder().setSkusList(skuList).setType(itemType).build()

        billingClient.querySkuDetailsAsync(skuDetailsParams, {
            responseCode, skuDetailsList: MutableList<SkuDetails> ->
            if(skuDetailsList.size >= 1) {
                val billingFlowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetailsList[0]).build()
                billingClient.launchBillingFlow(activity, billingFlowParams)
            } else {
                Toast.makeText(activity, getString(R.string.error_connect_failed)+" Code : "+responseCode, Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        Log.e("***onPurchasesUpdated", "billingResult code : "+billingResult.responseCode)

        if (purchases != null && billingResult.responseCode == BillingClient.BillingResponseCode.OK){
            // 購入成功
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED){
                    if(purchase.isAcknowledged){
                        // 購入が承認済みなら広告を削除
                        removeAds()
                    } else {
                        // 購入が未承認なら承認する
                        val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                                .setPurchaseToken(purchase.purchaseToken)
                                .build()
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams, this)
                    }
                }
            }
        } else if(billingResult.responseCode == BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED) {
            // すでに購入済みであれば広告を削除
            removeAds()
        } else if(billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED){
            return
        } else {
            Toast.makeText(activity, getString(R.string.error_purchase_failed)+" Code : "+billingResult.responseCode, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onAcknowledgePurchaseResponse(billingResult: BillingResult) {
        Log.e("***onAcknowledgePurcha", "billingResult code : "+billingResult.responseCode)

        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // 承認成功なら広告を削除
            removeAds()
        } else {
            Toast.makeText(activity, getString(R.string.error_purchase_failed)+" Code : "+billingResult.responseCode, Toast.LENGTH_SHORT).show()
        }
    }

    fun removeAds(){
        ConfigManager.saveShowAdsFlag(false)
        purchaseRemoveAdsButton.visibility = View.GONE
        (requireActivity() as TemperatureMainActivity).removeAds()
        Toast.makeText(activity, getString(R.string.message_remove_ads), Toast.LENGTH_SHORT).show()
    }

    // TODO: Rename method, update argument and hook method into UI event
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    private fun openPrivacyPolicy() {
        val uri = Uri.parse("https://tatsuof0126.s3-ap-northeast-1.amazonaws.com/temperature_android_privacy_policy.html")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        startActivity(intent)
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     *
     *
     * See the Android Training lesson [Communicating with Other Fragments]
     * (http://developer.android.com/training/basics/fragments/communicating.html)
     * for more information.
     */
    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ConfigFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                ConfigFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }
    }
}
