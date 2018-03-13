package io.github.feelfreelinux.wykopmobilny.ui.widgets.link.comment

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialog
import android.support.v4.app.ShareCompat
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import io.github.feelfreelinux.wykopmobilny.R
import io.github.feelfreelinux.wykopmobilny.models.dataclass.LinkComment
import io.github.feelfreelinux.wykopmobilny.models.pojo.apiv2.models.LinkVoteResponse
import io.github.feelfreelinux.wykopmobilny.ui.dialogs.showExceptionDialog
import io.github.feelfreelinux.wykopmobilny.ui.modules.profile.ProfileActivity
import io.github.feelfreelinux.wykopmobilny.utils.SettingsPreferencesApi
import io.github.feelfreelinux.wykopmobilny.utils.api.getGroupColor
import io.github.feelfreelinux.wykopmobilny.utils.getActivityContext
import io.github.feelfreelinux.wykopmobilny.utils.isVisible
import io.github.feelfreelinux.wykopmobilny.utils.textview.prepareBody
import io.github.feelfreelinux.wykopmobilny.utils.usermanager.UserManagerApi
import kotlinx.android.synthetic.main.link_comment_layout.view.*
import kotlinx.android.synthetic.main.link_comment_menu_bottomsheet.view.*
import kotlin.math.absoluteValue

class LinkCommentWidget(context: Context, attrs: AttributeSet) : ConstraintLayout(context, attrs), LinkCommentView {
    lateinit var comment : LinkComment
    lateinit var presenter : LinkCommentPresenter
    lateinit var settingsApi : SettingsPreferencesApi
    lateinit var userManagerApi : UserManagerApi
    var shouldEnableClickListener = false
    lateinit var collapseListener : (Boolean, Int) -> Unit
    init {
        View.inflate(context, R.layout.link_comment_layout, this)
        isClickable = true
        isFocusable = true
        val typedValue = TypedValue()
        getActivityContext()!!.theme?.resolveAttribute(R.attr.itemBackgroundColorStatelist, typedValue, true)
        setBackgroundResource(typedValue.resourceId)
    }

    var showHiddenCommentsCountCard : (Boolean) -> Unit = {}


    fun setLinkCommentData(linkComment: LinkComment, linkPresenter: LinkCommentPresenter, userManager: UserManagerApi, settingsPreferencesApi: SettingsPreferencesApi) {
        comment = linkComment
        userManagerApi = userManager
        presenter = linkPresenter
        presenter.subscribe(this)
        presenter.linkId = linkComment.id
        settingsApi = settingsPreferencesApi
        setupHeader()
        setupBody()
        setupButtons()
    }

    override fun markCommentAsRemoved() {
        commentContentTextView.setText(R.string.commentRemoved)
        commentImageView.isVisible = false
    }

    private fun setupHeader() {
        comment.author.apply {
            //val params = avatarView.layoutParams
            //params.width = if (comment.id != comment.parentId) resources.getDimensionPixelSize(R.dimen.avatar_comment_width_small) else resources.getDimensionPixelSize(R.dimen.avatar_comment_width)
            //avatarView.layoutParams = params
            avatarView.setAuthor(this)
            avatarView.setOnClickListener { openProfile(nick) }
            authorTextView.apply {
                text = nick
                setTextColor(context.getGroupColor(group))
                setOnClickListener { openProfile(nick) }
            }
            dateTextView.text = comment.date.replace(" temu", "")
            authorHeaderView.setAuthorData(comment.author, comment.date, comment.app)
        }
    }

    fun openProfile(username : String) {
        getActivityContext()!!.startActivity(ProfileActivity.createIntent(getActivityContext()!!, username))
    }

    private fun setupBody() {
        replyTextView.isVisible = userManagerApi.isUserAuthorized()
        commentImageView.setEmbed(comment.embed, settingsApi, presenter.newNavigatorApi, comment.isNsfw)
        val clickListener =  { presenter.handleUrl(url) }
        if (shouldEnableClickListener) setOnClickListener { clickListener() }
        else setOnClickListener(null)
        comment.body?.let {
            commentContentTextView.prepareBody(comment.body!!, { presenter.handleUrl(it) }, if (shouldEnableClickListener) clickListener else null, settingsApi.openSpoilersDialog)
        }
        collapseButton.setOnClickListener {
            commentContentTextView.isVisible = false
        }
        commentContentTextView.isVisible = !comment.body.isNullOrEmpty()
        if ((comment.id != comment.parentId) || comment.childCommentCount == 0) {
            showHiddenCommentsCountCard(false)
            collapseButton.isVisible = false
        } else collapseButton.isVisible = true

        val params = separatorView.layoutParams

        //avatarView.layoutParams = params
        if (comment.id != comment.parentId) {
            authorHeaderView.isVisible = false
            avatarView.isVisible = true
            authorTextView.isVisible = true
            dotTextView.isVisible = true
            dateTextView.isVisible = true
            params.height = resources.getDimensionPixelSize(R.dimen.separator_normal)
            commentContentTextView.setPadding(0, commentContentTextView.paddingTop, commentContentTextView.paddingRight, commentContentTextView.paddingBottom)
        } else {
            authorHeaderView.isVisible = true
            avatarView.isVisible = false
            authorTextView.isVisible = false
            dotTextView.isVisible = false
            dateTextView.isVisible = false
            params.height = resources.getDimensionPixelSize(R.dimen.separator_large)
            commentContentTextView.setPadding(resources.getDimensionPixelSize(R.dimen.padding_dp_large), commentContentTextView.paddingTop, commentContentTextView.paddingRight, commentContentTextView.paddingBottom)
        }
        separatorView.layoutParams = params
        //setPadding(if (comment.id != comment.parentId) resources.getDimensionPixelSize(R.dimen.comment_reply_padding) else 0, paddingTop, paddingRight, paddingBottom)
        requestLayout()
    }

