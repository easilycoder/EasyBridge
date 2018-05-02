package tech.easily.webviewperformancedemo

data class PerformanceTiming(val connectEnd: Long, val connectStart: Long, val domComplete: Long, val domContentLoadedEventEnd: Long,
                             val domContentLoadedEventStart: Long, val domInteractive: Long, val domLoading: Long, val domainLookupEnd: Long,
                             val domainLookupStart: Long, val fetchStart: Long, val loadEventEnd: Long, val loadEventStart: Long, val navigationStart: Long,
                             val redirectEnd: Long, val redirectStart: Long, val requestStart: Long, val responseEnd: Long, val responseStart: Long,
                             val secureConnectionStart: Long, val unloadEventEnd: Long, val unloadEventStart: Long)