package eu.kanade.tachiyomi.data.library

import com.evernote.android.job.Job
import com.evernote.android.job.JobManager
import com.evernote.android.job.JobRequest
import eu.kanade.tachiyomi.data.preference.PreferencesHelper
import eu.kanade.tachiyomi.data.preference.getOrDefault
import uy.kohesive.injekt.Injekt
import uy.kohesive.injekt.api.get
import java.util.concurrent.TimeUnit

class LibraryUpdateJob : Job() {

    override fun onRunJob(params: Params): Result {
        LibraryUpdateService.start(context)
        return Job.Result.SUCCESS
    }

    companion object {
        const val TAG = "LibraryUpdate"

        fun setupTask(prefInterval: Int? = null) {
            val preferences = Injekt.get<PreferencesHelper>()
            val interval = (prefInterval ?: preferences.libraryUpdateInterval().getOrDefault())
                .toLong()
            if (interval > 0) {
                val restrictions = preferences.libraryUpdateRestriction()!!
                val acRestriction = "ac" in restrictions
                val wifiRestriction = if ("wifi" in restrictions)
                    JobRequest.NetworkType.UNMETERED
                else
                    JobRequest.NetworkType.CONNECTED

                JobRequest.Builder(TAG)
                        .setPeriodic(
                            TimeUnit.HOURS.toMillis(interval), TimeUnit.MINUTES.toMillis
                                (10))
                        .setRequiredNetworkType(wifiRestriction)
                        .setRequiresCharging(acRestriction)
                        .setRequirementsEnforced(true)
                        .setUpdateCurrent(true)
                        .build()
                        .schedule()
            }
        }

        fun cancelTask() {
            JobManager.instance().cancelAllForTag(TAG)
        }
    }
}