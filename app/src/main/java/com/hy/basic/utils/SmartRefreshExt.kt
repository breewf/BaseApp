package com.hy.basic.utils

import com.scwang.smartrefresh.layout.SmartRefreshLayout
import com.scwang.smartrefresh.layout.footer.ClassicsFooter
import com.scwang.smartrefresh.layout.header.ClassicsHeader

/**
 * desc: SmartRefreshLayout 下拉刷新 / 上拉加载封装
 *
 * 把"注册头尾、绑定监听、结束动画"这些每页都要抄一遍的样板收口:
 * - [applyDefaults]:统一默认经典头/尾(不写 XML 头尾时用这个,风格全局一致);
 * - [bindPaging]:把下拉、上拉两个手势接到外部(通常是 Activity/Fragment 里 launch 一个 viewModel 挂起方法);
 * - [refreshDone] / [loadMoreDone] / [refreshFailed] / [loadMoreFailed]:按请求结果结束动画并维护"没有更多"。
 *
 * 约定:业务只负责"取数据 + 告诉我成功与否/还有没有更多",刷新框架的收尾全在这里。
 */

/** 设置经典上拉下拉头尾,默认开启"滑到底部自动预加载下一页" */
fun SmartRefreshLayout.applyDefaults(
    enableLoadMore: Boolean = true,
): SmartRefreshLayout = apply {
    setRefreshHeader(ClassicsHeader(context))
    // finishDuration=0:上拉加载成功后不在"加载完成"态停留(默认约 500ms),
    // 直接收起 footer 插入数据,避免"先看到成功态→再收起→再插数据"的割裂感。
    setRefreshFooter(ClassicsFooter(context).setFinishDuration(0))
    setEnableLoadMore(enableLoadMore)
    // 自动加载:滚动到底部(或快到底部)即触发上拉手势,无需用户手动再拽一次 footer
    setEnableAutoLoadMore(enableLoadMore)
}

/**
 * 绑定下拉刷新 / 上拉加载两个手势。
 *
 * 回调里由调用方发起真正的加载并在完成后调用 [refreshDone]/[loadMoreDone] 等收尾,
 * 本方法只管"手势 → 通知",不关心数据来源。[onLoadMore] 为 null 时不接上拉手势。
 */
fun SmartRefreshLayout.bindPaging(
    onRefresh: () -> Unit,
    onLoadMore: (() -> Unit)? = null,
): SmartRefreshLayout = apply {
    setOnRefreshListener { onRefresh() }
    if (onLoadMore != null) {
        setEnableLoadMore(true)
        setOnLoadMoreListener { onLoadMore() }
    } else {
        setEnableLoadMore(false)
    }
}

/** 下拉刷新成功:[hasMore]=false 时额外标记"没有更多"(下拉场景一般总是有更多) */
fun SmartRefreshLayout.refreshDone(hasMore: Boolean = true) {
    finishRefresh(true)
    if (!hasMore) finishRefreshWithNoMoreData() else resetNoMoreData()
}

/** 下拉刷新失败:头部长按失败态收起 */
fun SmartRefreshLayout.refreshFailed() {
    finishRefresh(false)
}

/**
 * 上拉加载完成:[hasMore]=false 则展示"没有更多数据"并停止上拉。
 * 成功时只收起转圈、直接插入数据(不额外提示文案);
 * 配合 [applyDefaults] 中 ClassicsFooter.setFinishDuration(0),收起即完成、无成功态停留。
 */
fun SmartRefreshLayout.loadMoreDone(hasMore: Boolean) {
    if (hasMore) finishLoadMore(0) else finishLoadMoreWithNoMoreData()
}

/** 上拉加载失败 */
fun SmartRefreshLayout.loadMoreFailed() {
    finishLoadMore(false)
}
