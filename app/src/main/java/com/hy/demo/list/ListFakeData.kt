package com.hy.demo.list

import com.hy.myapp.R
import kotlinx.coroutines.delay

/**
 * desc: 列表演示的假数据 + 模型
 *
 * 三种形态各用一个精简模型(字段够用即可,重点在框架用法不是建模);
 * 数据全部本地生成,竖向列表的"分页/刷新"通过 [ListFakeData] 模拟网络延迟,
 * 让 [androidx.lifecycle.ViewModel] 的 loading / pageState / SmartRefresh 收尾有真实的异步节奏。
 */

/** 竖向列表项(卡片样式:标题 + 摘要 + 标签 + 封面 + 作者/时间) */
data class FeedItem(
    val id: Int,
    val title: String,
    val summary: String,
    val tag: String,
    val author: String,
    val imageModel: Any,
)

/** 横向 / 网格列表项(图标 + 文字,同一模型两种排布) */
data class CellItem(
    val id: Int,
    val label: String,
    val imageModel: Any,
)

/** 瀑布流项(标题 + 一段长短不一的正文 + 主题色块,靠正文长度制造错落高度) */
data class CardItem(
    val id: Int,
    val title: String,
    val content: String,
    val colorArgb: Int,
)

/**
 * 本地假数据工厂。
 *
 * 图片模型混用"gstatic 网络图 + 本地 mipmap",既演示 ImageLoader 加载网络图,
 * 也保证断网时本地项仍可显示。[fetchPage] 模拟一次分页网络请求(带 [delay])。
 */
object ListFakeData {

    private val images = listOf(
        "https://www.gstatic.com/webp/gallery/1.jpg",
        "https://www.gstatic.com/webp/gallery/2.jpg",
        "https://www.gstatic.com/webp/gallery/3.jpg",
        "https://www.gstatic.com/webp/gallery/4.jpg",
        "https://www.gstatic.com/webp/gallery/5.jpg",
        R.mipmap.img_lab_view1,
        R.mipmap.img_lab_view2,
    )

    /** 瀑布流色块(取项目色值,保证有白/深自动前景判定的对照样本) */
    private val blockColors = intArrayOf(
        0xFF72ABF1.toInt(), 0xFFFF8A21.toInt(), 0xFF24899C.toInt(),
        0xFFFF4848.toInt(), 0xFF6B8E23.toInt(), 0xFF8A2BE2.toInt(),
        0xFF008B8B.toInt(), 0xFFC71585.toInt(),
    )

    /** 主题最多 4 页,超过即"没有更多",供上拉加载收尾演示 */
    const val TOTAL_PAGE = 4

    /** 竖向列表:第 [page] 页(每页 [pageSize] 条),模拟 [latencyMs] 毫秒网络延迟 */
    suspend fun feedPage(page: Int, pageSize: Int = 8, latencyMs: Long = 600): List<FeedItem> {
        delay(latencyMs)
        val base = (page - 1) * pageSize
        return (0 until pageSize).map { i ->
            val index = base + i + 1
            FeedItem(
                id = index,
                title = "第 $index 条 · 列表框架能力演示",
                summary = "BRVAH4 适配器 + ImageLoader 封面 + 点击事件,一行 submitList 增量刷新。",
                tag = TAGS[index % TAGS.size],
                author = "作者${index % 5 + 1}",
                imageModel = images[index % images.size],
            )
        }
    }

    /** 横向 / 网格:一组图标项 */
    fun cells(count: Int = 10): List<CellItem> = (1..count).map { i ->
        CellItem(id = i, label = "功能$i", imageModel = images[i % images.size])
    }

    /** 瀑布流:一组卡片,正文长短按 index 变化制造错落高度 */
    fun cards(count: Int = 12): List<CardItem> = (1..count).map { i ->
        val paragraphs = (i % 4 + 1)
        CardItem(
            id = i,
            title = "瀑布流卡片 #$i",
            content = ("这是第 $i 张卡片的内容,正文行数会变化以制造不同高度。").repeat(paragraphs),
            colorArgb = blockColors[i % blockColors.size],
        )
    }

    /**
     * 多类型列表:按"标题 → 两篇图文 → 一条便签"的节奏循环排布,一屏内即可看到三种 item 混排。
     * [groupCount] 控制分组数量,每组头部一个 [MultiItem.Header]。
     */
    fun multiItems(groupCount: Int = 3): List<MultiItem> {
        val list = mutableListOf<MultiItem>()
        for (g in 1..groupCount) {
            list += MultiItem.Header("第 $g 组 · 图文混排")
            repeat(2) { k ->
                val index = g * 10 + k
                list += MultiItem.Article(
                    title = "图文卡片 $index",
                    summary = "BaseMultiItemAdapter 按类型分发 onCreate/onBind,不同布局互不干扰。",
                    imageModel = images[index % images.size],
                )
            }
            list += MultiItem.Note("便签 $g:纯文本类型,无图片,验证另一种 ViewHolder 的复用与绑定。")
        }
        return list
    }

    private val TAGS = arrayOf("推荐", "热门", "置顶", "最新", "精选")
}
