package com.nasa.nasaapod.ui

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.widget.Toast
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.snackbar.Snackbar
import com.nasa.nasaapod.ApodApp
import com.nasa.nasaapod.R
import com.nasa.nasaapod.data.Result
import com.nasa.nasaapod.databinding.FragmentApodBinding
import com.nasa.nasaapod.db.NasaApodEntity
import com.nasa.nasaapod.utils.Utils
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class NasaApodFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    companion object {
        fun newInstance() = NasaApodFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        ApodApp.instance.getApodAppComponent().inject(this)
        val nasaapodViewModel: NasaApodViewModel by viewModels { viewModelFactory }

        val binding = DataBindingUtil.inflate<FragmentApodBinding>(
            inflater, R.layout.fragment_apod, container, false
        )
        subscribeUi(binding, nasaapodViewModel)
        return binding.root
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        nasaapodViewModel.nasaApodPicture.observe(viewLifecycleOwner, Observer {
//            bindView(it)
//        })
//    }

    private fun subscribeUi(binding: FragmentApodBinding, nasaapodViewModel: NasaApodViewModel) {
        nasaapodViewModel.nasaApod.observe(viewLifecycleOwner, Observer { result ->
            when (result.status) {
                Result.Status.SUCCESS -> {
                    binding.progressBar.hide()
                    result.data?.let { bindView(binding, it) }
                }
                Result.Status.LOADING -> binding.progressBar.show()
                Result.Status.ERROR -> {
                    binding.progressBar.hide()
                    Snackbar.make(binding.titleTv, result.message!!, Snackbar.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun bindView(binding: FragmentApodBinding, nasaApodEntityList: List<NasaApodEntity>) {
        if (!nasaApodEntityList.isEmpty()) {
            Timber.e("List is %s", nasaApodEntityList)
            Timber.e("Size is %d", nasaApodEntityList.size)
            val nasaApodEntity = nasaApodEntityList[0];

            //nasaApodEntityList contains data from all previous days as well thats why we are selecting the data at 0th position

            Timber.i("Date is %s", Utils.convertDateFormat(nasaApodEntity.date))

            if (nasaApodEntity.mediaType.equals("video")) {

                binding.apodImageview.visibility = View.INVISIBLE;
                binding.apodVideoview.visibility = View.VISIBLE;
                nasaApodEntity.apply {


                    if (!nasaApodEntity.url.isNullOrEmpty()) {
                        /*  val mediaController = MediaController(context)

                          // on below line we are creating
                          // uri for our video view.
                          val uri: Uri = Uri.parse(nasaApodEntity.url)

                          // on below line we are setting
                          // video uri for our video view.
                          binding.apodVideoview.setVideoURI(uri)

                          mediaController.setAnchorView(binding.apodVideoview)
                          // set media controller object for a video view
                          binding.apodVideoview.setMediaController(mediaController)*/


                        /*    val dataUrl = "<html>" +
                                    "<body>" +
                                    "<h2>Video From YouTube</h2>" +
                                    "<br>" +
                                    "<iframe width=\"560\" height=\"315\" src=\"" + myYouTubeVideoUrl + "\" frameborder=\"0\" allowfullscreen/>" +
                                    "</body>" +
                                    "</html>"

                            val myWebView = findViewById<WebView>(R.id.mWebView)
    */
                        val webSettings = binding.apodVideoview.settings

                        webSettings.javaScriptEnabled = true
                        binding.apodVideoview.settings.layoutAlgorithm =
                            WebSettings.LayoutAlgorithm.SINGLE_COLUMN
                        binding.apodVideoview.settings.loadWithOverviewMode = true
                        binding.apodVideoview.settings.useWideViewPort = true
                        binding.apodVideoview.loadUrl(nasaApodEntity.url)


                    }

                    // Create a formatted string with HTML for making "Title" bold
                    var formattedTitle = "<b>Title : </b>${nasaApodEntity.title}"

                    // Convert the HTML string to a Spanned object
                    var spannedText: Spanned =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.titleTv.text = spannedText


                    formattedTitle = "<b>Details : </b>${nasaApodEntity.explanation}"
                    // Convert the HTML string to a Spanned object
                    spannedText =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.explainTv.text = spannedText

                    var date1 = convertDateFormat(nasaApodEntity.date.toString())
                    formattedTitle = "<b>Date : </b>${date1}"
                    // Convert the HTML string to a Spanned object
                    spannedText =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.dateTv.text = spannedText
                }

                binding.apodVideoview.post(Runnable {  setConstraintLayoutProperties(binding.apodImageview,0,30F,0,0,0,0) })


            } else {
                binding.apodVideoview.visibility = View.INVISIBLE;
                binding.apodImageview.visibility = View.VISIBLE;
                nasaApodEntity.apply {


                    if (!nasaApodEntity.url.isNullOrEmpty()) {
                        Glide.with(binding.apodImageview.context)
                            .load(nasaApodEntity.url)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(binding.apodImageview)

                    }

                    // Create a formatted string with HTML for making "Title" bold
                    var formattedTitle = "<b>Title : </b>${nasaApodEntity.title}"

                    // Convert the HTML string to a Spanned object
                    var spannedText: Spanned =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.titleTv.text = spannedText


                    formattedTitle = "<b>Details : </b>${nasaApodEntity.explanation}"
                    // Convert the HTML string to a Spanned object
                    spannedText =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.explainTv.text = spannedText

                    var date1 = convertDateFormat(nasaApodEntity.date.toString())
                    formattedTitle = "<b>Date : </b>${date1}"
                    // Convert the HTML string to a Spanned object
                    spannedText =
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            Html.fromHtml(formattedTitle, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            HtmlCompat.fromHtml(formattedTitle, HtmlCompat.FROM_HTML_MODE_LEGACY)
                        }
                    binding.dateTv.text = spannedText
                }

                binding.apodImageview.post(Runnable {  setConstraintLayoutProperties(binding.apodImageview,0,30F,0,0,0,0) })


            }

            Timber.i("Current Date is %s", Utils.getCurrentDate(Calendar.getInstance()))
            Timber.e(
                "Date compare %s",
                Utils.convertDateFormat(nasaApodEntity.date) == (Utils.getCurrentDate(Calendar.getInstance()))
            )
            if (!Utils.convertStringToDate(Utils.convertDateFormat(nasaApodEntity.date))
                    .equals(Utils.convertStringToDate(Utils.getCurrentDate(Calendar.getInstance()))) && !Utils.isNetworkAvailable(
                    binding.titleTv.context
                )
            ) {
                Toast.makeText(
                    binding.titleTv.context,
                    getString(R.string.last_cache_image),
                    Toast.LENGTH_LONG
                ).show()
            }
        } else if (!Utils.isNetworkAvailable(binding.titleTv.context)) {
            Toast.makeText(
                binding.titleTv.context,
                getString(R.string.no_internet),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun convertDateFormat(inputDate: String): String {
        try {
            val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss 'GMT'Z yyyy", Locale.US)
            val outputFormat = SimpleDateFormat("EEE, MMM dd yyyy", Locale.US)

            val date = inputFormat.parse(inputDate)
            return outputFormat.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return inputDate // Return the original input if an error occurs
    }

    fun setConstraintLayoutProperties(
        view: View?,
        BackgroundColorOfTheView: Int,
        CornerRadiusOfTheView: Float,
        Shape: Int,
        strokeWidth: Int,
        i_top_or_bottom: Int,
        vararg StrokeColor: Int,
    ) {
        try {
            if (view != null && Shape == 0) {
                val gradientDrawable = GradientDrawable()
                // i_top_or_bottom ==1 means show corner on the top only
                if (i_top_or_bottom == 0) {
                    gradientDrawable.cornerRadius = CornerRadiusOfTheView
                } else if (i_top_or_bottom == 1) {
                    gradientDrawable.cornerRadii = floatArrayOf(
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView,
                        0f,
                        0f,
                        0f,
                        0f
                    )
                } else if (i_top_or_bottom == 2) {
                    gradientDrawable.cornerRadii = floatArrayOf(
                        0f,
                        0f,
                        0f,
                        0f,
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView,
                        CornerRadiusOfTheView
                    )
                }
                for (Color in StrokeColor) {
                    gradientDrawable.setStroke(strokeWidth, Color)
                }
                gradientDrawable.setColor(BackgroundColorOfTheView)
                //                view.setBackgroundDrawable(new RippleDrawable(Objects.requireNonNull(getPressedColorSelector(BackgroundColorOfTheView, context.getResources().getColor(R.color.ripple_color))), gradientDrawable, null));
                view.setBackgroundDrawable(gradientDrawable)
            } else if (view != null) {
                val gradientDrawable = GradientDrawable()
                // SHape == 1 means Oval Shape
                if (Shape == 1) {
                    gradientDrawable.shape = GradientDrawable.OVAL
                }
                for (Color in StrokeColor) {
                    gradientDrawable.setStroke(strokeWidth, Color)
                }
                gradientDrawable.setColor(BackgroundColorOfTheView)
                view.background = RippleDrawable(
                    Objects.requireNonNull<ColorStateList>(
                        getPressedColorSelector(
                            BackgroundColorOfTheView,
                            context!!.resources.getColor(R.color.teal_700)
                        )
                    ), gradientDrawable, null
                )
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun getPressedColorSelector(normalColor: Int, pressedColor: Int): ColorStateList? {
        return try {
            ColorStateList(
                arrayOf(
                    intArrayOf(android.R.attr.state_pressed),
                    intArrayOf(android.R.attr.state_focused),
                    intArrayOf(android.R.attr.state_activated),
                    intArrayOf()
                ), intArrayOf(
                    pressedColor,
                    pressedColor,
                    pressedColor,
                    normalColor
                )
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }
    }


}
