package com.gamemale.client

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import kotlinx.serialization.Serializable

/**
 * 导航键。首页四个标签是同一个 Home 里的横向 Pager，不各占一条栈。
 * 帖子、版块、搜索、设置压在首页栈上。深链单独一条栈，返回即离开应用。
 */
/**
 * 一次栈顶替换怎么出现。
 * [Enter] 是打开另一页，从点击处展开。
 * [InPlace] 是留在当前页，只换地址和内容，例如翻页。
 */
enum class NavArrival {
    Enter,
    InPlace,
}

@Serializable
sealed interface AppRoute : NavKey {
    @Serializable data object Home : AppRoute
    /** 只作为深链栈的标识，不会显示成页面 */
    @Serializable data object Deep : AppRoute
    @Serializable data class Thread(val url: String, val title: String) : AppRoute
    @Serializable data class Forum(val url: String, val title: String) : AppRoute
    @Serializable data object Search : AppRoute
    @Serializable data object Settings : AppRoute
    @Serializable data object Attachments : AppRoute
}

private val stackKeys = listOf(
    AppRoute.Home,
    AppRoute.Deep,
)

/**
 * 首页栈加一条深链栈。界面只显示当前栈顶；首页里的四个标签由 Pager 自己切换。
 */
class AppNavigation(
    private val stacks: Map<AppRoute, NavBackStack<NavKey>>,
    activeReader: () -> String,
    private val activeWriter: (String) -> Unit,
) {
    private val readActive = activeReader

    /** 最近一次 [replaceTop] 带进来的呈现方式，只对那一次传入的路由对象有效。 */
    private var arrivalRoute: AppRoute? = null
    private var arrival: NavArrival = NavArrival.Enter

    fun arrivalOf(route: AppRoute): NavArrival =
        if (route === arrivalRoute) arrival else NavArrival.Enter

    var activeId: String
        get() = readActive()
        set(value) = activeWriter(value)

    val activeStack: AppRoute
        get() = stackKeys.first { it.stackId() == activeId }

    fun stack(): NavBackStack<NavKey> = stacks.getValue(activeStack)

    val top: AppRoute
        get() = stack().last() as AppRoute

    /** 栈顶是首页时才显示底栏；首页内部的标签切换不改变栈顶 */
    val showBottomBar: Boolean
        get() = top == AppRoute.Home

    /** 栈里还有上一页，或当前是深链帖子（返回应退出应用） */
    val canGoBack: Boolean
        get() = stack().size > 1 || activeStack == AppRoute.Deep

    fun push(route: AppRoute) {
        arrivalRoute = null
        stack().add(route)
    }

    fun replaceTop(route: AppRoute, arrival: NavArrival = NavArrival.Enter) {
        arrivalRoute = route
        this.arrival = arrival
        val current = stack()
        if (current.isEmpty()) current.add(route) else current[current.lastIndex] = route
    }

    fun openDeepLink(thread: AppRoute.Thread) {
        val deep = stacks.getValue(AppRoute.Deep)
        deep.clear()
        deep.add(thread)
        activeId = AppRoute.Deep.stackId()
    }

    /**
     * 弹出当前栈顶。已经在首页或深链帖子上时返回 null，调用方结束 Activity。
     * 弹出成功时返回新的栈顶，供缓存还原。
     */
    fun pop(): AppRoute? {
        arrivalRoute = null
        val current = stack()
        if (current.size <= 1) return null
        current.removeLast()
        return current.last() as AppRoute
    }

    @Composable
    fun decoratedEntries(
        entryProvider: (NavKey) -> NavEntry<NavKey>,
    ): List<NavEntry<NavKey>> {
        val current = activeStack
        val built = stackKeys.map { key ->
            val decorators = listOf(rememberSaveableStateHolderNavEntryDecorator<NavKey>())
            key to rememberDecoratedNavEntries(
                backStack = stacks.getValue(key),
                entryDecorators = decorators,
                entryProvider = entryProvider,
            )
        }
        return built.first { it.first == current }.second
    }
}

private fun AppRoute.stackId(): String = when (this) {
    AppRoute.Home -> "home"
    AppRoute.Deep -> "deep"
    else -> "home"
}

@Composable
fun rememberAppNavigation(deepLinkUrl: String?): AppNavigation {
    val launchThread = remember(deepLinkUrl) {
        deepLinkUrl?.let { AppRoute.Thread(it, "帖子") }
    }
    val home = rememberNavBackStack(AppRoute.Home)
    val deep = rememberNavBackStack(launchThread ?: AppRoute.Deep)
    var activeId by rememberSaveable {
        mutableStateOf(if (launchThread != null) "deep" else "home")
    }
    val stacks = remember(home, deep) {
        mapOf(
            AppRoute.Home to home,
            AppRoute.Deep to deep,
        )
    }
    return remember(stacks) {
        AppNavigation(stacks, { activeId }, { activeId = it })
    }
}
