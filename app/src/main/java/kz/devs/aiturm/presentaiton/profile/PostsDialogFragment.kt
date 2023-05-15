package kz.devs.aiturm.presentaiton.profile

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.shroomies.R
import com.google.android.gms.tasks.Task
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import kz.devs.aiturm.*

class PostsDialogFragment : DialogFragment() {

    companion object{
        private const val APARTMENT_PER_PAGINATION = 10
    }

    private var cancelButton: ShapeableImageView? = null
    private var postsTabLayout: TabLayout? = null
    private var recyclerView: RecyclerView? = null
    private var apartmentAdapter: RecycleViewAdapterApartments? = null
    private var personalPostRecyclerAdapter: PersonalPostRecyclerAdapter? = null
    private var progressBar: ProgressBar? = null

    private val apartmentPostList = ArrayList<Apartment>()
    private val personalPostList = ArrayList<PersonalPostModel>()
    private var firebaseFirestore: FirebaseFirestore? = null

    private var userId: String? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        firebaseFirestore = FirebaseFirestore.getInstance()
        userId = arguments?.getString("USERID")
        return inflater.inflate(R.layout.fragment_posts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cancelButton = view.findViewById(R.id.cancel_button)
        postsTabLayout = view.findViewById(R.id.posts_tab_layout)
        recyclerView = view.findViewById(R.id.recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        setupRecyclerView()
        setupCancelButton()
        setupTabLayout()
        if (userId != null) setupApartmentPosts(userId!!)

    }

    override fun onStart() {
        super.onStart()
        if (dialog != null) {
            dialog!!.window!!
                .setLayout(ActionBar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.MATCH_PARENT)
            dialog!!.window!!.setBackgroundDrawableResource(R.drawable.create_group_fragment_background)
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
        }
    }

    private fun setupCancelButton() {
        cancelButton?.setOnClickListener {
            dismiss()
        }
    }

    private fun setupRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.layoutManager = layoutManager
    }

    private fun setupTabLayout(){
        postsTabLayout?.addOnTabSelectedListener(object: OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab?.position == 1) {
                    if (userId != null) setupPersonalPosts(userId!!)
                } else if (tab?.position == 0) {
                    if (userId != null) setupApartmentPosts(userId!!)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }

        })
    }

    private fun setupApartmentPosts(userId: String) {
        displayProgress()
        apartmentAdapter = RecycleViewAdapterApartments(apartmentPostList, context, false, true)
        recyclerView?.adapter = apartmentAdapter
        val query = firebaseFirestore?.collection(Config.APARTMENT_POST)?.orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING)?.whereEqualTo(Config.userID, userId)?.limit(APARTMENT_PER_PAGINATION.toLong())
        query?.get()?.addOnCompleteListener { task: Task<QuerySnapshot> ->
            apartmentPostList.clear()
            for (document in task.result) {
                val apartmentPosts = document.toObject(Apartment::class.java)
                apartmentPosts.apartmentID = document.id
                apartmentPostList.add(apartmentPosts)
            }
            apartmentAdapter?.notifyDataSetChanged()
            removeProgress()
        }?.addOnFailureListener { e: Exception? -> removeProgress()}
    }

    private fun setupPersonalPosts(userId: String) {
        displayProgress()
        personalPostRecyclerAdapter = PersonalPostRecyclerAdapter(
            personalPostList,
            activity, false, true
        )
        recyclerView?.adapter = personalPostRecyclerAdapter
        val query = firebaseFirestore?.collection(Config.PERSONAL_POST)?.orderBy(Config.TIME_STAMP, Query.Direction.DESCENDING)?.whereEqualTo(Config.userID, userId)?.limit(UserProfileFragment.APARTMENT_PER_PAGINATION.toLong())
        query?.get()?.addOnCompleteListener { task: Task<QuerySnapshot> ->
            personalPostList.clear()
            for (document in task.result) {
                val personalPosts =
                    document.toObject(PersonalPostModel::class.java)
                personalPosts.postID = document.id
                personalPostList.add(personalPosts)
            }
            personalPostRecyclerAdapter?.notifyDataSetChanged()
            removeProgress()
        }?.addOnFailureListener { e: java.lang.Exception? ->  removeProgress()}
    }

    private fun displayProgress(){
        progressBar?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
    }

    private fun removeProgress(){
        progressBar?.visibility = View.GONE
        recyclerView?.visibility = View.VISIBLE
    }
}