package coffeecode.co.daftarfilm.features.main.home


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper

import coffeecode.co.daftarfilm.R
import coffeecode.co.daftarfilm.adapter.AdapterNowPlaying
import coffeecode.co.daftarfilm.adapter.AdapterKindOfMovies
import coffeecode.co.daftarfilm.database.MovieHelper
import coffeecode.co.daftarfilm.datasource.DataSource
import coffeecode.co.daftarfilm.features.detail.MovieDetailActivity
import coffeecode.co.daftarfilm.viewmodel.MovieViewModel
import coffeecode.co.daftarfilm.model.kindofmovies.KindOfMovies
import coffeecode.co.daftarfilm.model.movie.MovieResponse
import kotlinx.android.synthetic.main.fragment_home.*
import org.jetbrains.anko.support.v4.startActivity

class HomeFragment : Fragment() {

    private lateinit var adapterNowPlaying: AdapterNowPlaying
    private lateinit var adapterKindOfMovies: AdapterKindOfMovies

    private val snapHelper = PagerSnapHelper()
    private var movieViewModel: MovieViewModel? = null
    private lateinit var movieHelper: MovieHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initDatabase()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        initSwipeRefreshHome()
        getAllDataHomeViewModel()
    }

    private fun initDatabase() {
        movieHelper = MovieHelper.getInstance(activity!!)
        movieHelper.open()
    }

    override fun onResume() {
        super.onResume()
        initDatabase()
    }

    override fun onDetach() {
        super.onDetach()
        movieHelper.close()
    }

    private fun showLoading(){
        swipeRefreshHome.isRefreshing = true
    }

    private fun hideLoading(){
        swipeRefreshHome.isRefreshing = false
    }

    private fun getAllDataHomeViewModel() {
        movieViewModel = ViewModelProviders.of(activity!!).get(MovieViewModel::class.java).apply {
            getIsLoading()?.observe(this@HomeFragment, Observer {isLoading ->
                if (isLoading){
                    showLoading()
                }else{
                    hideLoading()
                }
            })

            getDataNowPlaying()?.observe(this@HomeFragment, Observer {movieResponse ->
                setAdapterNowPlaying(movieResponse)
            })

            getDataKindOfMoviesForHome()?.observe(this@HomeFragment, Observer {
                if (it != null){
                    setAdapterKindOfMovies(it)
                }
            })
        }
    }

    private fun initSwipeRefreshHome() {
        swipeRefreshHome.setOnRefreshListener {
            getAllDataHomeViewModel()
            swipeRefreshHome.isRefreshing = false
        }
    }

    private fun setAdapterKindOfMovies(listKindOfMovies: List<KindOfMovies>) {
        adapterKindOfMovies = AdapterKindOfMovies(activity!!, movieHelper)
        adapterKindOfMovies.setData(listKindOfMovies)

        rvKindOfMovies.layoutManager = LinearLayoutManager(activity)
        rvKindOfMovies.adapter = adapterKindOfMovies
    }

    private fun setAdapterNowPlaying(movieResponse: MovieResponse) {
        adapterNowPlaying = AdapterNowPlaying(activity!!, movieResponse){
            startActivity<MovieDetailActivity>(
                MovieDetailActivity.KEY_MOVIE_RESPONSE to it,
                MovieDetailActivity.KEY_IS_MOVIE to true)
        }
        adapterNowPlaying.notifyDataSetChanged()

        rvNowPlaying.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false)
        rvNowPlaying.adapter = adapterNowPlaying
        snapHelper.attachToRecyclerView(rvNowPlaying)
        indicatorNowPlaying.attachTo(rvNowPlaying)
    }
}
