package com.payu.kube.log.ui.compose.tab

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.snapshotFlow
import com.payu.kube.log.model.PodInfo
import com.payu.kube.log.service.PodLogService
import com.payu.kube.log.ui.compose.tab.content.SearchState
import com.payu.kube.log.ui.compose.tab.content.SearchType
import com.payu.kube.log.ui.compose.tab.content.SettingsState
import com.payu.kube.log.util.FlowUtils.debounceWithCache
import com.payu.kube.log.util.Item
import com.payu.kube.log.util.ShowMoreAfterItem
import com.payu.kube.log.util.ShowMoreBeforeItem
import com.payu.kube.log.util.VirtualItem
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.math.max
import kotlin.math.min

class LogTab(initialPodInfo: PodInfo, parentScope: CoroutineScope, allListFlow: Flow<List<PodInfo>>) {
    companion object {
        private const val MORE_ELEMENT = 3
    }

    private val coroutineScope = parentScope + SupervisorJob()

    val podInfoState = allListFlow
        .map { list -> list.firstOrNull { it.isSamePod(initialPodInfo) } }
        .filterNotNull()
        .stateIn(coroutineScope, SharingStarted.Eagerly, initialPodInfo)
    val podInfo: PodInfo
        get() = podInfoState.value

    val newestAppPodState = allListFlow
        .map { list ->
            list.filter { it.calculatedAppName == podInfo.calculatedAppName }
                .maxByOrNull { it.creationTimestamp }
                ?.takeIf { !it.isSamePod(podInfo) }
        }
        .stateIn(coroutineScope, SharingStarted.Eagerly, initialPodInfo)

    val settings = SettingsState()
    val search = SearchState()

    private var allLogs = listOf<String>()
    val logs = MutableStateFlow(listOf<VirtualItem<String>>())

    val scrollState = LazyListState(0, 0)

    fun init() {
        snapshotFlow {
            search.query.value to search.searchType
        }.onEach { (query, searchType) ->
            if (searchType == SearchType.FILTER && query != null) {
                logs.value = recalculate(allLogs) { query.check(it) }
            } else {
                logs.value = recalculate(allLogs) { true }
            }
        }.launchIn(coroutineScope)

        PodLogService.watchingLogsSuspending(podInfoState)
            .debounceWithCache(50)
            .filter { it.isNotEmpty() }
            .buffer()
            .flowOn(Dispatchers.IO)
            .onEach { addLogLines(it) }
            .launchIn(coroutineScope)
    }

    fun clear() {
        allLogs = listOf()
        logs.value = listOf()
    }

    fun destroy() {
        coroutineScope.cancel()
    }

    private fun addLogLines(newLines: List<String>) {
        val startRealIndex = allLogs.size
        allLogs = allLogs + newLines
        val newItems = newLines
            .mapIndexed { index, line -> Item(line, startRealIndex + index) }

        val query = search.query.value
        if (search.searchType == SearchType.FILTER && query != null) {
            logs.value = addNewItems(logs.value, newItems) { query.check(it) }
        } else {
            logs.value += newItems
        }
    }

    private fun addNewItems(showedLogList: List<VirtualItem<String>>,
                            items: List<Item<String>>,
                            predicate: (String) -> Boolean):
            List<VirtualItem<String>> {
        val newShowedList = showedLogList.toMutableList()
        for (item in items) {
            val lastElement = newShowedList.lastOrNull()
            if (predicate(item.value)) {
                if (lastElement == null) {
                    if (item.index > 0) {
                        newShowedList.add(ShowMoreBeforeItem(item))
                    }
                    newShowedList.add(item)
                } else if (lastElement is ShowMoreAfterItem) {
                    if (lastElement.originalIndex + 1 == item.originalIndex) {
                        newShowedList[newShowedList.lastIndex] = item
                    } else {
                        newShowedList.add(ShowMoreBeforeItem(item))
                        newShowedList.add(item)
                    }
                } else {
                    newShowedList.add(item)
                }
            } else {
                if (lastElement is Item) {
                    newShowedList.add(ShowMoreAfterItem(lastElement))
                }
            }
        }
        return newShowedList
    }

