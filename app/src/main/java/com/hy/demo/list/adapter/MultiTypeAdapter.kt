package com.hy.demo.list.adapter

import android.content.Context
import android.view.ViewGroup
import android.widget.ImageView
import com.chad.library.adapter4.BaseMultiItemAdapter
import com.chad.library.adapter4.viewholder.QuickViewHolder
import com.hy.basic.utils.ImageLoader
import com.hy.myapp.R
import com.hy.demo.list.MultiItem

/**
 * desc: 多类型适配器示例(BRVAH4 BaseMultiItemAdapter)
 *
 * 4.x 多类型不再有 MultiItemEntity,改为在 init 里声明式注册:
 * - [onItemViewType]:单方法 fun interface,可用 lambda;按 [MultiItem] 子类型返回视图类型常量;
 * - [addItemType]:每种类型注册一个 [BaseMultiItemAdapter.OnMultiItemAdapterListener]
 *   (含 onCreate + onBind 两个抽象方法,故用 object 表达式而非 lambda);
 * 三种类型复用轻量的 [QuickViewHolder],各自 inflate 不同布局、各自绑定,互不干扰。
 */
class MultiTypeAdapter : BaseMultiItemAdapter<MultiItem>() {

    init {
        onItemViewType { position, list -> MultiItem.typeOf(list?.getOrNull(position)) }

        // 类型一:分组标题(整行)
        addItemType(MultiItem.TYPE_HEADER, object : OnMultiItemAdapterListener<MultiItem, QuickViewHolder> {
            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int) =
                QuickViewHolder(R.layout.item_multi_header, parent)

            override fun onBind(holder: QuickViewHolder, position: Int, item: MultiItem?) {
                (item as? MultiItem.Header)?.let { holder.setText(R.id.tvSectionTitle, it.title) }
            }
        })

        // 类型二:图文卡片(标题 + 摘要 + 封面)
        addItemType(MultiItem.TYPE_ARTICLE, object : OnMultiItemAdapterListener<MultiItem, QuickViewHolder> {
            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int) =
                QuickViewHolder(R.layout.item_multi_article, parent)

            override fun onBind(holder: QuickViewHolder, position: Int, item: MultiItem?) {
                val article = item as? MultiItem.Article ?: return
                with(holder) {
                    setText(R.id.tvArticleTitle, article.title)
                    setText(R.id.tvArticleSummary, article.summary)
                    ImageLoader.loadRoundImage(getView<ImageView>(R.id.ivArticle), article.imageModel, radiusDp = 8)
                }
            }
        })

        // 类型三:纯文本便签
        addItemType(MultiItem.TYPE_NOTE, object : OnMultiItemAdapterListener<MultiItem, QuickViewHolder> {
            override fun onCreate(context: Context, parent: ViewGroup, viewType: Int) =
                QuickViewHolder(R.layout.item_multi_note, parent)

            override fun onBind(holder: QuickViewHolder, position: Int, item: MultiItem?) {
                (item as? MultiItem.Note)?.let { holder.setText(R.id.tvNote, it.content) }
            }
        })
    }
}
