package com.tatsuo.bodytemperature

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.room.Room
import com.tatsuo.bodytemperature.db.Person
import com.tatsuo.bodytemperature.db.TemperatureDatabase
import kotlinx.android.synthetic.main.dialog_editperson.view.*
import kotlinx.android.synthetic.main.fragment_people.*
import java.util.concurrent.Executors

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Activities that contain this fragment must implement the
 * [PeopleFragment.OnFragmentInteractionListener] interface
 * to handle interaction events.
 * Use the [PeopleFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class PeopleFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        database = Room.databaseBuilder(requireActivity().applicationContext, TemperatureDatabase::class.java, "temperature_database.db").build()

        requireActivity().setTitle(getString(R.string.title_person))

        updateViewAdapter()
    }

    fun updateViewAdapter() {
        val dao = database.personDao()
        val handler = Handler()
        val myExecutor = Executors.newSingleThreadExecutor()
        myExecutor.execute() {
            var personList = dao.getAllPerson()
            handler.post(Runnable() {
                personListView.adapter = PersonAdapter(personList)
            })
        }
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.menu_people, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.addPerson -> {
                val dialog = AddPersonDialogFragment()
                dialog.show(requireActivity().supportFragmentManager, "addperson")
                true
            }
            R.id.editPerson -> {
                val dialog = EditPersonDialogFragment()
                dialog.show(requireActivity().supportFragmentManager, "editperson")
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
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
         * @return A new instance of fragment PeopleFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
                PeopleFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, param1)
                        putString(ARG_PARAM2, param2)
                    }
                }

        lateinit var database: TemperatureDatabase
    }

    class AddPersonDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            val inflater = activity!!.layoutInflater
            val editPersonView = inflater.inflate(R.layout.dialog_editperson, null)

            builder.setView(editPersonView)
                    .setTitle(getString(R.string.add_person))
                    .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                        val personId = ConfigManager.loadTargetPersonId()
                        val personName = editPersonView.nameText.text.toString().trim()

                        Log.e("***Temperature***", "PersonName : [" + personName + "]")
                        if (personName != "") {
                            val dao = database.personDao()
                            val myExecutor = Executors.newSingleThreadExecutor()
                            myExecutor.execute() {
                                val person = Person()
                                person.name = personName
                                dao.insert(person)

                                requireActivity().supportFragmentManager.beginTransaction()
                                        .replace(R.id.frameLayout, PeopleFragment())
                                        .commit()
                            }
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, id ->

                    }

            editPersonView.requestFocus()

            return builder.create()
        }
    }

    class EditPersonDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())
            val inflater = activity!!.layoutInflater
            val editPersonView = inflater.inflate(R.layout.dialog_editperson, null)
            var deletable = false

            builder.setView(editPersonView)
                    .setTitle(getString(R.string.edit_name))
                    .setPositiveButton(getString(R.string.ok)) { dialog, id ->
                        val personId = ConfigManager.loadTargetPersonId()
                        val personName = editPersonView.nameText.text.toString().trim()
                        if (personName != "") {
                            val dao = database.personDao()
                            val myExecutor = Executors.newSingleThreadExecutor()
                            myExecutor.execute() {
                                val person = dao.loadPersonById(personId)
                                if (person.size >= 1) {
                                    person[0].name = personName
                                    dao.update(person[0])
                                    ConfigManager.saveTargetPersonName(personName)
                                }
                            }
                            requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.frameLayout, PeopleFragment())
                                    .commit()
                        }
                    }
                    .setNegativeButton(getString(R.string.cancel)) { dialog, id ->

                    }
                    .setNeutralButton(getString(R.string.delete)) { dialog, id ->
                        if (deletable) {
                            val dialog = DeletePersonConfirmDialogFragment()
                            dialog.show(requireActivity().supportFragmentManager, "deleteperson")
                        } else {
                            Toast.makeText(requireActivity(), getString(R.string.error_delete_person), Toast.LENGTH_SHORT).show()
                        }
                    }


            val dao = database.personDao()
            val myExecutor = Executors.newSingleThreadExecutor()
            myExecutor.execute() {
                val personList = dao.getAllPerson()
                if (personList.size >= 2) {
                    deletable = true
                }
            }

            editPersonView.nameText.setText(ConfigManager.loadTargetPersonName())
            editPersonView.requestFocus()

            return builder.create()
        }

    }

    class DeletePersonConfirmDialogFragment : DialogFragment() {
        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
            val builder = AlertDialog.Builder(requireActivity())

            val personId = ConfigManager.loadTargetPersonId()
            val personName = ConfigManager.loadTargetPersonName()

            builder.setTitle(getString(R.string.confirm))
                    .setMessage(getString(R.string.confirm_delete_person, personName))
                    .setPositiveButton(getString(R.string.ok), { dialog, id ->
                        val dao = database.personDao()
                        val temperatureDao = database.temperatureDao()
                        val myExecutor = Executors.newSingleThreadExecutor()
                        myExecutor.execute() {
                            val person = Person()
                            person.personId = personId
                            dao.delete(person)

                            val temperatureList = temperatureDao.getAllTemperatureData(personId)
                            for(temperature in temperatureList){
                                temperatureDao.delete(temperature)
                            }

                            val personList = dao.getAllPerson()
                            ConfigManager.saveTargetPersonId(personList[0].personId)
                            ConfigManager.saveTargetPersonName(personList[0].name)

                            requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.frameLayout, PeopleFragment())
                                    .commit()
                        }
                    })
                    .setNegativeButton(getString(R.string.cancel), { dialog, id ->

                    })

            return builder.create()
        }
    }



}