    private fun recalculate(allLogs: List<String>, predicate: (String) -> Boolean): List<VirtualItem<String>> {
        val newShowedList = mutableListOf<VirtualItem<String>>()
        var areAnyElementsBefore = false
        for (i in allLogs.indices) {
            val value = allLogs[i]
            val isShowed = predicate(value)
            if (isShowed) {
                val lastElement = newShowedList.lastOrNull()
                val item = Item(value, i)
                if (areAnyElementsBefore) {
                    if (lastElement is Item) {
                        newShowedList.add(ShowMoreAfterItem(lastElement))
                    }
                    newShowedList.add(ShowMoreBeforeItem(item))
                }
                newShowedList.add(item)
            }
            areAnyElementsBefore = !isShowed
        }

        newShowedList.lastOrNull()
            ?.takeIf { areAnyElementsBefore && it is Item }
            ?.let { newShowedList.add(ShowMoreAfterItem(it as Item)) }

        return newShowedList
    }

    fun showBefore(cellIndex: Int) {
        val item = logs.value[cellIndex] as? ShowMoreBeforeItem ?: return

        var prevItem: Item<String>? = null
        var actualIndex = cellIndex + 1
        for (i in cellIndex - 1 downTo 0) {
            prevItem = logs.value[i] as? Item ?: continue
            break
        }

        val newLogs = logs.value.toMutableList()
        val minAddedElement = max((prevItem?.originalIndex ?: -1) + 1, item.originalIndex - MORE_ELEMENT)
        var lastAddedItem: Item<String>? = null
        for (i in item.originalIndex - 1 downTo minAddedElement) {
            val newItem = Item(allLogs[i], i)
            newLogs.add(actualIndex, newItem)
            lastAddedItem = newItem
        }

        lastAddedItem ?: return
        actualIndex--
        newLogs[actualIndex] = ShowMoreBeforeItem(lastAddedItem)

        if (lastAddedItem.originalIndex == 0 ||
            lastAddedItem.originalIndex - 1 == prevItem?.originalIndex) {
            newLogs.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreBeforeItem }
                ?.let { newLogs.removeAt(actualIndex) }
            actualIndex--

            newLogs.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreAfterItem }
                ?.let { newLogs.removeAt(actualIndex) }
        }
        logs.value = newLogs
    }

    fun showAfter(cellIndex: Int) {
        val item = logs.value[cellIndex] as? ShowMoreAfterItem ?: return

        var nextItem: Item<String>? = null
        var actualIndex = cellIndex
        for (i in cellIndex + 1 until logs.value.size) {
            nextItem = logs.value[i] as? Item ?: continue
            break
        }

        val newLogs = logs.value.toMutableList()

        val maxAddedElement = min(nextItem?.originalIndex ?: allLogs.size, item.originalIndex + 1 + MORE_ELEMENT)
        var lastAddedItem: Item<String>? = null
        for (i in item.originalIndex + 1 until maxAddedElement) {
            val newItem = Item(allLogs[i], i)
            newLogs.add(actualIndex, newItem)
            actualIndex++
            lastAddedItem = newItem
        }

        lastAddedItem ?: return
        newLogs[actualIndex] = ShowMoreAfterItem(lastAddedItem)

        if (lastAddedItem.originalIndex == allLogs.lastIndex ||
            lastAddedItem.originalIndex + 1 == nextItem?.originalIndex) {
            newLogs.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreAfterItem }
                ?.let { newLogs.removeAt(actualIndex) }

            newLogs.getOrNull(actualIndex)
                ?.takeIf { it is ShowMoreBeforeItem }
                ?.let { newLogs.removeAt(actualIndex) }
        }
        logs.value = newLogs
    }
}