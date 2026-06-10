package io.legado.app.ui.about

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import io.legado.app.data.appDb
import io.legado.app.ui.book.readRecord.ReadPeriod
import io.legado.app.ui.book.readRecord.ReadRecordOverviewScreen
import io.legado.app.ui.book.readRecord.ReadRecordOverviewState
import io.legado.app.ui.book.search.SearchActivity
import io.legado.app.ui.common.compose.LegadoTheme
import io.legado.app.ui.widget.ReadBarChartView
import io.legado.app.utils.startActivityForBook
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Locale

class ReadRecordOverviewActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LegadoTheme {
                var state by remember { mutableStateOf(ReadRecordOverviewState()) }

                fun load(period: ReadPeriod, refDate: LocalDate) {
                    lifecycleScope.launch {
                        state = state.copy(period = period, referenceDate = refDate)
                        val cal = Calendar.getInstance()
                        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        val today = fmt.format(cal.time)
                        val refInstant = refDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                        cal.time = java.util.Date.from(refInstant)

                        val startDate = when (period) {
                            ReadPeriod.DAY -> { cal.add(Calendar.DAY_OF_YEAR, -30); fmt.format(cal.time) }
                            ReadPeriod.WEEK -> { cal.add(Calendar.DAY_OF_YEAR, -7); fmt.format(cal.time) }
                            ReadPeriod.MONTH -> { cal.add(Calendar.MONTH, -1); fmt.format(cal.time) }
                            ReadPeriod.YEAR -> { cal.add(Calendar.YEAR, -1); fmt.format(cal.time) }
                            else -> ""
                        }
                        val endDate = if (period != ReadPeriod.ALL) fmt.format(java.util.Date.from(refInstant)) else today

                        val totalTime = withContext(IO) { appDb.readRecordDao.allTime }
                        val showRecords = withContext(IO) { appDb.readRecordDao.allShow }
                        val dailyRecords = if (period != ReadPeriod.ALL)
                            withContext(IO) { appDb.dailyReadRecordDao.sumDailyByDateRange(startDate, endDate) }
                        else Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -364) }
                            .let { c -> withContext(IO) { appDb.dailyReadRecordDao.sumDailyByDateRange(fmt.format(c.time), today) } }

                        val todayTime = withContext(IO) { appDb.dailyReadRecordDao.sumByDateRange(today, today) }
                        val readingDays = withContext(IO) {
                            var count = 0; val c = Calendar.getInstance(); val f = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            while (true) { val d = f.format(c.time); if (appDb.dailyReadRecordDao.sumByDateRange(d, d) > 0) { count++; c.add(Calendar.DAY_OF_YEAR, -1) } else break }; count
                        }
                        val barItems = dailyRecords.filter { it.readTime > 0 }.map { ReadBarChartView.BarItem(it.date.takeLast(5), it.readTime) }
                        val topBooks = showRecords.sortedByDescending { it.readTime }.take(20)

                        state = state.copy(totalTime = totalTime, readingDays = readingDays, totalBooks = showRecords.size,
                            todayTime = todayTime, topBooks = topBooks, dailyBarItems = barItems, heatmapData = dailyRecords.associate { it.date to it.readTime })
                    }
                }

                fun prevDate() { val ref = state.referenceDate; load(state.period, when (state.period) { ReadPeriod.DAY -> ref.minusDays(1); ReadPeriod.WEEK -> ref.minusWeeks(1); ReadPeriod.MONTH -> ref.minusMonths(1); ReadPeriod.YEAR -> ref.minusYears(1); else -> ref }) }
                fun nextDate() { val ref = state.referenceDate; load(state.period, when (state.period) { ReadPeriod.DAY -> ref.plusDays(1); ReadPeriod.WEEK -> ref.plusWeeks(1); ReadPeriod.MONTH -> ref.plusMonths(1); ReadPeriod.YEAR -> ref.plusYears(1); else -> ref }) }

                load(state.period, state.referenceDate)

                ReadRecordOverviewScreen(
                    state = state, onPeriodChange = { load(it, if (it == ReadPeriod.ALL) LocalDate.now() else state.referenceDate) },
                    onPrevDate = { prevDate() }, onNextDate = { nextDate() }, onBack = { finish() },
                    onBookClick = { name, _ ->
                        lifecycleScope.launch { val b = withContext(IO) { appDb.bookDao.findByName(name).firstOrNull() }; if (b != null) startActivityForBook(b) else SearchActivity.start(this@ReadRecordOverviewActivity, name) }
                    },
                )
            }
        }
    }
}