    fun setStyleForComment(isAuthorComment: Boolean, commentId : Int = -1) {
        val credentials = userManagerApi.getUserCredentials()
        if (credentials != null && credentials.login == comment.author.nick) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBadgeOwn))
        } else if (isAuthorComment) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(context, R.color.colorBadgeAuthors))
        } else {
            authorBadgeStrip.isVisible = false
        }

        if (commentId == comment.id) {
            authorBadgeStrip.isVisible = true
            authorBadgeStrip.setBackgroundColor(ContextCompat.getColor(context, R.color.plusPressedColor))
        }

    }

    private fun setupButtons() {
        plusButton.setup(userManagerApi)
        plusButton.text = comment.voteCountPlus.toString()
        minusButton.setup(userManagerApi)
        minusButton.text = comment.voteCountMinus.absoluteValue.toString()
        moreOptionsTextView.setOnClickListener { openLinkCommentMenu() }
        shareTextView.setOnClickListener { shareUrl() }
        if (comment.isCollapsed) collapseComment(false)
        else expandComment(false)

        plusButton.voteListener = {
            presenter.voteUp()
            minusButton.isEnabled = false
        }

        minusButton.voteListener = {
            presenter.voteDown()
            plusButton.isEnabled = false
        }

        plusButton.unvoteListener = {
            minusButton.isEnabled = false
            presenter.voteCancel()
        }

        minusButton.unvoteListener = {
            plusButton.isEnabled = false
            presenter.voteCancel()
        }

        when (comment.userVote) {
            1 -> {
                plusButton.isButtonSelected = true
                minusButton.isButtonSelected = false
            }

            0 -> {
                plusButton.isButtonSelected = false
                minusButton.isButtonSelected = false
            }

            -1 -> {
                plusButton.isButtonSelected = false
                minusButton.isButtonSelected = true
            }
        }
    }

    override fun setVoteCount(voteResponse: LinkVoteResponse) {
        comment.voteCount = voteResponse.voteCount
        comment.voteCountMinus = voteResponse.voteCountMinus
        comment.voteCountPlus = voteResponse.voteCountPlus
        plusButton.voteCount = voteResponse.voteCountPlus
        minusButton.voteCount = voteResponse.voteCountMinus.absoluteValue
    }

    override fun markVotedMinus() {
        comment.userVote = -1
        minusButton.isEnabled = true
        plusButton.isEnabled = true
        minusButton.isButtonSelected = true
        plusButton.isButtonSelected = false
    }

    override fun markVotedPlus() {
        comment.userVote = 1
        minusButton.isEnabled = true
        plusButton.isEnabled = true
        minusButton.isButtonSelected = false
        plusButton.isButtonSelected = true
    }

    override fun markVoteRemoved() {
        comment.userVote = 0
        minusButton.isEnabled = true
        plusButton.isEnabled = true
        minusButton.isButtonSelected = false
        plusButton.isButtonSelected = false
    }

    fun openLinkCommentMenu() {
        val activityContext = getActivityContext()!!
        val dialog = BottomSheetDialog(activityContext)
        val bottomSheetView = activityContext.layoutInflater.inflate(R.layout.link_comment_menu_bottomsheet, null)
        dialog.setContentView(bottomSheetView)

        bottomSheetView.apply {
            comment_menu_copy.setOnClickListener {
                dialog.dismiss()
            }

            comment_menu_delete.setOnClickListener {
                presenter.deleteComment()
                dialog.dismiss()
            }

            comment_menu_report.setOnClickListener {
                presenter.reportContent(comment.violationUrl)
                dialog.dismiss()
            }

            comment_menu_edit.setOnClickListener {
                presenter.openEditCommentActivity(comment.id, comment.body!!)
                dialog.dismiss()
            }

            comment_menu_report.isVisible = userManagerApi.isUserAuthorized()

            val canUserEdit = userManagerApi.isUserAuthorized() && comment.author.nick == userManagerApi.getUserCredentials()!!.login
            comment_menu_delete.isVisible = canUserEdit
            comment_menu_edit.isVisible = canUserEdit
        }

        val mBehavior = BottomSheetBehavior.from(bottomSheetView.parent as View)
        dialog.setOnShowListener {
            mBehavior.peekHeight = bottomSheetView.height
        }
        dialog.show()
    }

    fun collapseComment(shouldCallListener : Boolean = true) {
        val typedArray = context.obtainStyledAttributes(arrayOf(
                R.attr.expandDrawable).toIntArray())
        collapseButton.setImageDrawable(typedArray.getDrawable(0))
        typedArray.recycle()
        collapseButton.setOnClickListener {
            expandComment()
        }
        showHiddenCommentsCountCard(true)
        if (shouldCallListener) collapseListener(true, comment.id)
    }

    fun expandComment(shouldCallListener : Boolean = true) {
        val typedArray = context.obtainStyledAttributes(arrayOf(
                R.attr.collapseDrawable).toIntArray())
        collapseButton.setImageDrawable(typedArray.getDrawable(0))
        typedArray.recycle()
        collapseButton.setOnClickListener {
            collapseComment()
        }
        showHiddenCommentsCountCard(false)
        if (shouldCallListener) collapseListener(false, comment.id)
    }

    override fun showErrorDialog(e: Throwable) {
        context.showExceptionDialog(e)
    }

    fun shareUrl() {
        ShareCompat.IntentBuilder
                .from(getActivityContext()!!)
                .setType("text/plain")
                .setChooserTitle(R.string.share)
                .setText(url)
                .startChooser()
    }

    val url : String
        get() = "https://www.wykop.pl/link/${comment.linkId}/#comment-${comment.id}"
}