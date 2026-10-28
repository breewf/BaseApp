package com.hy.demo.touch

import com.hy.myapp.R

/** 横向拖拽的色块 */
data class DragTile(val id: Int, val label: String, val colorArgb: Int)

/** 九宫格图片;[isAdd] 为 true 表示末尾固定的「＋」 */
data class GridImage(val id: Int, val url: Any = "", val isAdd: Boolean = false)

/** 侧滑列表的一行 */
data class TaskItem(val id: Int, val title: String, val desc: String, val colorArgb: Int)

/**
 * desc: 拖拽 / 侧滑演示用的假数据。
 *
 * 图片混用网络图和本地图,断网时也能看出排序变化。
 */
object TouchFakeData {

    /** 添加新图时按顺序循环取 */
    val imagePool: List<Any> = listOf(
        "https://www.gstatic.com/webp/gallery/1.jpg",
        "https://www.gstatic.com/webp/gallery/2.jpg",
        "https://www.gstatic.com/webp/gallery/3.jpg",
        "https://www.gstatic.com/webp/gallery/4.jpg",
        "https://www.gstatic.com/webp/gallery/5.jpg",
        R.mipmap.img_lab_view1,
        R.mipmap.img_lab_view2,
    )

    private val blockColors = intArrayOf(
        0xFF72ABF1.toInt(), 0xFFFF8A21.toInt(), 0xFF24899C.toInt(),
        0xFFFF4848.toInt(), 0xFF6B8E23.toInt(), 0xFF8A2BE2.toInt(),
    )

    /** 横向列表。返回可变集合:BRVAH4 的 move 会原地换位 */
    fun tiles(count: Int = 6): MutableList<DragTile> = MutableList(count) { i ->
        DragTile(id = i + 1, label = "块 ${i + 1}", colorArgb = blockColors[i % blockColors.size])
    }

    /** 九宫格:默认 7 张图,末尾补一个固定的「＋」 */
    fun gridImages(count: Int = 7): MutableList<GridImage> = MutableList(count) { i ->
        GridImage(id = i + 1, url = imagePool[i % imagePool.size])
    }.apply { add(GridImage(id = -1, isAdd = true)) }

    /** 侧滑列表,每条对应一个演示点 */
    fun tasks(): MutableList<TaskItem> = mutableListOf(
        TaskItem(1, "拖拽排序", "长按整行上下拖动,松手落位", blockColors[0]),
        TaskItem(2, "侧滑操作", "左滑露出「备注 / 删除」,滑开后固定", blockColors[1]),
        TaskItem(3, "删除撤销", "底部 Snackbar 点「撤销」,插回原位置", blockColors[2]),
        TaskItem(4, "运行时开关", "顶部按钮分别关掉拖拽 / 侧滑", blockColors[3]),
        TaskItem(5, "恢复默认", "重新灌数据,顺序与删除一起还原", blockColors[4]),
        TaskItem(6, "点击反馈", "点击行弹 Toast,拖拽/侧滑互不影响", blockColors[5]),
    )
}
