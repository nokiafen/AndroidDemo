package stan.androiddemo.project.Mito

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.support.graphics.drawable.VectorDrawableCompat
import android.support.v4.app.Fragment
import android.support.v4.graphics.drawable.DrawableCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.StaggeredGridLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.BaseViewHolder
import com.facebook.drawee.view.SimpleDraweeView
import kotlinx.android.synthetic.main.fragment_image.*
import stan.androiddemo.Model.ResultInfo
import stan.androiddemo.R
import stan.androiddemo.project.Mito.Model.ImageSetInfo
import stan.androiddemo.project.Mito.Model.Resolution
import stan.androiddemo.tool.ImageLoad.ImageLoadBuilder

class ImageFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    override fun onRefresh() {
        index = 0
        loadData()
    }


    var cat = "全部"
    var arrImageSet = ArrayList<ImageSetInfo>()
    lateinit var mAdapter:BaseQuickAdapter<ImageSetInfo,BaseViewHolder>
    lateinit var failView: View
    lateinit var loadingView: View
    var index = 0
    var ratio:Float = 1F
    var imageCat = 0
    lateinit var progressLoading: Drawable
    companion object {
        fun createFragment():ImageFragment{
            val fg = ImageFragment()
            return fg
        }
    }



    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_image, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        cat =  arguments.getString("cat")
        val d0 = VectorDrawableCompat.create(resources,R.drawable.ic_toys_black_24dp,null)
        progressLoading = DrawableCompat.wrap(d0!!.mutate())
        DrawableCompat.setTint(progressLoading,resources.getColor(R.color.tint_list_pink))
        mAdapter = object:BaseQuickAdapter<ImageSetInfo,BaseViewHolder>(R.layout.image_set_item,arrImageSet){
            override fun convert(helper: BaseViewHolder, item: ImageSetInfo) {
                val img = helper.getView<SimpleDraweeView>(R.id.img_set)
                ratio = item.resolution.pixelX.toFloat() / item.resolution.pixelY.toFloat()
                img.aspectRatio = ratio
                ImageLoadBuilder.Start(this@ImageFragment.context,img,item.mainImage).setProgressBarImage(progressLoading).build()
                helper.setText(R.id.txt_image_title,item.title)
                helper.setText(R.id.txt_image_tag,item.category)
                helper.setText(R.id.txt_image_resolution,item.resolutionStr)
                helper.setText(R.id.txt_image_theme,item.theme)
            }
        }
        swipe_refresh_mito.setColorSchemeResources(R.color.colorPrimary)
        swipe_refresh_mito.setOnRefreshListener(this)

        recycler_images.layoutManager = StaggeredGridLayoutManager(2,StaggeredGridLayoutManager.VERTICAL)
        recycler_images.adapter = mAdapter
        loadingView = View.inflate(this@ImageFragment.context,R.layout.list_loading_hint,null)
        failView = View.inflate(this@ImageFragment.context,R.layout.list_empty_hint,null)
        failView.setOnClickListener {
            mAdapter.emptyView = loadingView
            index = 0
            loadData()
        }
        mAdapter.emptyView = loadingView
        mAdapter.setEnableLoadMore(true)
        mAdapter.setOnLoadMoreListener({
            loadData()
        },recycler_images)

        mAdapter.setOnItemClickListener { adapter, view, position ->
            val set = arrImageSet[position]
            val intent = Intent(this@ImageFragment.context,ImageSetActivity::class.java)
            intent.putExtra("set",set)
            startActivity(intent)
        }

        loadData()
    }


    override fun onAttach(context: Context?) {
        super.onAttach(context)
        println("onAttach" + cat)
    }

    override fun onAttachFragment(childFragment: Fragment?) {
        super.onAttachFragment(childFragment)
        println("onAttachFragment" + cat)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate" + cat)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        println("onActivityCreated" + cat)
    }

    override fun onStart() {
        super.onStart()
        println("onStart" + cat)
        if (arrImageSet.size > 0){
            if (arrImageSet.first().imgBelongCat != imageCat){
                swipe_refresh_mito.isRefreshing = true
                onRefresh()
            }
        }
    }

    fun refreshCat(cat:Int){
        if (cat == imageCat){
            return
        }
        imageCat = cat
        if (swipe_refresh_mito != null){
            swipe_refresh_mito.isRefreshing = true
            onRefresh()
        }

    }

    fun loadData(){
        ImageSetInfo.imageSets(imageCat, cat,Resolution(),"全部",index,{ v: ResultInfo ->
            activity.runOnUiThread {
                if (swipe_refresh_mito != null){
                    swipe_refresh_mito.isRefreshing = false
                }

                if (v.code != 0) {
                    Toast.makeText(this@ImageFragment.context,v.message, Toast.LENGTH_LONG).show()
                    mAdapter.emptyView = failView
                    return@runOnUiThread
                }
                val imageSets = v.data!! as ArrayList<ImageSetInfo>
                if (imageSets.size <= 0){
                    if (index == 0){
                        mAdapter.emptyView = failView
                        return@runOnUiThread
                    }
                    else{
                        mAdapter.loadMoreEnd()
                    }
                }
                if (index == 0){
                    arrImageSet.clear()
                }
                else{
                    mAdapter.loadMoreComplete()
                }
                index ++
                arrImageSet.addAll(imageSets.map {
                    it.imgBelongCat = imageCat
                    it
                })
                mAdapter.notifyDataSetChanged()
            }
        })
    }


}
